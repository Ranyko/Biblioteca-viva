package br.com.bibliotecaviva.integration;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Integração do módulo de avisos com PostgreSQL real, Flyway e filtro JWT reais (doc §9). */
@SpringBootTest
@Import(AvisoPostgresTest.Relogio.class)
@EnabledIfEnvironmentVariable(named = "SPRINT2_TEST_DB_URL", matches = ".+")
@Sql("/sprint2-fixture.sql")
class AvisoPostgresTest {

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
    String admin;
    String atendente;
    String leitor;

    @BeforeEach void configurar() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        admin = "Bearer " + tokens.gerar(usuarios.findById(1L).orElseThrow());
        atendente = "Bearer " + tokens.gerar(usuarios.findById(4L).orElseThrow());
        leitor = "Bearer " + tokens.gerar(usuarios.findById(2L).orElseThrow());
    }

    // --- Autorização (doc §9) -------------------------------------------------

    @Test void naoAutenticadoNaContagemRetorna401() throws Exception {
        mvc.perform(get("/avisos/nao-lidos/contagem")).andExpect(status().isUnauthorized());
    }

    @Test void atendenteNaCentralDoLeitorRetorna403() throws Exception {
        mvc.perform(get("/leitor/avisos").header("Authorization", atendente)).andExpect(status().isForbidden());
    }

    @Test void leitorNaVarreduraDoAdminRetorna403() throws Exception {
        mvc.perform(post("/admin/avisos/varredura").header("Authorization", leitor)).andExpect(status().isForbidden());
    }

    @Test void atendenteNasPreferenciasDoAdminRetorna403() throws Exception {
        mvc.perform(get("/admin/avisos/preferencias").header("Authorization", atendente)).andExpect(status().isForbidden());
    }

    @Test void adminLePreferenciasRetorna200() throws Exception {
        mvc.perform(get("/admin/avisos/preferencias").header("Authorization", admin))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.filaPosicao").value(false))
            .andExpect(jsonPath("$.devidaAmanha").value(true));
    }

    // --- Preferências ---------------------------------------------------------

    @Test void adminAtualizaPreferenciasELeDeVolta() throws Exception {
        mvc.perform(put("/admin/avisos/preferencias").header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"devidaAmanha":false,"vencido":true,"reservaDisponivel":true,"reservaExpirando":true,
                     "reservaExpirada":true,"multaGerada":true,"filaPosicao":true}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.filaPosicao").value(true))
            .andExpect(jsonPath("$.devidaAmanha").value(false));
        assertEquals(false, jdbc.queryForObject("SELECT devida_amanha FROM aviso_preferencia WHERE id = 1", Boolean.class));
    }

    @Test void preferenciasInvalidasRetornam400() throws Exception {
        mvc.perform(put("/admin/avisos/preferencias").header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"devidaAmanha\":true}"))
            .andExpect(status().isBadRequest());
    }

    // --- Varredura idempotente (doc §9) --------------------------------------

    @Test void varreduraDuasVezesNoMesmoDiaEmiteUmaVez() throws Exception {
        // Fixture: empréstimo do leitor 1 vence em 2026-09-24 (< hoje 2026-10-01) → VENCIDO.
        mvc.perform(post("/admin/avisos/varredura").header("Authorization", admin))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.vencido").value(1));
        mvc.perform(post("/admin/avisos/varredura").header("Authorization", admin))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.vencido").value(0));
        assertEquals(1, jdbc.queryForObject(
                "SELECT count(*) FROM aviso WHERE tipo = 'VENCIDO'", Integer.class));
    }

    // --- Isolamento do leitor (doc §9) ---------------------------------------

    @Test void leitorMarcaAvisoDeOutroRetorna404() throws Exception {
        jdbc.update("""
            INSERT INTO aviso (usuario_id, tipo, titulo, mensagem, referencia_tipo, referencia_id, dia_referencia, criado_em)
            VALUES (3, 'VENCIDO', 't', 'm', 'emprestimo', 999, '2026-10-01', '2026-10-01T10:00:00-03:00')
            """);
        long idDeOutro = jdbc.queryForObject("SELECT id FROM aviso", Long.class);
        mvc.perform(patch("/leitor/avisos/" + idDeOutro + "/leitura").header("Authorization", leitor))
            .andExpect(status().isNotFound());
    }

    @Test void leituraTodosNaoAfetaAvisosDeOutroUsuario() throws Exception {
        jdbc.update("""
            INSERT INTO aviso (usuario_id, tipo, titulo, mensagem, referencia_tipo, referencia_id, dia_referencia, criado_em)
            VALUES (2, 'VENCIDO', 't', 'm', 'emprestimo', 1, '2026-10-01', '2026-10-01T10:00:00-03:00'),
                   (3, 'VENCIDO', 't', 'm', 'emprestimo', 2, '2026-10-01', '2026-10-01T10:00:00-03:00')
            """);
        mvc.perform(post("/leitor/avisos/leitura-todos").header("Authorization", leitor))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.atualizados").value(1));
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM aviso WHERE usuario_id = 3 AND lido_em IS NULL",
                Integer.class));
    }

    @Test void leitorListaPropriosAvisos() throws Exception {
        jdbc.update("""
            INSERT INTO aviso (usuario_id, tipo, titulo, mensagem, referencia_tipo, referencia_id, dia_referencia, criado_em)
            VALUES (2, 'VENCIDO', 't', 'm', 'emprestimo', 1, '2026-10-01', '2026-10-01T10:00:00-03:00'),
                   (3, 'VENCIDO', 't', 'm', 'emprestimo', 2, '2026-10-01', '2026-10-01T10:00:00-03:00')
            """);
        mvc.perform(get("/leitor/avisos").header("Authorization", leitor))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(1))
            .andExpect(jsonPath("$.itens.length()").value(1));
    }

    @Test void paginacaoInvalidaRetorna400() throws Exception {
        mvc.perform(get("/leitor/avisos?pagina=-1").header("Authorization", leitor))
            .andExpect(status().isBadRequest());
    }

    @Test void paginaForaDoIntervaloMantemTotal() throws Exception {
        // R1: com COUNT(*) OVER() o total sumia (0) quando o OFFSET passava do fim. Aqui deve vir 1.
        jdbc.update("""
            INSERT INTO aviso (usuario_id, tipo, titulo, mensagem, referencia_tipo, referencia_id, dia_referencia, criado_em)
            VALUES (2, 'VENCIDO', 't', 'm', 'emprestimo', 1, '2026-10-01', '2026-10-01T10:00:00-03:00')
            """);
        mvc.perform(get("/leitor/avisos?pagina=5&tamanho=20").header("Authorization", leitor))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.itens.length()").value(0))
            .andExpect(jsonPath("$.total").value(1));
    }

    @Test void filtroLidoSeparaLidosDeNaoLidos() throws Exception {
        jdbc.update("""
            INSERT INTO aviso (usuario_id, tipo, titulo, mensagem, referencia_tipo, referencia_id, dia_referencia, criado_em, lido_em)
            VALUES (2, 'VENCIDO', 't', 'm', 'emprestimo', 1, '2026-10-01', '2026-10-01T10:00:00-03:00', '2026-10-01T11:00:00-03:00'),
                   (2, 'VENCIDO', 't', 'm', 'emprestimo', 2, '2026-10-01', '2026-10-01T10:00:00-03:00', NULL)
            """);
        mvc.perform(get("/leitor/avisos?lido=false").header("Authorization", leitor))
            .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(1));
        mvc.perform(get("/leitor/avisos?lido=true").header("Authorization", leitor))
            .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(1));
        mvc.perform(get("/leitor/avisos").header("Authorization", leitor))
            .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(2));
    }

    @Test void atendenteVePendenciasComAtrasadosEReservasExpirando() throws Exception {
        // Fixture: empréstimo do leitor 1 vence 2026-09-24 (atrasado). Reserva disponível com prazo amanhã.
        jdbc.update("""
            INSERT INTO reserva (leitor_id, livro_id, exemplar_id, status, data_limite_retirada)
            VALUES (2, 2, 3, 'disponivel', '2026-10-02T10:00:00-03:00')
            """);
        mvc.perform(get("/atendente/avisos/pendencias").header("Authorization", atendente))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.atrasados.length()").value(1))
            .andExpect(jsonPath("$.reservasExpirando.length()").value(1));
    }
}