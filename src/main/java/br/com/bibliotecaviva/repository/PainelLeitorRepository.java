package br.com.bibliotecaviva.repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import br.com.bibliotecaviva.dto.EmprestimoLeitorResponse;
import br.com.bibliotecaviva.dto.MultaLeitorResponse;
import br.com.bibliotecaviva.dto.ReservaLeitorResponse;

@Repository
public class PainelLeitorRepository {

    private final JdbcTemplate jdbc;

    public PainelLeitorRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<EmprestimoLeitorResponse> listarEmprestimos(long leitorId) {
        String sql = """
                SELECT e.id, l.titulo AS livro_titulo, e.data_retirada,
                       e.data_prevista_devolucao, e.data_devolucao
                FROM emprestimo e
                JOIN exemplar ex ON ex.id = e.exemplar_id
                JOIN livro l ON l.id = ex.livro_id
                WHERE e.leitor_id = ?
                ORDER BY (e.data_devolucao IS NULL) DESC,
                         e.data_prevista_devolucao ASC, e.id DESC
                """;

        return jdbc.query(sql, (rs, rowNum) -> {
            OffsetDateTime dataDevolucao = rs.getObject("data_devolucao", OffsetDateTime.class);
            return new EmprestimoLeitorResponse(
                    rs.getLong("id"),
                    rs.getString("livro_titulo"),
                    rs.getObject("data_retirada", OffsetDateTime.class),
                    rs.getObject("data_prevista_devolucao", LocalDate.class),
                    dataDevolucao,
                    dataDevolucao == null ? "ATIVO" : "DEVOLVIDO");
        }, leitorId);
    }

    public List<ReservaLeitorResponse> listarReservas(long leitorId) {
        String sql = """
                WITH fila AS (
                    SELECT id, ROW_NUMBER() OVER (
                        PARTITION BY livro_id ORDER BY data_solicitacao ASC, id ASC
                    )::INTEGER AS posicao_fila
                    FROM reserva
                    WHERE status = 'ativa'
                )
                SELECT r.id, l.titulo AS livro_titulo, r.status,
                       fila.posicao_fila, r.data_limite_retirada
                FROM reserva r
                JOIN livro l ON l.id = r.livro_id
                LEFT JOIN fila ON fila.id = r.id
                WHERE r.leitor_id = ?
                ORDER BY r.data_solicitacao DESC, r.id DESC
                """;

        return jdbc.query(sql, (rs, rowNum) -> new ReservaLeitorResponse(
                rs.getLong("id"),
                rs.getString("livro_titulo"),
                rs.getString("status"),
                rs.getObject("posicao_fila", Integer.class),
                rs.getObject("data_limite_retirada", OffsetDateTime.class)), leitorId);
    }

    public List<MultaLeitorResponse> listarMultas(long leitorId) {
        String sql = """
                SELECT m.id, m.valor_total, m.status, m.gerada_em, m.paga_em
                FROM multa m
                JOIN emprestimo e ON e.id = m.emprestimo_id
                WHERE e.leitor_id = ?
                ORDER BY m.gerada_em DESC
                """;

        return jdbc.query(sql, (rs, rowNum) -> new MultaLeitorResponse(
                rs.getLong("id"),
                rs.getBigDecimal("valor_total"),
                rs.getString("status"),
                rs.getObject("gerada_em", OffsetDateTime.class),
                rs.getObject("paga_em", OffsetDateTime.class)), leitorId);
    }
}
