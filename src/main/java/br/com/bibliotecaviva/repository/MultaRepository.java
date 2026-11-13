package br.com.bibliotecaviva.repository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import br.com.bibliotecaviva.dto.MultaResponse;
import br.com.bibliotecaviva.dto.PaginaResponse;
import br.com.bibliotecaviva.exception.RecursoNaoEncontradoException;

@Repository
public class MultaRepository {
    private static final String COLUNAS = """
        m.id, m.emprestimo_id, e.leitor_id, e.exemplar_id, m.dias_atraso,
        m.valor_diario_aplicado, m.valor_maximo_aplicado, m.valor_total,
        m.status, m.gerada_em, m.paga_em
        """;
    private static final String TABELAS = " FROM multa m JOIN emprestimo e ON e.id = m.emprestimo_id ";
    private static final RowMapper<MultaResponse> MULTA = (rs, n) -> new MultaResponse(
        rs.getLong("id"), rs.getLong("emprestimo_id"), rs.getLong("leitor_id"), rs.getLong("exemplar_id"),
        rs.getInt("dias_atraso"), rs.getBigDecimal("valor_diario_aplicado"),
        rs.getBigDecimal("valor_maximo_aplicado"), rs.getBigDecimal("valor_total"), rs.getString("status"),
        rs.getObject("gerada_em", OffsetDateTime.class), rs.getObject("paga_em", OffsetDateTime.class));
    private final JdbcTemplate jdbc;

    public MultaRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public MultaResponse buscar(long id) {
        return jdbc.query("SELECT " + COLUNAS + TABELAS + " WHERE m.id = ?", MULTA, id)
            .stream().findFirst().orElseThrow(() -> new RecursoNaoEncontradoException("Multa não encontrada"));
    }

    public PaginaResponse<MultaResponse> listar(String status, Long leitorId, Long emprestimoId,
            int pagina, int tamanho) {
        StringBuilder filtros = new StringBuilder(" WHERE 1 = 1");
        var parametros = new ArrayList<Object>();
        if (status != null) { filtros.append(" AND m.status = ?"); parametros.add(status); }
        if (leitorId != null) { filtros.append(" AND e.leitor_id = ?"); parametros.add(leitorId); }
        if (emprestimoId != null) { filtros.append(" AND m.emprestimo_id = ?"); parametros.add(emprestimoId); }
        long total = jdbc.queryForObject("SELECT count(*)" + TABELAS + filtros, Long.class, parametros.toArray());
        parametros.add(tamanho);
        parametros.add((long) pagina * tamanho);
        var itens = jdbc.query("SELECT " + COLUNAS + TABELAS + filtros + " ORDER BY m.id DESC LIMIT ? OFFSET ?",
            MULTA, parametros.toArray());
        return new PaginaResponse<>(itens, pagina, tamanho, total);
    }

    public boolean baixarPendente(long id, OffsetDateTime agora) {
        return jdbc.update("""
            UPDATE multa SET status = 'quitada', paga_em = ?
            WHERE id = ? AND status = 'pendente' AND paga_em IS NULL
            """, agora, id) == 1;
    }
}
