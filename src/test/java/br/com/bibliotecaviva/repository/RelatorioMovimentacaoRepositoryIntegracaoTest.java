package br.com.bibliotecaviva.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import br.com.bibliotecaviva.dto.MovimentacaoCsvResponse;

@SpringBootTest
@Transactional
@EnabledIfEnvironmentVariable(named = "RELATORIO_TEST_DB_URL", matches = ".+")
class RelatorioMovimentacaoRepositoryIntegracaoTest {

        private static final LocalDate INICIO = LocalDate.of(2026, 10, 1);

        private static final LocalDate FIM = LocalDate.of(2026, 10, 31);

        @Autowired
        private RelatorioMovimentacaoRepository repository;

        @Autowired
        private JdbcTemplate jdbcTemplate;

        private Long atendenteId;

        private String tituloAnterior;
        private String tituloInicio;
        private String tituloFim;
        private String tituloPosterior;

        @DynamicPropertySource
        static void configurarBancoDeTeste(
                        DynamicPropertyRegistry registry) {

                String url = System.getenv("RELATORIO_TEST_DB_URL");
                String usuario = System.getenv("RELATORIO_TEST_DB_USER");
                String senha = System.getenv("RELATORIO_TEST_DB_PASSWORD");

                if (url == null || !url.matches(
                                "jdbc:postgresql://[^/]+/[A-Za-z0-9_]+_sprint2_test(?:\\?.*)?")) {
                        throw new IllegalStateException(
                                        "O teste exige um banco exclusivo com sufixo _sprint2_test");
                }

                if (usuario == null || usuario.isBlank()
                                || senha == null || senha.isBlank()) {
                        throw new IllegalStateException(
                                        "As credenciais do banco de teste devem estar configuradas");
                }

                registry.add("spring.datasource.url", () -> url);
                registry.add("spring.datasource.username", () -> usuario);
                registry.add("spring.datasource.password", () -> senha);

                registry.add("spring.datasource.url", () -> url);
                registry.add(
                                "spring.datasource.username",
                                () -> System.getenv("RELATORIO_TEST_DB_USER"));
                registry.add(
                                "spring.datasource.password",
                                () -> System.getenv("RELATORIO_TEST_DB_PASSWORD"));
        }

        @BeforeEach
        void prepararDados() {
                atendenteId = inserirUsuario(
                                "Atendente de teste",
                                "atendente-" + UUID.randomUUID() + "@teste.local",
                                "atendente");

                tituloAnterior = inserirMovimentacao(
                                "Movimentacao anterior",
                                OffsetDateTime.parse("2026-09-30T23:59:59-03:00"));

                tituloInicio = inserirMovimentacao(
                                "Movimentacao inicial",
                                OffsetDateTime.parse("2026-10-01T00:00:00-03:00"));

                tituloFim = inserirMovimentacao(
                                "Movimentacao final",
                                OffsetDateTime.parse("2026-10-31T23:59:59-03:00"));

                tituloPosterior = inserirMovimentacao(
                                "Movimentacao posterior",
                                OffsetDateTime.parse("2026-11-01T00:00:00-03:00"));
        }

        @Test
        void deveFiltrarPorDataInicialEFinal() {
                var resultado = repository.buscarMovimentacoes(INICIO, FIM);

                assertThat(titulos(resultado))
                                .containsExactlyInAnyOrder(
                                                tituloInicio,
                                                tituloFim);
        }

        @Test
        void deveFiltrarSomentePorDataInicial() {
                var resultado = repository.buscarMovimentacoes(INICIO, null);

                assertThat(titulos(resultado))
                                .containsExactlyInAnyOrder(
                                                tituloInicio,
                                                tituloFim,
                                                tituloPosterior);
        }

        @Test
        void deveFiltrarSomentePorDataFinal() {
                var resultado = repository.buscarMovimentacoes(null, FIM);

                assertThat(titulos(resultado))
                                .containsExactlyInAnyOrder(
                                                tituloAnterior,
                                                tituloInicio,
                                                tituloFim);
        }

        @Test
        void deveRetornarTodasQuandoDatasNaoInformadas() {
                var resultado = repository.buscarMovimentacoes(null, null);

                assertThat(titulos(resultado))
                                .containsExactlyInAnyOrder(
                                                tituloAnterior,
                                                tituloInicio,
                                                tituloFim,
                                                tituloPosterior);
        }

        private Set<String> titulos(
                        java.util.List<MovimentacaoCsvResponse> movimentacoes) {

                Set<String> resultado = new HashSet<>();

                for (MovimentacaoCsvResponse movimentacao : movimentacoes) {
                        String titulo = movimentacao.tituloObra();

                        if (titulo.startsWith("TESTE-RELATORIO-")) {
                                resultado.add(titulo);
                        }
                }

                return resultado;
        }

        private Long inserirUsuario(
                        String nome,
                        String email,
                        String perfil) {

                return jdbcTemplate.queryForObject("""
                                INSERT INTO usuario (nome, email, senha_hash, perfil)
                                VALUES (?, ?, ?, ?)
                                RETURNING id
                                """,
                                Long.class,
                                nome,
                                email,
                                "hash-de-teste",
                                perfil);
        }

        private String inserirMovimentacao(
                        String descricao,
                        OffsetDateTime dataRetirada) {

                String identificador = UUID.randomUUID().toString();

                Long usuarioId = inserirUsuario(
                                "Leitor " + descricao,
                                "leitor-" + identificador + "@teste.local",
                                "leitor");

                Long leitorId = jdbcTemplate.queryForObject("""
                                INSERT INTO leitor (usuario_id, documento, telefone)
                                VALUES (?, ?, ?)
                                RETURNING id
                                """,
                                Long.class,
                                usuarioId,
                                identificador.substring(0, 26),
                                "11999999999");

                String titulo = "TESTE-RELATORIO-" + descricao + " " + identificador;

                String isbn = "978" + String.format(
                                "%010d",
                                Math.floorMod(
                                                System.nanoTime(),
                                                10_000_000_000L));

                Long livroId = jdbcTemplate.queryForObject("""
                                INSERT INTO livro (titulo, isbn, descricao, ativo)
                                VALUES (?, ?, ?, TRUE)
                                RETURNING id
                                """,
                                Long.class,
                                titulo,
                                isbn,
                                "Livro criado para teste de integração");

                Long exemplarId = jdbcTemplate.queryForObject("""
                                INSERT INTO exemplar (
                                    livro_id, codigo_tombo, estado_conservacao, status
                                )
                                VALUES (?, ?, ?, ?)
                                RETURNING id
                                """,
                                Long.class,
                                livroId,
                                "TOMBO-" + identificador,
                                "bom",
                                "emprestado");

                jdbcTemplate.update("""
                                INSERT INTO emprestimo (
                                    leitor_id,
                                    exemplar_id,
                                    atendente_retirada_id,
                                    data_retirada,
                                    data_prevista_devolucao
                                )
                                VALUES (?, ?, ?, ?, ?)
                                """,
                                leitorId,
                                exemplarId,
                                atendenteId,
                                dataRetirada,
                                dataRetirada.toLocalDate().plusDays(14));

                return titulo;
        }
}