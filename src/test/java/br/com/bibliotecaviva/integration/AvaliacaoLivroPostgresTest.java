package br.com.bibliotecaviva.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "SPRINT2_TEST_DB_URL", matches = ".+")
@Sql("/sprint2-fixture.sql")
class AvaliacaoLivroPostgresTest {
    @DynamicPropertySource
    static void banco(DynamicPropertyRegistry registry) {
        CirculacaoPostgresTest.banco(registry);
    }

    @Autowired WebApplicationContext context;
    @Autowired JdbcTemplate jdbc;
    @Autowired UsuarioRepository usuarios;
    @Autowired TokenService tokens;

    private MockMvc mvc;
    private String leitor;
    private String outroLeitor;
    private String atendente;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        leitor = token(2);
        outroLeitor = token(3);
        atendente = token(4);
    }

    private String token(long usuarioId) {
        return "Bearer " + tokens.gerar(usuarios.findById(usuarioId).orElseThrow());
    }

    private void registrarEmprestimoDevolvido(long leitorId, long exemplarId) {
        jdbc.update("""
                INSERT INTO emprestimo (
                    leitor_id, exemplar_id, atendente_retirada_id, atendente_devolucao_id,
                    data_retirada, data_prevista_devolucao, data_devolucao
                )
                VALUES (?, ?, 4, 4, CURRENT_TIMESTAMP - INTERVAL '10 days',
                    CURRENT_DATE - 3, CURRENT_TIMESTAMP - INTERVAL '2 days')
                """, leitorId, exemplarId);
    }

    @Test
    void criaEConsultaAvaliacaoSemExporIdentidadeDoLeitor() throws Exception {
        registrarEmprestimoDevolvido(1, 2);

        mvc.perform(post("/leitor/livros/1/avaliacao").header("Authorization", leitor)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nota\":5,\"resenha\":\"  Leitura envolvente.  \"}"))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/leitor/livros/1/avaliacao"))
            .andExpect(jsonPath("$.nota").value(5))
            .andExpect(jsonPath("$.resenha").value("Leitura envolvente."))
            .andExpect(jsonPath("$.leitorId").doesNotExist());

        mvc.perform(get("/acervo/livros/1/avaliacoes").header("Authorization", atendente))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.media").value(5.00))
            .andExpect(jsonPath("$.total").value(1))
            .andExpect(jsonPath("$.itens[0].resenha").value("Leitura envolvente."))
            .andExpect(jsonPath("$.itens[0].leitorId").doesNotExist());
        mvc.perform(get("/leitor/livros/1/avaliacao").header("Authorization", leitor))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nota").value(5));
    }

    @Test
    void exigeDevolucaoEImpedeDuplicidade() throws Exception {
        mvc.perform(post("/leitor/livros/1/avaliacao").header("Authorization", leitor)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nota\":4}"))
            .andExpect(status().isConflict());

        registrarEmprestimoDevolvido(1, 2);
        mvc.perform(post("/leitor/livros/1/avaliacao").header("Authorization", leitor)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nota\":4}"))
            .andExpect(status().isCreated());
        mvc.perform(post("/leitor/livros/1/avaliacao").header("Authorization", leitor)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nota\":3}"))
            .andExpect(status().isConflict());

        registrarEmprestimoDevolvido(2, 1);
        mvc.perform(post("/leitor/livros/1/avaliacao").header("Authorization", outroLeitor)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nota\":3}"))
            .andExpect(status().isCreated());
        mvc.perform(get("/acervo/livros/1/avaliacoes").header("Authorization", leitor))
            .andExpect(jsonPath("$.total").value(2))
            .andExpect(jsonPath("$.media").value(3.5));
    }

    @Test
    void atualizaEExcluiAvaliacaoAtualizandoResumo() throws Exception {
        registrarEmprestimoDevolvido(1, 2);
        mvc.perform(post("/leitor/livros/1/avaliacao").header("Authorization", leitor)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nota\":5,\"resenha\":\"Inicial\"}"))
            .andExpect(status().isCreated());

        mvc.perform(put("/leitor/livros/1/avaliacao").header("Authorization", leitor)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nota\":2,\"resenha\":\"   \"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nota").value(2))
            .andExpect(jsonPath("$.resenha").doesNotExist());
        mvc.perform(get("/acervo/livros/1/avaliacoes").header("Authorization", leitor))
            .andExpect(jsonPath("$.media").value(2.00));

        mvc.perform(delete("/leitor/livros/1/avaliacao").header("Authorization", leitor))
            .andExpect(status().isNoContent());
        mvc.perform(get("/acervo/livros/1/avaliacoes").header("Authorization", leitor))
            .andExpect(jsonPath("$.media").value(0.00))
            .andExpect(jsonPath("$.total").value(0))
            .andExpect(jsonPath("$.itens.length()").value(0));
    }

    @Test
    void validaEntradaPaginacaoPerfilELivroAtivo() throws Exception {
        for (int nota : new int[] {0, 6}) {
            mvc.perform(post("/leitor/livros/1/avaliacao").header("Authorization", leitor)
                    .contentType(MediaType.APPLICATION_JSON).content("{\"nota\":" + nota + "}"))
                .andExpect(status().isBadRequest());
        }
        mvc.perform(post("/leitor/livros/1/avaliacao").header("Authorization", leitor)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nota\":4,\"resenha\":\"" + "x".repeat(1001) + "\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/leitor/livros/1/avaliacao").header("Authorization", atendente)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nota\":4}"))
            .andExpect(status().isForbidden());
        mvc.perform(get("/acervo/livros/1/avaliacoes").param("pagina", "-1").header("Authorization", leitor))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/acervo/livros/1/avaliacoes").param("tamanho", "101").header("Authorization", leitor))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/acervo/livros/999/avaliacoes").header("Authorization", leitor))
            .andExpect(status().isNotFound());

        jdbc.update("UPDATE livro SET ativo = FALSE WHERE id = 1");
        mvc.perform(get("/acervo/livros/1/avaliacoes").header("Authorization", leitor))
            .andExpect(status().isNotFound());
    }
}
