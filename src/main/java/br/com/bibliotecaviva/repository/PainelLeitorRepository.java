package br.com.bibliotecaviva.repository;

import br.com.bibliotecaviva.dto.EmprestimoLeitorResponse;
import br.com.bibliotecaviva.dto.MultaLeitorResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PainelLeitorRepository {

    private final JdbcTemplate jdbcTemplate;

    public PainelLeitorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<EmprestimoLeitorResponse> listarEmprestimos(Long leitorId) {
        String sql = """
                SELECT
                    e.id,
                    l.titulo AS livro_titulo,
                    e.data_retirada,
                    e.data_prevista_devolucao,
                    e.data_devolucao
                FROM emprestimo e
                JOIN exemplar ex ON ex.id = e.exemplar_id
                JOIN livro l ON l.id = ex.livro_id
                WHERE e.leitor_id = ?
                AND e.data_devolucao IS NULL
                ORDER BY e.data_prevista_devolucao ASC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            var dataDevolucao = rs.getObject(
                    "data_devolucao",
                    java.time.OffsetDateTime.class
            );

            var dataPrevista = rs.getDate("data_prevista_devolucao");

            String situacao = dataDevolucao == null
                    ? "ATIVO"
                    : "DEVOLVIDO";

            return new EmprestimoLeitorResponse(
                    rs.getLong("id"),
                    rs.getString("livro_titulo"),
                    rs.getObject(
                            "data_retirada",
                            java.time.OffsetDateTime.class
                    ),
                    dataPrevista == null ? null : dataPrevista.toLocalDate(),
                    dataDevolucao,
                    situacao
            );
        }, leitorId);
    }

    public List<MultaLeitorResponse> listarMultas(Long leitorId) {
        String sql = """
                SELECT
                    m.id,
                    m.valor_total,
                    m.status,
                    m.gerada_em,
                    m.paga_em
                FROM multa m
                JOIN emprestimo e ON e.id = m.emprestimo_id
                WHERE e.leitor_id = ?
                ORDER BY m.gerada_em DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new MultaLeitorResponse(
                        rs.getLong("id"),
                        rs.getBigDecimal("valor_total"),
                        rs.getString("status"),
                        rs.getObject(
                                "gerada_em",
                                java.time.OffsetDateTime.class
                        ),
                        rs.getObject(
                                "paga_em",
                                java.time.OffsetDateTime.class
                        )
                ), leitorId);
    }
}
