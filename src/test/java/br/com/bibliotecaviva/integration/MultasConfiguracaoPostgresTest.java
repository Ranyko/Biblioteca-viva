package br.com.bibliotecaviva.integration;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

@SpringBootTest
@Import(MultasConfiguracaoPostgresTest.Relogio.class)
@EnabledIfEnvironmentVariable(named = "SPRINT2_TEST_DB_URL", matches = ".+")
@Sql("/sprint2-fixture.sql")
class MultasConfiguracaoPostgresTest {
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

    @BeforeEach void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        admin = "Bearer " + tokens.gerar(usuarios.findById(1L).orElseThrow());
        atendente = "Bearer " + tokens.gerar(usuarios.findById(4L).orElseThrow());
        leitor = "Bearer " + tokens.gerar(usuarios.findById(2L).orElseThrow());
    }

    private long gerarMulta() throws Exception {
        mvc.perform(post("/atendente/emprestimos/1/devolucao").header("Authorization", atendente))
            .andExpect(status().isOk()).andExpect(jsonPath("$.valorMulta").value(17.50));
        return jdbc.queryForObject("SELECT id FROM multa WHERE emprestimo_id = 1", Long.class);
    }

    private String configuracao(int prazo, int limite, String diaria, String teto, int reserva) {
        return "{\"prazoEmprestimoDias\":" + prazo + ",\"limiteEmprestimos\":" + limite
            + ",\"valorMultaDia\":" + diaria + ",\"valorMaximoMulta\":" + teto
            + ",\"prazoRetiradaReservaDias\":" + reserva + "}";
    }

    private void alterar(String json) throws Exception {
        mvc.perform(put("/admin/configuracao").header("Authorization", admin)
            .contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isOk());
    }

    private void segundaMultaQuitada() {
        jdbc.update("""
            INSERT INTO emprestimo(leitor_id, exemplar_id, atendente_retirada_id, atendente_devolucao_id,
                data_retirada, data_prevista_devolucao, data_devolucao)
            VALUES (2, 3, 4, 4, '2026-09-01T10:00:00-03:00', '2026-09-10', '2026-09-13T10:00:00-03:00')
            """);
        jdbc.update("""
            INSERT INTO multa(emprestimo_id, dias_atraso, valor_diario_aplicado, valor_maximo_aplicado, status, paga_em)
            VALUES (2, 3, 1, 100, 'quitada', '2026-09-13T11:00:00-03:00')
            """);
    }

    @Test void endpointsExigemAutenticacao() throws Exception {
        mvc.perform(get("/atendente/multas")).andExpect(status().isUnauthorized());
        mvc.perform(get("/atendente/multas/1")).andExpect(status().isUnauthorized());
        mvc.perform(post("/atendente/multas/1/baixa")).andExpect(status().isUnauthorized());
        mvc.perform(get("/admin/configuracao")).andExpect(status().isUnauthorized());
        mvc.perform(put("/admin/configuracao").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized());
    }

    @Test void leitorNaoAcessaMultasOperacionaisNemConfiguracao() throws Exception {
        mvc.perform(get("/atendente/multas").header("Authorization", leitor)).andExpect(status().isForbidden());
        mvc.perform(get("/atendente/multas/1").header("Authorization", leitor)).andExpect(status().isForbidden());
        mvc.perform(post("/atendente/multas/1/baixa").header("Authorization", leitor)).andExpect(status().isForbidden());
        mvc.perform(get("/admin/configuracao").header("Authorization", leitor)).andExpect(status().isForbidden());
        mvc.perform(put("/admin/configuracao").header("Authorization", leitor)
            .contentType(MediaType.APPLICATION_JSON).content(configuracao(7, 2, "1", "10", 3)))
            .andExpect(status().isForbidden());
    }

    @Test void atendenteNaoLeNemAlteraConfiguracao() throws Exception {
        mvc.perform(get("/admin/configuracao").header("Authorization", atendente)).andExpect(status().isForbidden());
        mvc.perform(put("/admin/configuracao").header("Authorization", atendente)
            .contentType(MediaType.APPLICATION_JSON).content(configuracao(7, 2, "1", "10", 3)))
            .andExpect(status().isForbidden());
    }

    @Test void tokenAnteriorAInativacaoNaoDaBaixa() throws Exception {
        long id = gerarMulta();
        jdbc.update("UPDATE usuario SET ativo = FALSE WHERE id = 4");
        mvc.perform(post("/atendente/multas/" + id + "/baixa").header("Authorization", atendente))
            .andExpect(status().isUnauthorized());
        assertEquals("pendente", jdbc.queryForObject("SELECT status FROM multa WHERE id = ?", String.class, id));
    }

    @Test void listaVaziaTemFormatoPaginado() throws Exception {
        mvc.perform(get("/atendente/multas").header("Authorization", atendente))
            .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(0))
            .andExpect(jsonPath("$.itens.length()").value(0)).andExpect(jsonPath("$.pagina").value(0));
    }

    @Test void consultaMostraValoresHistoricosELigacaoComLeitor() throws Exception {
        long id = gerarMulta();
        mvc.perform(get("/atendente/multas/" + id).header("Authorization", atendente))
            .andExpect(status().isOk()).andExpect(jsonPath("$.emprestimoId").value(1))
            .andExpect(jsonPath("$.leitorId").value(1)).andExpect(jsonPath("$.exemplarId").value(2))
            .andExpect(jsonPath("$.diasAtraso").value(7)).andExpect(jsonPath("$.valorDiarioAplicado").value(2.5))
            .andExpect(jsonPath("$.valorMaximoAplicado").value(25)).andExpect(jsonPath("$.valorTotal").value(17.5))
            .andExpect(jsonPath("$.status").value("pendente")).andExpect(jsonPath("$.pagaEm").isEmpty());
    }

    @Test void filtrosPodemSerCombinadosEPaginacaoTemOrdemEstavel() throws Exception {
        gerarMulta(); segundaMultaQuitada();
        mvc.perform(get("/atendente/multas").header("Authorization", atendente)
            .param("status", " PENDENTE ").param("leitorId", "1").param("emprestimoId", "1"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(1))
            .andExpect(jsonPath("$.itens[0].id").value(1));
        mvc.perform(get("/atendente/multas").header("Authorization", atendente).param("status", "quitada"))
            .andExpect(jsonPath("$.total").value(1)).andExpect(jsonPath("$.itens[0].id").value(2));
        mvc.perform(get("/atendente/multas").header("Authorization", atendente).param("leitorId", "2"))
            .andExpect(jsonPath("$.itens[0].emprestimoId").value(2));
        mvc.perform(get("/atendente/multas").header("Authorization", atendente).param("pagina", "1").param("tamanho", "1"))
            .andExpect(jsonPath("$.total").value(2)).andExpect(jsonPath("$.itens.length()").value(1))
            .andExpect(jsonPath("$.itens[0].id").value(1));
    }

    @Test void filtrosSemCorrespondenciaRetornamListaVazia() throws Exception {
        gerarMulta();
        mvc.perform(get("/atendente/multas").header("Authorization", atendente).param("leitorId", "999"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(0));
    }

    @Test void idsPaginacaoESituacaoInvalidosRetornam400() throws Exception {
        for (String id : List.of("0", "-1")) {
            mvc.perform(get("/atendente/multas/" + id).header("Authorization", atendente))
                .andExpect(status().isBadRequest());
            mvc.perform(post("/atendente/multas/" + id + "/baixa").header("Authorization", atendente))
                .andExpect(status().isBadRequest());
        }
        for (String[] filtro : List.of(new String[]{"leitorId", "0"}, new String[]{"emprestimoId", "-1"},
                new String[]{"pagina", "-1"}, new String[]{"tamanho", "0"}, new String[]{"tamanho", "101"},
                new String[]{"status", "pendente' OR 1=1 --"})) {
            mvc.perform(get("/atendente/multas").header("Authorization", atendente).param(filtro[0], filtro[1]))
                .andExpect(status().isBadRequest());
        }
    }

    @Test void multaAusenteRetorna404NaConsultaENaBaixa() throws Exception {
        mvc.perform(get("/atendente/multas/999").header("Authorization", atendente)).andExpect(status().isNotFound());
        mvc.perform(post("/atendente/multas/999/baixa").header("Authorization", atendente)).andExpect(status().isNotFound());
    }

    @Test void baixaRegistraDataDoServidorEPreservaValor() throws Exception {
        long id = gerarMulta();
        mvc.perform(post("/atendente/multas/" + id + "/baixa").header("Authorization", atendente)
            .contentType(MediaType.APPLICATION_JSON).content("{\"valorTotal\":0,\"pagaEm\":\"2099-01-01T00:00:00Z\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("quitada"))
            .andExpect(jsonPath("$.valorTotal").value(17.5));
        assertEquals(Instant.parse("2026-10-01T13:00:00Z"),
            jdbc.queryForObject("SELECT paga_em FROM multa WHERE id = ?", java.time.OffsetDateTime.class, id).toInstant());
    }

    @Test void administradorTambemPodeConsultarEBaixarMulta() throws Exception {
        long id = gerarMulta();
        mvc.perform(get("/atendente/multas/" + id).header("Authorization", admin)).andExpect(status().isOk());
        mvc.perform(post("/atendente/multas/" + id + "/baixa").header("Authorization", admin))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("quitada"));
    }

    @Test void segundaBaixaRetorna409SemAlterarDataOriginal() throws Exception {
        long id = gerarMulta();
        mvc.perform(post("/atendente/multas/" + id + "/baixa").header("Authorization", atendente)).andExpect(status().isOk());
        jdbc.update("UPDATE multa SET paga_em = '2026-09-30T12:00:00Z' WHERE id = ?", id);
        mvc.perform(post("/atendente/multas/" + id + "/baixa").header("Authorization", atendente)).andExpect(status().isConflict());
        assertEquals(Instant.parse("2026-09-30T12:00:00Z"),
            jdbc.queryForObject("SELECT paga_em FROM multa WHERE id = ?", java.time.OffsetDateTime.class, id).toInstant());
    }

    @Test void multaCanceladaNaoPodeReceberBaixa() throws Exception {
        long id = gerarMulta();
        jdbc.update("UPDATE multa SET status = 'cancelada' WHERE id = ?", id);
        mvc.perform(post("/atendente/multas/" + id + "/baixa").header("Authorization", atendente)).andExpect(status().isConflict());
        assertEquals("cancelada", jdbc.queryForObject("SELECT status FROM multa WHERE id = ?", String.class, id));
    }

    @Test void baixasSimultaneasRegistramUmUnicoPagamento() throws Exception {
        long id = gerarMulta();
        var pool = Executors.newFixedThreadPool(2);
        var inicio = new CountDownLatch(1);
        Callable<Integer> baixar = () -> {
            inicio.await();
            return mvc.perform(post("/atendente/multas/" + id + "/baixa").header("Authorization", atendente))
                .andReturn().getResponse().getStatus();
        };
        try {
            var a = pool.submit(baixar); var b = pool.submit(baixar); inicio.countDown();
            assertEquals(List.of(200, 409), List.of(a.get(20, TimeUnit.SECONDS), b.get(20, TimeUnit.SECONDS))
                .stream().sorted().toList());
        } finally { pool.shutdownNow(); }
        assertEquals("quitada", jdbc.queryForObject("SELECT status FROM multa WHERE id = ?", String.class, id));
    }

    @Test void consultaConfiguracaoInicial() throws Exception {
        mvc.perform(get("/admin/configuracao").header("Authorization", admin))
            .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.prazoEmprestimoDias").value(14)).andExpect(jsonPath("$.limiteEmprestimos").value(3))
            .andExpect(jsonPath("$.valorMultaDia").value(2.5)).andExpect(jsonPath("$.valorMaximoMulta").value(25))
            .andExpect(jsonPath("$.prazoRetiradaReservaDias").value(2));
    }

    @Test void alteraConfiguracaoCompletaComDataDoServidor() throws Exception {
        alterar(configuracao(7, 2, "1.25", "15.00", 5));
        mvc.perform(get("/admin/configuracao").header("Authorization", admin))
            .andExpect(jsonPath("$.prazoEmprestimoDias").value(7)).andExpect(jsonPath("$.limiteEmprestimos").value(2))
            .andExpect(jsonPath("$.valorMultaDia").value(1.25)).andExpect(jsonPath("$.valorMaximoMulta").value(15))
            .andExpect(jsonPath("$.prazoRetiradaReservaDias").value(5));
        assertEquals(Instant.parse("2026-10-01T13:00:00Z"), jdbc.queryForObject(
            "SELECT atualizado_em FROM configuracao_biblioteca WHERE id = 1", java.time.OffsetDateTime.class).toInstant());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{\"prazoEmprestimoDias\":0,\"limiteEmprestimos\":3,\"valorMultaDia\":2.5,\"valorMaximoMulta\":25,\"prazoRetiradaReservaDias\":2}",
        "{\"prazoEmprestimoDias\":32768,\"limiteEmprestimos\":3,\"valorMultaDia\":2.5,\"valorMaximoMulta\":25,\"prazoRetiradaReservaDias\":2}",
        "{\"prazoEmprestimoDias\":14,\"limiteEmprestimos\":0,\"valorMultaDia\":2.5,\"valorMaximoMulta\":25,\"prazoRetiradaReservaDias\":2}",
        "{\"prazoEmprestimoDias\":14,\"limiteEmprestimos\":3,\"valorMultaDia\":-1,\"valorMaximoMulta\":25,\"prazoRetiradaReservaDias\":2}",
        "{\"prazoEmprestimoDias\":14,\"limiteEmprestimos\":3,\"valorMultaDia\":2.501,\"valorMaximoMulta\":25,\"prazoRetiradaReservaDias\":2}",
        "{\"prazoEmprestimoDias\":14,\"limiteEmprestimos\":3,\"valorMultaDia\":2.5,\"valorMaximoMulta\":100000000,\"prazoRetiradaReservaDias\":2}",
        "{\"prazoEmprestimoDias\":14,\"limiteEmprestimos\":3,\"valorMultaDia\":2.5,\"valorMaximoMulta\":-1,\"prazoRetiradaReservaDias\":2}",
        "{\"prazoEmprestimoDias\":14,\"limiteEmprestimos\":3,\"valorMultaDia\":2.5,\"valorMaximoMulta\":25,\"prazoRetiradaReservaDias\":0}",
        "{\"prazoEmprestimoDias\":14}", "{}"
    })
    void valoresInvalidosRetornam400SemAlterarConfiguracao(String json) throws Exception {
        mvc.perform(put("/admin/configuracao").header("Authorization", admin)
            .contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.campos").isNotEmpty());
        assertEquals(14, jdbc.queryForObject("SELECT prazo_emprestimo_dias FROM configuracao_biblioteca", Integer.class));
        assertEquals("2.50", jdbc.queryForObject("SELECT valor_multa_dia FROM configuracao_biblioteca", java.math.BigDecimal.class).toPlainString());
    }

    @Test void jsonMalformadoRetorna400() throws Exception {
        mvc.perform(put("/admin/configuracao").header("Authorization", admin)
            .contentType(MediaType.APPLICATION_JSON).content("{errado")).andExpect(status().isBadRequest());
    }

    @Test void configuracaoAusenteRetorna404SemCriarSegundaLinha() throws Exception {
        jdbc.update("DELETE FROM configuracao_biblioteca");
        mvc.perform(get("/admin/configuracao").header("Authorization", admin)).andExpect(status().isNotFound());
        mvc.perform(put("/admin/configuracao").header("Authorization", admin)
            .contentType(MediaType.APPLICATION_JSON).content(configuracao(7, 2, "1", "10", 3)))
            .andExpect(status().isNotFound());
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM configuracao_biblioteca", Integer.class));
    }

    @Test void novasRegrasAfetamNovosEmprestimosSemAlterarPrazoAntigo() throws Exception {
        alterar(configuracao(7, 1, "1", "10", 3));
        mvc.perform(post("/atendente/emprestimos").header("Authorization", atendente)
            .contentType(MediaType.APPLICATION_JSON).content("{\"leitorId\":1,\"exemplarId\":1}"))
            .andExpect(status().isConflict());
        mvc.perform(post("/atendente/emprestimos").header("Authorization", atendente)
            .contentType(MediaType.APPLICATION_JSON).content("{\"leitorId\":2,\"exemplarId\":3}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.dataPrevistaDevolucao").value("2026-10-08"));
        assertEquals(java.time.LocalDate.parse("2026-09-24"), jdbc.queryForObject(
            "SELECT data_prevista_devolucao FROM emprestimo WHERE id = 1", java.time.LocalDate.class));
    }

    @Test void configuracaoNaoRecalculaMultaJaGerada() throws Exception {
        long id = gerarMulta();
        alterar(configuracao(7, 2, "9.00", "50.00", 3));
        mvc.perform(get("/atendente/multas/" + id).header("Authorization", atendente))
            .andExpect(jsonPath("$.valorDiarioAplicado").value(2.5)).andExpect(jsonPath("$.valorMaximoAplicado").value(25))
            .andExpect(jsonPath("$.valorTotal").value(17.5));
    }

    @Test void novaDevolucaoAplicaDiariaETetoAtualizados() throws Exception {
        alterar(configuracao(7, 2, "9.00", "50.00", 3));
        mvc.perform(post("/atendente/emprestimos/1/devolucao").header("Authorization", atendente))
            .andExpect(status().isOk()).andExpect(jsonPath("$.valorMulta").value(50));
        mvc.perform(get("/atendente/multas/1").header("Authorization", atendente))
            .andExpect(jsonPath("$.valorDiarioAplicado").value(9)).andExpect(jsonPath("$.valorMaximoAplicado").value(50));
    }

    @Test void multaZeroNaoCriaCobrancaNaDevolucao() throws Exception {
        alterar(configuracao(7, 2, "0", "25", 3));
        mvc.perform(post("/atendente/emprestimos/1/devolucao").header("Authorization", atendente))
            .andExpect(status().isOk()).andExpect(jsonPath("$.valorMulta").value(0));
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM multa", Integer.class));
    }

    @Test void novoPrazoDeRetiradaEAplicadoNaDevolucaoComFilaPreparada() throws Exception {
        alterar(configuracao(7, 2, "1", "10", 5));
        jdbc.update("INSERT INTO reserva(leitor_id, livro_id, status) VALUES (2, 1, 'ativa')");
        mvc.perform(post("/atendente/emprestimos/1/devolucao").header("Authorization", atendente))
            .andExpect(status().isOk()).andExpect(jsonPath("$.statusExemplar").value("reservado"));
        assertEquals(Instant.parse("2026-10-06T13:00:00Z"), jdbc.queryForObject(
            "SELECT data_limite_retirada FROM reserva WHERE id = 1", java.time.OffsetDateTime.class).toInstant());
    }

    @Test void atualizacoesSimultaneasNaoMisturamCamposDeRegrasDiferentes() throws Exception {
        var pool = Executors.newFixedThreadPool(2);
        var inicio = new CountDownLatch(1);
        String a = configuracao(7, 2, "1", "10", 3);
        String b = configuracao(21, 5, "4", "40", 6);
        java.util.function.Function<String, Callable<Integer>> atualizar = json -> () -> {
            inicio.await();
            return mvc.perform(put("/admin/configuracao").header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON).content(json)).andReturn().getResponse().getStatus();
        };
        try {
            var x = pool.submit(atualizar.apply(a)); var y = pool.submit(atualizar.apply(b)); inicio.countDown();
            assertEquals(200, x.get(20, TimeUnit.SECONDS)); assertEquals(200, y.get(20, TimeUnit.SECONDS));
        } finally { pool.shutdownNow(); }
        String regra = jdbc.queryForObject("""
            SELECT prazo_emprestimo_dias || '/' || limite_emprestimos || '/' || valor_multa_dia
                || '/' || valor_maximo_multa || '/' || prazo_retirada_reserva_dias FROM configuracao_biblioteca
            """, String.class);
        assertTrue(List.of("7/2/1.00/10.00/3", "21/5/4.00/40.00/6").contains(regra));
    }
}
