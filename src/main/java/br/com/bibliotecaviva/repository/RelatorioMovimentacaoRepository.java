package br.com.bibliotecaviva.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import br.com.bibliotecaviva.dto.MovimentacaoCsvResponse;

@Repository
public class RelatorioMovimentacaoRepository {

    private final JdbcTemplate jdbcTemplate;

    public RelatorioMovimentacaoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<MovimentacaoCsvResponse> buscarMovimentacoes(
            LocalDate dataInicio,
            LocalDate dataFim) {

        String sql = """
                SELECT
                    l.titulo AS titulo_obra,
                    u.nome AS nome_leitor,
                    e.data_retirada,
                    e.data_prevista_devolucao,
                    e.data_devolucao
                FROM emprestimo e
                JOIN leitor le ON le.id = e.leitor_id
                JOIN usuario u ON u.id = le.usuario_id
                JOIN exemplar ex ON ex.id = e.exemplar_id
                JOIN livro l ON l.id = ex.livro_id
                WHERE (? IS NULL OR e.data_retirada >= CAST(? AS DATE))
                  AND (? IS NULL OR e.data_retirada < CAST(? AS DATE) + INTERVAL '1 day')
                ORDER BY e.data_retirada DESC, e.id DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new MovimentacaoCsvResponse(
                        rs.getString("titulo_obra"),
                        rs.getString("nome_leitor"),
                        rs.getObject("data_retirada", java.time.OffsetDateTime.class),
                        rs.getDate("data_prevista_devolucao").toLocalDate(),
                        rs.getObject("data_devolucao", java.time.OffsetDateTime.class)),
                dataInicio, dataInicio, dataFim, dataFim);
    }
}