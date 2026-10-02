package br.com.bibliotecaviva.integration;

import java.util.List;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import br.com.bibliotecaviva.repository.UsuarioRepository;
import br.com.bibliotecaviva.service.TokenService;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(CirculacaoPostgresTest.Relogio.class)
@EnabledIfEnvironmentVariable(named="SPRINT2_TEST_DB_URL",matches=".+")
@Sql("/sprint2-fixture.sql")
class CatalogoPostgresTest {
    @DynamicPropertySource static void banco(DynamicPropertyRegistry r) { CirculacaoPostgresTest.banco(r); }
    @Autowired WebApplicationContext context;
    @Autowired JdbcTemplate jdbc;
    @Autowired UsuarioRepository usuarios;
    @Autowired TokenService tokens;
    MockMvc mvc;
    String admin, atendente, leitor;
    @BeforeEach void preparar() {
        mvc=MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        admin=token(1); leitor=token(2); atendente=token(4);
    }
    String token(long id) { return "Bearer "+tokens.gerar(usuarios.findById(id).orElseThrow()); }
    String livro() { return "{\"titulo\":\"Obra nova\",\"isbn\":\"978-1234567890\",\"autorIds\":[1,2],\"categoriaIds\":[1,2]}"; }
    @Test void somenteAdminCadastraOuEditaAcervo() throws Exception {
        for (String token : List.of(leitor,atendente)) {
            mvc.perform(post("/admin/livros").header("Authorization",token).contentType(MediaType.APPLICATION_JSON).content(livro()))
                .andExpect(status().isForbidden());
            mvc.perform(get("/admin/exemplares").header("Authorization",token)).andExpect(status().isForbidden());
        }
        mvc.perform(get("/admin/livros")).andExpect(status().isUnauthorized());
        assertEquals(3,jdbc.queryForObject("SELECT count(*) FROM livro",Integer.class));
    }
    @Test void criaLivroComVariosVinculosEConsultaAcervo() throws Exception {
        mvc.perform(post("/admin/livros").header("Authorization",admin).contentType(MediaType.APPLICATION_JSON).content(livro()))
            .andExpect(status().isCreated()).andExpect(header().string("Location","/admin/livros/4"))
            .andExpect(jsonPath("$.isbn").value("9781234567890"))
            .andExpect(jsonPath("$.autores.length()").value(2)).andExpect(jsonPath("$.categorias.length()").value(2));
        mvc.perform(get("/acervo").header("Authorization",leitor).param("titulo","Obra nova"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.itens[0].exemplaresDisponiveis").value(0));
    }
    @Test void recusaIsbnSemPreencherEFormatoInvalido() throws Exception {
        for (String isbn : List.of("","abc"))
            mvc.perform(post("/admin/livros").header("Authorization",admin).contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\":\"Livro\",\"isbn\":\""+isbn+"\"}")).andExpect(status().isBadRequest());
        assertEquals(3,jdbc.queryForObject("SELECT count(*) FROM livro",Integer.class));
    }
    @Test void isbnDuplicadoNormalizadoRetorna409() throws Exception {
        mvc.perform(post("/admin/livros").header("Authorization",admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"titulo\":\"Livro\",\"isbn\":\"123-456-7890-123\"}")).andExpect(status().isConflict());
    }
    @Test void referenciasInvalidasNaoGravamLivro() throws Exception {
        mvc.perform(post("/admin/livros").header("Authorization",admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"titulo\":\"Livro\",\"isbn\":\"9781234567890\",\"autorIds\":[999]}"))
            .andExpect(status().isNotFound());
        assertEquals(3,jdbc.queryForObject("SELECT count(*) FROM livro",Integer.class));
    }
    @Test void editaLivroPreservandoEmprestimoEExemplar() throws Exception {
        mvc.perform(put("/admin/livros/1").header("Authorization",admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"titulo\":\"Título editado\",\"isbn\":\"1234567890123\",\"autorIds\":[2],\"categoriaIds\":[2]}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));
        assertEquals(1L,jdbc.queryForObject("SELECT livro_id FROM exemplar WHERE id=2",Long.class));
        assertEquals(2L,jdbc.queryForObject("SELECT exemplar_id FROM emprestimo WHERE id=1",Long.class));
        assertEquals(2L,jdbc.queryForObject("SELECT autor_id FROM livro_autor WHERE livro_id=1",Long.class));
    }
    @Test void updateInvalidoPreservaTituloEVinculos() throws Exception {
        mvc.perform(put("/admin/livros/1").header("Authorization",admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"titulo\":\"Não salvar\",\"isbn\":\"1234567890123\",\"categoriaIds\":[999]}"))
            .andExpect(status().isNotFound());
        assertEquals("Dom Casmurro",jdbc.queryForObject("SELECT titulo FROM livro WHERE id=1",String.class));
        assertEquals(2,jdbc.queryForObject("SELECT count(*) FROM livro_categoria WHERE livro_id=1",Integer.class));
    }
    @Test void autoresECategoriasCriadosConsultadosEEditados() throws Exception {
        for (String rota : List.of("autores","categorias")) {
            mvc.perform(post("/admin/"+rota).header("Authorization",admin).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\" Novo nome \"}")).andExpect(status().isCreated())
                .andExpect(header().string("Location","/admin/"+rota+"/3"));
            mvc.perform(get("/admin/"+rota+"/3").header("Authorization",admin)).andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Novo nome"));
            mvc.perform(put("/admin/"+rota+"/3").header("Authorization",admin).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Nome editado\"}")).andExpect(status().isOk());
        }
    }
    @Test void categoriaDuplicadaEComprimentoInvalidoRecusados() throws Exception {
        mvc.perform(post("/admin/categorias").header("Authorization",admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\"Romance\"}")).andExpect(status().isConflict());
        mvc.perform(post("/admin/categorias").header("Authorization",admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\""+"x".repeat(81)+"\"}")).andExpect(status().isBadRequest());
    }
    @Test void exemplarNovoSempreDisponivel() throws Exception {
        mvc.perform(post("/admin/exemplares").header("Authorization",admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"livroId\":1,\"codigoTombo\":\" N1 \",\"estadoConservacao\":\"novo\",\"status\":\"emprestado\"}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("disponivel"))
            .andExpect(jsonPath("$.codigoTombo").value("N1"));
    }
    @Test void recusaTomboDuplicadoLivroAusenteEConservacaoInvalida() throws Exception {
        mvc.perform(post("/admin/exemplares").header("Authorization",admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"livroId\":1,\"codigoTombo\":\"T1\",\"estadoConservacao\":\"bom\"}"))
            .andExpect(status().isConflict());
        mvc.perform(post("/admin/exemplares").header("Authorization",admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"livroId\":999,\"codigoTombo\":\"N1\",\"estadoConservacao\":\"bom\"}"))
            .andExpect(status().isNotFound());
        mvc.perform(post("/admin/exemplares").header("Authorization",admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"livroId\":1,\"codigoTombo\":\"N1\",\"estadoConservacao\":\"inválido\"}"))
            .andExpect(status().isBadRequest());
    }
    @Test void livroInativoNaoRecebeExemplares() throws Exception {
        jdbc.update("UPDATE livro SET ativo=false WHERE id=1");
        mvc.perform(post("/admin/exemplares").header("Authorization",admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"livroId\":1,\"codigoTombo\":\"N1\",\"estadoConservacao\":\"bom\"}"))
            .andExpect(status().isConflict());
    }
    @Test void editarExemplarMantemLivroStatusEHistorico() throws Exception {
        mvc.perform(put("/admin/exemplares/2").header("Authorization",admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"codigoTombo\":\"N2\",\"estadoConservacao\":\"regular\",\"livroId\":2,\"status\":\"disponivel\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.livroId").value(1))
            .andExpect(jsonPath("$.status").value("emprestado"));
        assertEquals(2L,jdbc.queryForObject("SELECT exemplar_id FROM emprestimo WHERE id=1",Long.class));
    }
    @Test void situacaoManualNaoDesfazEmprestimoOuReserva() throws Exception {
        mvc.perform(patch("/admin/exemplares/2/situacao").header("Authorization",admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"ativo\":false}")).andExpect(status().isConflict());
        jdbc.update("UPDATE exemplar SET status='reservado' WHERE id=1");
        jdbc.update("INSERT INTO reserva(leitor_id,livro_id,exemplar_id,status) VALUES (2,1,1,'disponivel')");
        mvc.perform(patch("/admin/exemplares/1/situacao").header("Authorization",admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"ativo\":false}")).andExpect(status().isConflict());
    }
    @Test void inativaEReativaExemplarLivre() throws Exception {
        mvc.perform(patch("/admin/exemplares/1/situacao").header("Authorization",admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"ativo\":false}")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("inativo"));
        mvc.perform(patch("/admin/exemplares/1/situacao").header("Authorization",admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"ativo\":true}")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("disponivel"));
    }
    @Test void listaPaginadaComFiltroDeLivro() throws Exception {
        mvc.perform(get("/admin/exemplares").header("Authorization",admin).param("livroId","1").param("tamanho","1"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(2)).andExpect(jsonPath("$.itens.length()").value(1));
        mvc.perform(get("/admin/livros").header("Authorization",admin).param("pagina","-1")).andExpect(status().isBadRequest());
    }
    @Test void fluxoCadastrarLivroExemplarEmprestarDevolver() throws Exception {
        mvc.perform(post("/admin/livros").header("Authorization",admin).contentType(MediaType.APPLICATION_JSON).content(livro()))
            .andExpect(status().isCreated());
        mvc.perform(post("/admin/exemplares").header("Authorization",admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"livroId\":4,\"codigoTombo\":\"NOVO\",\"estadoConservacao\":\"bom\"}"))
            .andExpect(status().isCreated());
        mvc.perform(post("/atendente/emprestimos").header("Authorization",atendente).contentType(MediaType.APPLICATION_JSON)
            .content("{\"leitorId\":2,\"exemplarId\":5}")).andExpect(status().isCreated());
        mvc.perform(post("/atendente/emprestimos/2/devolucao").header("Authorization",atendente))
            .andExpect(status().isOk()).andExpect(jsonPath("$.valorMulta").value(0));
        assertEquals("disponivel",jdbc.queryForObject("SELECT status FROM exemplar WHERE id=5",String.class));
    }
    @Test void concorrenciaDeIsbnUmCadastroSomente() throws Exception {
        ExecutorService pool=Executors.newFixedThreadPool(2); CountDownLatch inicio=new CountDownLatch(1);
        Callable<Integer> criar=()->{ inicio.await(); return mvc.perform(post("/admin/livros").header("Authorization",admin)
            .contentType(MediaType.APPLICATION_JSON).content(livro())).andReturn().getResponse().getStatus(); };
        try {
            var a=pool.submit(criar); var b=pool.submit(criar); inicio.countDown();
            assertEquals(List.of(201,409),List.of(a.get(15,TimeUnit.SECONDS),b.get(15,TimeUnit.SECONDS)).stream().sorted().toList());
        } finally { pool.shutdownNow(); }
        assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM livro WHERE isbn='9781234567890'",Integer.class));
    }
    @Test void sprint1EdicaoDeUsuarioMantemId() throws Exception {
        mvc.perform(put("/admin/usuarios/4").header("Authorization",admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\"Carlos editado\",\"email\":\"carlos@biblioteca.com\",\"perfil\":\"atendente\",\"ativo\":true}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(4)).andExpect(jsonPath("$.nome").value("Carlos editado"));
    }
    @Test void sprint1InativacaoBloqueiaTokenAntigo() throws Exception {
        mvc.perform(put("/admin/usuarios/4").header("Authorization",admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\"Carlos\",\"email\":\"carlos@biblioteca.com\",\"perfil\":\"atendente\",\"ativo\":false}"))
            .andExpect(status().isOk());
        mvc.perform(get("/atendente/painel").header("Authorization",atendente)).andExpect(status().isUnauthorized());
    }
    @Test void sprint1CadastraLeitorAtivoSemExporSenha() throws Exception {
        mvc.perform(post("/atendente/leitores").header("Authorization",atendente).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\"Novo leitor\",\"email\":\"novo@teste.com\",\"senha\":\"password\",\"documento\":\"333\"}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.ativo").value(true))
            .andExpect(jsonPath("$.senha").doesNotExist()).andExpect(jsonPath("$.senhaHash").doesNotExist());
        assertEquals(3,jdbc.queryForObject("SELECT count(*) FROM leitor",Integer.class));
    }
    @Test void sprint1DocumentoDuplicadoNaoCriaUsuarioCT03() throws Exception {
        mvc.perform(post("/atendente/leitores").header("Authorization",atendente).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\"Novo leitor\",\"email\":\"novo@teste.com\",\"senha\":\"password\",\"documento\":\"111\"}"))
            .andExpect(status().isConflict());
        assertEquals(4,jdbc.queryForObject("SELECT count(*) FROM usuario",Integer.class));
        assertEquals(2,jdbc.queryForObject("SELECT count(*) FROM leitor",Integer.class));
    }
}
