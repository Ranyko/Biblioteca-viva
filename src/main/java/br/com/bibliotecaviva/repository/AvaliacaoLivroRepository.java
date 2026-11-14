package br.com.bibliotecaviva.repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import br.com.bibliotecaviva.dto.AvaliacaoResponse;
import br.com.bibliotecaviva.dto.AvaliacoesLivroResponse;

@Repository
public class AvaliacaoLivroRepository {
    private static final RowMapper<AvaliacaoResponse> AVALIACAO = (rs, rowNum) -> new AvaliacaoResponse(
            rs.getLong("id"),
            rs.getLong("livro_id"),
            rs.getInt("nota"),
            rs.getString("resenha"),
            rs.getObject("atualizada_em", OffsetDateTime.class));

    private final JdbcTemplate jdbc;

    public AvaliacaoLivroRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean livroAtivo(long livroId) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM livro WHERE id = ? AND ativo = TRUE)",
                Boolean.class, livroId));
    }

    public boolean temEmprestimoDevolvido(long livroId, long usuarioId) {
        return Boolean.TRUE.equals(jdbc.queryForObject("""
                SELECT EXISTS (
                    SELECT 1
                    FROM emprestimo em
                    JOIN exemplar e ON e.id = em.exemplar_id
                    JOIN leitor l ON l.id = em.leitor_id
                    WHERE e.livro_id = ?
                      AND l.usuario_id = ?
                      AND em.data_devolucao IS NOT NULL
                )
                """, Boolean.class, livroId, usuarioId));
    }

    public boolean existeAvaliacao(long livroId, long usuarioId) {
        return Boolean.TRUE.equals(jdbc.queryForObject("""
                SELECT EXISTS (
                    SELECT 1
                    FROM avaliacao_livro a
                    JOIN leitor l ON l.id = a.leitor_id
                    WHERE a.livro_id = ? AND l.usuario_id = ?
                )
                """, Boolean.class, livroId, usuarioId));
    }

    public Optional<AvaliacaoResponse> buscarPropria(long livroId, long usuarioId) {
        return jdbc.query("""
                SELECT a.id, a.livro_id, a.nota, a.resenha, a.atualizada_em
                FROM avaliacao_livro a
                JOIN leitor l ON l.id = a.leitor_id
                WHERE a.livro_id = ? AND l.usuario_id = ?
                """, AVALIACAO, livroId, usuarioId).stream().findFirst();
    }

    public AvaliacaoResponse criar(long livroId, long usuarioId, int nota, String resenha) {
        return jdbc.queryForObject("""
                INSERT INTO avaliacao_livro (livro_id, leitor_id, nota, resenha)
                SELECT ?, l.id, ?, ?
                FROM leitor l
                WHERE l.usuario_id = ?
                RETURNING id, livro_id, nota, resenha, atualizada_em
                """, AVALIACAO, livroId, nota, resenha, usuarioId);
    }

    public Optional<AvaliacaoResponse> atualizar(long livroId, long usuarioId, int nota, String resenha) {
        return jdbc.query("""
                UPDATE avaliacao_livro a
                SET nota = ?, resenha = ?, atualizada_em = CURRENT_TIMESTAMP
                FROM leitor l
                WHERE a.livro_id = ? AND a.leitor_id = l.id AND l.usuario_id = ?
                RETURNING a.id, a.livro_id, a.nota, a.resenha, a.atualizada_em
                """, AVALIACAO, nota, resenha, livroId, usuarioId).stream().findFirst();
    }

    public boolean excluir(long livroId, long usuarioId) {
        return jdbc.update("""
                DELETE FROM avaliacao_livro a
                USING leitor l
                WHERE a.livro_id = ? AND a.leitor_id = l.id AND l.usuario_id = ?
                """, livroId, usuarioId) == 1;
    }

    public AvaliacoesLivroResponse listar(long livroId, int pagina, int tamanho) {
        long total = jdbc.queryForObject(
                "SELECT count(*) FROM avaliacao_livro WHERE livro_id = ?", Long.class, livroId);
        BigDecimal media = jdbc.queryForObject("""
                SELECT COALESCE(ROUND(AVG(nota)::numeric, 2), 0.00)
                FROM avaliacao_livro
                WHERE livro_id = ?
                """, BigDecimal.class, livroId);
        List<AvaliacaoResponse> itens = jdbc.query("""
                SELECT id, livro_id, nota, resenha, atualizada_em
                FROM avaliacao_livro
                WHERE livro_id = ?
                ORDER BY atualizada_em DESC, id DESC
                LIMIT ? OFFSET ?
                """, AVALIACAO, livroId, tamanho, (long) pagina * tamanho);
        return new AvaliacoesLivroResponse(livroId, media, total, pagina, tamanho, itens);
    }
}
