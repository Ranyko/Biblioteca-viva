package br.com.bibliotecaviva.integration;

import java.time.*;
import java.util.List;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
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

/** Integração com PostgreSQL real, Flyway, transações e filtro JWT reais. Não usa H2. */
@SpringBootTest
@Import(CirculacaoPostgresTest.Relogio.class)
@EnabledIfEnvironmentVariable(named = "SPRINT2_TEST_DB_URL", matches = ".+")
@Sql("/sprint2-fixture.sql")
class CirculacaoPostgresTest {
    @DynamicPropertySource
    static void banco(DynamicPropertyRegistry registry) {
        String url = System.getenv("SPRINT2_TEST_DB_URL");
        if (url == null || !url.matches("jdbc:postgresql://[^/]+/[A-Za-z0-9_]+_sprint2_test(?:\\?.*)?"))
            throw new IllegalArgumentException("Use somente um banco descartável com nome terminado em _sprint2_test");
        registry.add("spring.datasource.url", () -> url);
        registry.add("spring.datasource.username", () -> System.getenv().getOrDefault("SPRINT2_TEST_DB_USER", "postgres"));
        registry.add("spring.datasource.password", () -> System.getenv().getOrDefault("SPRINT2_TEST_DB_PASSWORD", "postgres"));
    }
    @TestConfiguration
    static class Relogio {
        @Bean @Primary Clock relogioTeste() {
            return Clock.fixed(Instant.parse("2026-10-01T13:00:00Z"), ZoneId.of("America/Sao_Paulo"));
        }
    }
    @Autowired WebApplicationContext context;
    @Autowired JdbcTemplate jdbc;
    @Autowired UsuarioRepository usuarios;
    @Autowired TokenService tokens;
    MockMvc mvc;
    String atendente;
    String leitor;

