package br.com.bibliotecaviva.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import br.com.bibliotecaviva.dto.EmprestimosPorCategoriaResponse;
import br.com.bibliotecaviva.dto.ObraMaisEmprestadaResponse;

@Repository
public class PainelBibliotecaRepository {

    private final JdbcTemplate jdbcTemplate;

    public PainelBibliotecaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long contarEmprestimosAtivos() {
        Long total = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM emprestimo
                WHERE data_devolucao IS NULL
                """, Long.class);

        return total == null ? 0L : total;
    }

    public long contarEmprestimosAtrasados() {
        Long total = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM emprestimo
                WHERE data_devolucao IS NULL
                  AND data_prevista_devolucao < CURRENT_DATE
                """, Long.class);

        return total == null ? 0L : total;
    }

    public List<ObraMaisEmprestadaResponse> buscarRankingObras() {
        return jdbcTemplate.query("""
                SELECT
                    l.id AS livro_id,
                    l.titulo,
                    COUNT(e.id) AS total_emprestimos
                FROM emprestimo e
                JOIN exemplar ex ON ex.id = e.exemplar_id
                JOIN livro l ON l.id = ex.livro_id
                GROUP BY l.id, l.titulo
                ORDER BY COUNT(e.id) DESC, l.titulo ASC
                LIMIT 10
                """,
                (rs, rowNum) -> new ObraMaisEmprestadaResponse(
                        rs.getLong("livro_id"),
                        rs.getString("titulo"),
                        rs.getLong("total_emprestimos")));
    }

    public List<EmprestimosPorCategoriaResponse> buscarEmprestimosPorCategoria() {
        return jdbcTemplate.query("""
                SELECT
                    c.id AS categoria_id,
                    c.nome AS categoria,
                    COUNT(DISTINCT e.id) AS total_emprestimos
                FROM emprestimo e
                JOIN exemplar ex ON ex.id = e.exemplar_id
                JOIN livro_categoria lc ON lc.livro_id = ex.livro_id
                JOIN categoria c ON c.id = lc.categoria_id
                GROUP BY c.id, c.nome
                ORDER BY COUNT(DISTINCT e.id) DESC, c.nome ASC
                """,
                (rs, rowNum) -> new EmprestimosPorCategoriaResponse(
                        rs.getLong("categoria_id"),
                        rs.getString("categoria"),
                        rs.getLong("total_emprestimos")));
    }
}