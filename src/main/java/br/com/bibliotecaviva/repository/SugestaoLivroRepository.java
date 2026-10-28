package br.com.bibliotecaviva.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import br.com.bibliotecaviva.dto.SugestaoLivroResponse;

@Repository
public class SugestaoLivroRepository {

    private final JdbcTemplate jdbcTemplate;

    public SugestaoLivroRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<SugestaoLivroResponse> buscarSugestoes(Long leitorId) {
        String sql = """
                WITH categorias_leitor AS (
                    SELECT
                        lc.categoria_id,
                        COUNT(*) AS ocorrencias
                    FROM emprestimo e
                    JOIN exemplar ex
                        ON ex.id = e.exemplar_id
                    JOIN livro_categoria lc
                        ON lc.livro_id = ex.livro_id
                    WHERE e.leitor_id = ?
                    GROUP BY lc.categoria_id
                ),
                livros_candidatos AS (
                    SELECT
                        l.id AS livro_id,
                        l.titulo,
                        l.descricao,
                        COUNT(DISTINCT lc.categoria_id) AS categorias_correspondentes,
                        MAX(cl.ocorrencias) AS relevancia,
                        COUNT(DISTINCT ex.id) FILTER (
                            WHERE ex.status = 'disponivel'
                        ) AS exemplares_disponiveis
                    FROM livro l
                    JOIN livro_categoria lc
                        ON lc.livro_id = l.id
                    JOIN categorias_leitor cl
                        ON cl.categoria_id = lc.categoria_id
                    LEFT JOIN exemplar ex
                        ON ex.livro_id = l.id
                    WHERE l.ativo = TRUE
                      AND NOT EXISTS (
                          SELECT 1
                          FROM emprestimo anterior
                          JOIN exemplar ex_anterior
                              ON ex_anterior.id = anterior.exemplar_id
                          WHERE anterior.leitor_id = ?
                            AND ex_anterior.livro_id = l.id
                      )
                    GROUP BY l.id, l.titulo, l.descricao
                )
                SELECT
                    lc.livro_id,
                    lc.titulo,
                    lc.descricao,
                    (
                        SELECT c.nome
                        FROM livro_categoria livro_cat
                        JOIN categoria c
                            ON c.id = livro_cat.categoria_id
                        JOIN categorias_leitor cl
                            ON cl.categoria_id = livro_cat.categoria_id
                        WHERE livro_cat.livro_id = lc.livro_id
                        ORDER BY cl.ocorrencias DESC, c.nome ASC
                        LIMIT 1
                    ) AS categoria,
                    lc.exemplares_disponiveis
                FROM livros_candidatos lc
                ORDER BY
                    (lc.exemplares_disponiveis > 0) DESC,
                    lc.relevancia DESC,
                    lc.categorias_correspondentes DESC,
                    lc.exemplares_disponiveis DESC,
                    lc.titulo ASC
                LIMIT 10
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new SugestaoLivroResponse(
                        rs.getLong("livro_id"),
                        rs.getString("titulo"),
                        rs.getString("descricao"),
                        rs.getString("categoria"),
                        rs.getLong("exemplares_disponiveis")),
                leitorId,
                leitorId);
    }
}