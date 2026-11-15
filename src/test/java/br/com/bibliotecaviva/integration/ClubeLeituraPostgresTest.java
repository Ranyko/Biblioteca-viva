package br.com.bibliotecaviva.integration;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(CirculacaoPostgresTest.Relogio.class)
@EnabledIfEnvironmentVariable(named = "SPRINT2_TEST_DB_URL", matches = ".+")
@Sql("/sprint2-fixture.sql")
class ClubeLeituraPostgresTest {
    @DynamicPropertySource
    static void banco(DynamicPropertyRegistry registry) {
        CirculacaoPostgresTest.banco(registry);
    }

    @Autowired WebApplicationContext context;
    @Autowired JdbcTemplate jdbc;
    @Autowired UsuarioRepository usuarios;
    @Autowired TokenService tokens;
    MockMvc mvc;
    String admin;
    String atendente;
    String leitor;
    String outroLeitor;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        admin = token(1);
        leitor = token(2);
        outroLeitor = token(3);
        atendente = token(4);
    }

    private String token(long usuarioId) {
        return "Bearer " + tokens.gerar(usuarios.findById(usuarioId).orElseThrow());
    }

    private void criarClubeEEncontro(int capacidade) throws Exception {
        mvc.perform(post("/admin/clubes-leitura").header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Clube\",\"capacidadeMaxima\":" + capacidade
                    + ",\"categoriaIds\":[1]}"))
            .andExpect(status().isCreated());
        mvc.perform(post("/atendente/clubes-leitura/1/encontros").header("Authorization", atendente)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"livroId\":1,\"dataHora\":\"2026-10-02T10:00:00-03:00\","
                    + "\"localOuAcesso\":\"Sala de leitura\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.vagasDisponiveis").value(capacidade));
    }

    @Test
    void permissaoSeparadaPorPerfil() throws Exception {
        String conteudo = "{\"nome\":\"Clube\",\"capacidadeMaxima\":5}";
        mvc.perform(post("/admin/clubes-leitura").contentType(MediaType.APPLICATION_JSON).content(conteudo))
            .andExpect(status().isUnauthorized());
        mvc.perform(post("/admin/clubes-leitura").header("Authorization", leitor)
                .contentType(MediaType.APPLICATION_JSON).content(conteudo))
            .andExpect(status().isForbidden());
        mvc.perform(get("/leitor/clubes-leitura").header("Authorization", atendente))
            .andExpect(status().isForbidden());
    }

    @Test
    void controlaCapacidadeMantemHistoricoDeInscricaoECancelaEncontro() throws Exception {
        criarClubeEEncontro(1);
        mvc.perform(post("/leitor/encontros-clube/1/inscricao").header("Authorization", leitor))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("confirmada"));
        mvc.perform(post("/leitor/encontros-clube/1/inscricao").header("Authorization", outroLeitor))
            .andExpect(status().isConflict());
        mvc.perform(delete("/leitor/encontros-clube/1/inscricao").header("Authorization", leitor))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("cancelada"));
        mvc.perform(post("/leitor/encontros-clube/1/inscricao").header("Authorization", outroLeitor))
            .andExpect(status().isCreated());
        mvc.perform(get("/atendente/clubes-leitura/1/encontros/1/inscricoes")
                .header("Authorization", atendente))
            .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(2));
    }

    @Test
    void naoUltrapassaCapacidadeComInscricoesConcorrentes() throws Exception {
        criarClubeEEncontro(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch inicio = new CountDownLatch(1);
        try {
            var primeira = executor.submit(() -> inscreverQuandoLiberado(inicio, leitor));
            var segunda = executor.submit(() -> inscreverQuandoLiberado(inicio, outroLeitor));
            inicio.countDown();
            int statusPrimeira = primeira.get(10, TimeUnit.SECONDS);
            int statusSegunda = segunda.get(10, TimeUnit.SECONDS);

            assertTrue((statusPrimeira == 201 && statusSegunda == 409)
                || (statusPrimeira == 409 && statusSegunda == 201));
            assertEquals(1, jdbc.queryForObject("""
                SELECT count(*) FROM inscricao_encontro
                WHERE encontro_id = 1 AND status = 'confirmada'
                """, Integer.class));
        } finally {
            executor.shutdownNow();
        }
    }

    private int inscreverQuandoLiberado(CountDownLatch inicio, String token) throws Exception {
        inicio.await();
        return mvc.perform(post("/leitor/encontros-clube/1/inscricao").header("Authorization", token))
            .andReturn().getResponse().getStatus();
    }

    @Test
    void registraPresencaEConcluiEncontro() throws Exception {
        criarClubeEEncontro(2);
        mvc.perform(post("/leitor/encontros-clube/1/inscricao").header("Authorization", leitor))
            .andExpect(status().isCreated());
        mvc.perform(patch("/atendente/clubes-leitura/1/encontros/1/inscricoes/1/presenca")
                .header("Authorization", atendente).contentType(MediaType.APPLICATION_JSON)
                .content("{\"presente\":true}"))
            .andExpect(status().isConflict());

        jdbc.update("UPDATE encontro_clube SET data_hora = '2026-09-30T10:00:00-03:00' WHERE id = 1");
        mvc.perform(patch("/atendente/clubes-leitura/1/encontros/1/inscricoes/1/presenca")
                .header("Authorization", atendente).contentType(MediaType.APPLICATION_JSON)
                .content("{\"presente\":true}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.presente").value(true));
        mvc.perform(patch("/atendente/clubes-leitura/1/encontros/1/concluir").header("Authorization", atendente))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("concluido"));
        mvc.perform(get("/leitor/minhas-inscricoes").header("Authorization", leitor).param("historico", "true"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.itens[0].presente").value(true));
    }
}
