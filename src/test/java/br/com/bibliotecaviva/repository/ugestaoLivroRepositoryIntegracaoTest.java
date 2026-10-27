package br.com.bibliotecaviva.repository;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import br.com.bibliotecaviva.dto.SugestaoLivroResponse;

@SpringBootTest
class SugestaoLivroRepositoryIntegracaoTest {

    @Autowired
    private SugestaoLivroRepository repository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void naoDeveRecomendarLivroJaEmprestadoPeloLeitor() {
        // No seed inicial, o leitor 1 já possui empréstimo do livro 1.
        List<SugestaoLivroResponse> sugestoes = repository.buscarSugestoes(1L);

        assertThat(sugestoes)
                .extracting(SugestaoLivroResponse::livroId)
                .doesNotContain(1L);
    }

    @Test
    void devePriorizarLivrosComExemplaresDisponiveis() {
        List<SugestaoLivroResponse> sugestoes = repository.buscarSugestoes(1L);

        assertThat(sugestoes).isNotEmpty();

        List<SugestaoLivroResponse> comDisponibilidade = sugestoes.stream()
                .filter(s -> s.exemplaresDisponiveis() > 0)
                .toList();

        List<SugestaoLivroResponse> semDisponibilidade = sugestoes.stream()
                .filter(s -> s.exemplaresDisponiveis() == 0)
                .toList();

        if (!comDisponibilidade.isEmpty() && !semDisponibilidade.isEmpty()) {
            int ultimaDisponivel = sugestoes.lastIndexOf(
                    comDisponibilidade.get(comDisponibilidade.size() - 1));

            int primeiraIndisponivel = sugestoes.indexOf(
                    semDisponibilidade.get(0));

            assertThat(ultimaDisponivel)
                    .isLessThan(primeiraIndisponivel);
        }
    }

    @Test
    void naoDeveRetornarLivrosDuplicados() {
        List<SugestaoLivroResponse> sugestoes = repository.buscarSugestoes(1L);

        List<Long> ids = sugestoes.stream()
                .map(SugestaoLivroResponse::livroId)
                .toList();

        Set<Long> idsUnicos = ids.stream()
                .collect(Collectors.toSet());

        assertThat(idsUnicos).hasSameSizeAs(ids);
    }

    @Test
    @Transactional
    void devePriorizarCategoriaMaisFrequenteNoHistorico() {
        // Arrange: cria um leitor e um atendente exclusivos para este teste.
        String identificador = UUID.randomUUID().toString();

        Long usuarioLeitorId = inserirUsuario(
                "Leitor de teste",
                "leitor-" + identificador + "@teste.local",
                "leitor");

        Long leitorId = jdbcTemplate.queryForObject("""
                INSERT INTO leitor (usuario_id, documento, telefone)
                VALUES (?, ?, ?)
                RETURNING id
                """,
                Long.class,
                usuarioLeitorId,
                identificador.substring(0, 26),
                "11999999999");

        Long atendenteId = inserirUsuario(
                "Atendente de teste",
                "atendente-" + identificador + "@teste.local",
                "atendente");

        Long fantasiaId = inserirCategoria("Fantasia " + identificador);
        Long historiaId = inserirCategoria("Historia " + identificador);

        // O leitor possui três empréstimos de livros de Fantasia.
        for (int i = 1; i <= 3; i++) {
            Long livroId = inserirLivro(
                    "Livro de Fantasia " + i + " " + identificador);

            associarCategoria(livroId, fantasiaId);

            Long exemplarId = inserirExemplar(livroId, "emprestado");

            registrarEmprestimo(leitorId, exemplarId, atendenteId);
        }

        // O leitor possui apenas um empréstimo de História.
        Long livroHistoricoId = inserirLivro(
                "Livro de Historia emprestado " + identificador);

        associarCategoria(livroHistoricoId, historiaId);

        Long exemplarHistoricoId = inserirExemplar(livroHistoricoId, "emprestado");

        registrarEmprestimo(
                leitorId, exemplarHistoricoId, atendenteId);

        // Dois candidatos inéditos, ambos com exemplar disponível.
        Long candidatoFantasiaId = inserirLivro(
                "Nova Fantasia " + identificador);

        associarCategoria(candidatoFantasiaId, fantasiaId);
        inserirExemplar(candidatoFantasiaId, "disponivel");

        Long candidatoHistoriaId = inserirLivro(
                "Nova Historia " + identificador);

        associarCategoria(candidatoHistoriaId, historiaId);
        inserirExemplar(candidatoHistoriaId, "disponivel");

        // Act: executa a consulta real no PostgreSQL.
        var sugestoes = repository.buscarSugestoes(leitorId);

        var ids = sugestoes.stream()
                .map(SugestaoLivroResponse::livroId)
                .toList();

        // Assert: ambos são recomendados, mas Fantasia vem primeiro.
        assertThat(ids)
                .contains(candidatoFantasiaId, candidatoHistoriaId);

        assertThat(ids.indexOf(candidatoFantasiaId))
                .isLessThan(ids.indexOf(candidatoHistoriaId));
    }

    private Long inserirUsuario(
            String nome, String email, String perfil) {
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

    private Long inserirCategoria(String nome) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO categoria (nome)
                VALUES (?)
                RETURNING id
                """,
                Long.class,
                nome);
    }

    private Long inserirLivro(String titulo) {
        String isbn = "978" + String.format(
                "%010d",
                Math.floorMod(
                        System.nanoTime(), 10_000_000_000L));

        return jdbcTemplate.queryForObject("""
                INSERT INTO livro (titulo, isbn, descricao, ativo)
                VALUES (?, ?, ?, TRUE)
                RETURNING id
                """,
                Long.class,
                titulo,
                isbn,
                "Livro criado para teste de integração");
    }

    private void associarCategoria(Long livroId, Long categoriaId) {
        jdbcTemplate.update("""
                INSERT INTO livro_categoria (livro_id, categoria_id)
                VALUES (?, ?)
                """,
                livroId,
                categoriaId);
    }

    private Long inserirExemplar(Long livroId, String status) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO exemplar (
                    livro_id, codigo_tombo, estado_conservacao, status
                )
                VALUES (?, ?, ?, ?)
                RETURNING id
                """,
                Long.class,
                livroId,
                "TOMBO-" + UUID.randomUUID(),
                "bom",
                status);
    }

    private void registrarEmprestimo(
            Long leitorId, Long exemplarId, Long atendenteId) {
        jdbcTemplate.update("""
                INSERT INTO emprestimo (
                    leitor_id,
                    exemplar_id,
                    atendente_retirada_id,
                    data_prevista_devolucao
                )
                VALUES (?, ?, ?, CURRENT_DATE + 14)
                """,
                leitorId,
                exemplarId,
                atendenteId);
    }
}