    @BeforeEach void configurar() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        atendente = "Bearer " + tokens.gerar(usuarios.findById(4L).orElseThrow());
        leitor = "Bearer " + tokens.gerar(usuarios.findById(2L).orElseThrow());
    }
    private int retirar(long leitorId, long exemplarId) throws Exception {
        return mvc.perform(post("/atendente/emprestimos").header("Authorization", atendente)
            .contentType(MediaType.APPLICATION_JSON).content("{\"leitorId\":" + leitorId + ",\"exemplarId\":" + exemplarId + "}"))
            .andReturn().getResponse().getStatus();
    }
    @Test void pesquisaTituloAutorCategoriaSemMultiplicarContagem() throws Exception {
        mvc.perform(get("/acervo").param("titulo", "DOM").param("autor", "machado").param("categoria", "romance")
            .header("Authorization", leitor)).andExpect(status().isOk()).andExpect(jsonPath("$.total").value(1))
            .andExpect(jsonPath("$.itens[0].exemplaresDisponiveis").value(1))
            .andExpect(jsonPath("$.itens[0].categorias.length()").value(2));
    }
    @Test void mantemLivroSemExemplaresVisivel() throws Exception {
        mvc.perform(get("/acervo").param("titulo", "Sem Exemplares").header("Authorization", leitor))
            .andExpect(status().isOk()).andExpect(jsonPath("$.itens[0].exemplaresDisponiveis").value(0));
    }
    @Test void buscaNaoInterpretaCuringasNemSql() throws Exception {
        mvc.perform(get("/acervo").param("titulo", "%' OR 1=1 --").header("Authorization", leitor))
            .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(0));
    }
    @Test void livrosInativosNaoAparecem() throws Exception {
        jdbc.update("UPDATE livro SET ativo = FALSE WHERE id = 1");
        mvc.perform(get("/acervo").param("titulo", "Dom").header("Authorization", leitor))
            .andExpect(jsonPath("$.total").value(0));
    }
    @Test void paginacaoEstavel() throws Exception {
        mvc.perform(get("/acervo").param("pagina", "1").param("tamanho", "1").header("Authorization", leitor))
            .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(3))
            .andExpect(jsonPath("$.itens.length()").value(1)).andExpect(jsonPath("$.itens[0].id").value(1));
    }
    @Test void exigeAutenticacao() throws Exception {
        mvc.perform(get("/acervo")).andExpect(status().isUnauthorized());
        mvc.perform(post("/atendente/emprestimos")).andExpect(status().isUnauthorized());
    }
    @Test void leitorNaoEmprestaNemDevolveNemListaMovimentacoesAlheias() throws Exception {
        mvc.perform(post("/atendente/emprestimos").header("Authorization", leitor)
            .contentType(MediaType.APPLICATION_JSON).content("{\"leitorId\":1,\"exemplarId\":1}"))
            .andExpect(status().isForbidden());
        mvc.perform(post("/atendente/emprestimos/1/devolucao").header("Authorization", leitor)).andExpect(status().isForbidden());
        mvc.perform(get("/atendente/emprestimos").header("Authorization", leitor)).andExpect(status().isForbidden());
    }
    @Test void administradorTambemPodeEmprestar() throws Exception {
        String admin = "Bearer " + tokens.gerar(usuarios.findById(1L).orElseThrow());
        mvc.perform(post("/atendente/emprestimos").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"leitorId\":2,\"exemplarId\":1}")).andExpect(status().isCreated());
    }
    @Test void emprestimoRegistraDatasEAtendenteDoToken() throws Exception {
        mvc.perform(post("/atendente/emprestimos").header("Authorization", atendente).contentType(MediaType.APPLICATION_JSON)
            .content("{\"leitorId\":2,\"exemplarId\":1,\"atendenteId\":1,\"dataPrevistaDevolucao\":\"2099-01-01\"}"))
            .andExpect(status().isCreated()).andExpect(header().string("Location", "/atendente/emprestimos/2"))
            .andExpect(jsonPath("$.dataPrevistaDevolucao").value("2026-10-15"))
            .andExpect(jsonPath("$.atendenteRetiradaId").value(4));
        assertEquals("emprestado", jdbc.queryForObject("SELECT status FROM exemplar WHERE id = 1", String.class));
    }
    @Test void recusaCamposObrigatoriosEIdsInvalidos() throws Exception {
        mvc.perform(post("/atendente/emprestimos").header("Authorization", atendente).contentType(MediaType.APPLICATION_JSON)
            .content("{\"leitorId\":0}")).andExpect(status().isBadRequest());
        assertEquals(404, retirar(999, 1));
        assertEquals(404, retirar(2, 999));
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM emprestimo", Integer.class));
    }
    @Test void recusaInativoELimiteSemAlterarExemplar() throws Exception {
        jdbc.update("UPDATE usuario SET ativo = FALSE WHERE id = 3");
        assertEquals(409, retirar(2, 1));
        jdbc.update("UPDATE configuracao_biblioteca SET limite_emprestimos = 1");
        assertEquals(409, retirar(1, 1));
        assertEquals("disponivel", jdbc.queryForObject("SELECT status FROM exemplar WHERE id = 1", String.class));
    }
    @Test void recusaEmprestadoEInativo() throws Exception {
        assertEquals(409, retirar(2, 2));
        assertEquals(409, retirar(2, 4));
    }
    @Test void retiraReservaSomenteDoTitularDentroDoPrazo() throws Exception {
        jdbc.update("UPDATE exemplar SET status = 'reservado' WHERE id = 1");
        jdbc.update("INSERT INTO reserva(leitor_id, livro_id, exemplar_id, status, data_limite_retirada) VALUES (2, 1, 1, 'disponivel', '2026-10-02T10:00:00-03:00')");
        assertEquals(409, retirar(1, 1));
        assertEquals(201, retirar(2, 1));
        assertEquals("atendida", jdbc.queryForObject("SELECT status FROM reserva WHERE id = 1", String.class));
    }
    @Test void recusaReservaExpirada() throws Exception {
        jdbc.update("UPDATE exemplar SET status = 'reservado' WHERE id = 1");
        jdbc.update("INSERT INTO reserva(leitor_id, livro_id, exemplar_id, status, data_limite_retirada) VALUES (2, 1, 1, 'disponivel', '2026-10-01T10:00:00-03:00')");
        assertEquals(409, retirar(2, 1));
    }
    @Test void naoFuraFila() throws Exception {
        jdbc.update("INSERT INTO reserva(leitor_id, livro_id) VALUES (2, 1)");
        assertEquals(409, retirar(1, 1));
    }
    @Test void devolveNoPrazoENaoGeraMulta() throws Exception {
        jdbc.update("UPDATE emprestimo SET data_prevista_devolucao = '2026-10-01' WHERE id = 1");
        mvc.perform(post("/atendente/emprestimos/1/devolucao").header("Authorization", atendente))
            .andExpect(status().isOk()).andExpect(jsonPath("$.diasAtraso").value(0))
            .andExpect(jsonPath("$.valorMulta").value(0)).andExpect(jsonPath("$.statusExemplar").value("disponivel"));
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM multa", Integer.class));
    }
    @Test void devolveComAtrasoPreservaValoresHistoricosERecusaRepeticao() throws Exception {
        mvc.perform(post("/atendente/emprestimos/1/devolucao").header("Authorization", atendente))
            .andExpect(status().isOk()).andExpect(jsonPath("$.diasAtraso").value(7)).andExpect(jsonPath("$.valorMulta").value(17.5));
        jdbc.update("UPDATE configuracao_biblioteca SET valor_multa_dia = 10, valor_maximo_multa = 100");
        assertEquals("17.50", jdbc.queryForObject("SELECT valor_total FROM multa WHERE emprestimo_id = 1", String.class));
        mvc.perform(post("/atendente/emprestimos/1/devolucao").header("Authorization", atendente)).andExpect(status().isConflict());
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM multa", Integer.class));
    }
    @Test void encaminhaAoPrimeiroElegivelMantendoFila() throws Exception {
        jdbc.update("INSERT INTO reserva(leitor_id, livro_id, data_solicitacao) VALUES (1, 1, '2026-09-01T10:00:00Z'), (2, 1, '2026-09-02T10:00:00Z')");
        jdbc.update("UPDATE usuario SET ativo = FALSE WHERE id = 2");
        mvc.perform(post("/atendente/emprestimos/1/devolucao").header("Authorization", atendente))
            .andExpect(status().isOk()).andExpect(jsonPath("$.statusExemplar").value("reservado"))
            .andExpect(jsonPath("$.reservaDisponibilizadaId").value(2));
        assertEquals("ativa", jdbc.queryForObject("SELECT status FROM reserva WHERE id = 1", String.class));
        assertEquals(2L, jdbc.queryForObject("SELECT exemplar_id FROM reserva WHERE id = 2", Long.class));
    }
    @Test void falhaNaMultaReverteDevolucaoEStatus() throws Exception {
        jdbc.update("INSERT INTO multa(emprestimo_id,dias_atraso,valor_diario_aplicado,valor_maximo_aplicado) VALUES (1,1,2.50,25)");
        mvc.perform(post("/atendente/emprestimos/1/devolucao").header("Authorization", atendente)).andExpect(status().isConflict());
        assertNull(jdbc.queryForObject("SELECT data_devolucao FROM emprestimo WHERE id = 1", Object.class));
        assertEquals("emprestado", jdbc.queryForObject("SELECT status FROM exemplar WHERE id = 2", String.class));
    }
    private List<Integer> simultaneas(long leitorA, long exemplarA, long leitorB, long exemplarB) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch largada = new CountDownLatch(1);
        try {
            Future<Integer> a = pool.submit(() -> { largada.await(); return retirar(leitorA, exemplarA); });
            Future<Integer> b = pool.submit(() -> { largada.await(); return retirar(leitorB, exemplarB); });
            largada.countDown();
            return List.of(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS)).stream().sorted().toList();
        } finally { pool.shutdownNow(); }
    }
    @Test void duasRetiradasDoMesmoExemplarUmaApenasConsegue() throws Exception {
        assertEquals(List.of(201, 409), simultaneas(1, 1, 2, 1));
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM emprestimo WHERE exemplar_id = 1 AND data_devolucao IS NULL", Integer.class));
    }
    @Test void emprestimosSimultaneosDoMesmoLeitorNaoUltrapassamLimite() throws Exception {
        jdbc.update("UPDATE configuracao_biblioteca SET limite_emprestimos = 1");
        assertEquals(List.of(201, 409), simultaneas(2, 1, 2, 3));
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM emprestimo WHERE leitor_id = 2 AND data_devolucao IS NULL", Integer.class));
    }
    @Test void consultasNoVolumeDeReferenciaCorretas() throws Exception {
        jdbc.update("INSERT INTO livro(titulo,isbn) SELECT 'Volume ' || n, lpad((10000 + n)::text, 13, '0') FROM generate_series(1,997) n");
        jdbc.update("INSERT INTO exemplar(livro_id,codigo_tombo,estado_conservacao) SELECT 1, 'V' || n, 'bom' FROM generate_series(1,1996) n");
        jdbc.update("INSERT INTO usuario(nome,email,senha_hash,perfil) SELECT 'Leitor '||n, 'volume'||n||'@teste.com', 'hash-teste', 'leitor' FROM generate_series(1,498) n");
        jdbc.update("INSERT INTO leitor(usuario_id,documento) SELECT id, 'D'||id FROM usuario WHERE email LIKE 'volume%@teste.com'");
        for (String[] filtro : List.of(new String[]{"titulo", "dom"}, new String[]{"autor", "machado"}, new String[]{"categoria", "romance"})) {
            long inicio = System.nanoTime();
            mvc.perform(get("/acervo").param(filtro[0], filtro[1]).header("Authorization", leitor))
                .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.itens[0].exemplaresDisponiveis").value(1997));
            long millis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inicio);
            System.out.println("H6 volume 1000/2000/500, " + filtro[0] + ": " + millis + " ms (API local, não inclui navegador/rede)");
            assertTrue(millis <= 3000, "Consulta excedeu 3s na API local: " + millis + " ms");
        }
    }
}
