package br.com.bibliotecaviva.repository;

import java.time.OffsetDateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import br.com.bibliotecaviva.dto.ConfiguracaoBibliotecaRequest;
import br.com.bibliotecaviva.dto.ConfiguracaoBibliotecaResponse;
import br.com.bibliotecaviva.exception.RecursoNaoEncontradoException;

@Repository
public class ConfiguracaoBibliotecaRepository {
    private static final String COLUNAS = """
        id, prazo_emprestimo_dias, limite_emprestimos, valor_multa_dia,
        valor_maximo_multa, prazo_retirada_reserva_dias, atualizado_em
        """;
    private static final RowMapper<ConfiguracaoBibliotecaResponse> CONFIGURACAO = (rs, n) ->
        new ConfiguracaoBibliotecaResponse(rs.getInt("id"), rs.getInt("prazo_emprestimo_dias"),
            rs.getInt("limite_emprestimos"), rs.getBigDecimal("valor_multa_dia"),
            rs.getBigDecimal("valor_maximo_multa"), rs.getInt("prazo_retirada_reserva_dias"),
            rs.getObject("atualizado_em", OffsetDateTime.class));
    private final JdbcTemplate jdbc;

    public ConfiguracaoBibliotecaRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public ConfiguracaoBibliotecaResponse buscar() {
        return jdbc.query("SELECT " + COLUNAS + " FROM configuracao_biblioteca WHERE id = 1", CONFIGURACAO)
            .stream().findFirst().orElseThrow(this::ausente);
    }

    public ConfiguracaoBibliotecaResponse atualizar(ConfiguracaoBibliotecaRequest request, OffsetDateTime agora) {
        return jdbc.query("""
            UPDATE configuracao_biblioteca
            SET prazo_emprestimo_dias = ?, limite_emprestimos = ?, valor_multa_dia = ?,
                valor_maximo_multa = ?, prazo_retirada_reserva_dias = ?, atualizado_em = ?
            WHERE id = 1 RETURNING
            """ + COLUNAS, CONFIGURACAO, request.prazoEmprestimoDias(), request.limiteEmprestimos(),
            request.valorMultaDia(), request.valorMaximoMulta(), request.prazoRetiradaReservaDias(), agora)
            .stream().findFirst().orElseThrow(this::ausente);
    }

    private RecursoNaoEncontradoException ausente() {
        return new RecursoNaoEncontradoException("Configuração da biblioteca não encontrada");
    }
}
