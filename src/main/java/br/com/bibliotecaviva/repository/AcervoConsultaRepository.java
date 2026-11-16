package br.com.bibliotecaviva.repository;

import java.sql.Array;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import br.com.bibliotecaviva.dto.AcervoResponse;
import br.com.bibliotecaviva.dto.PaginaResponse;

@Repository
public class AcervoConsultaRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public AcervoConsultaRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    private static final String FILTRO = """
        FROM livro l WHERE l.ativo = TRUE
          AND (:titulo = '' OR position(lower(:titulo) in lower(l.titulo)) > 0)
          AND (:autor = '' OR EXISTS (SELECT 1 FROM livro_autor la JOIN autor a ON a.id = la.autor_id
              WHERE la.livro_id = l.id AND position(lower(:autor) in lower(a.nome)) > 0))
          AND (:categoria = '' OR EXISTS (SELECT 1 FROM livro_categoria lc JOIN categoria c ON c.id = lc.categoria_id
              WHERE lc.livro_id = l.id AND position(lower(:categoria) in lower(c.nome)) > 0))
        """;

    public PaginaResponse<AcervoResponse> pesquisar(String titulo, String autor, String categoria,
            int pagina, int tamanho) {
        Map<String, Object> params = Map.of("titulo", titulo, "autor", autor, "categoria", categoria,
                "limite", tamanho, "offset", (long) pagina * tamanho);
        long total = jdbc.queryForObject("SELECT count(*) " + FILTRO, params, Long.class);
        List<AcervoResponse> itens = jdbc.query("""
            SELECT l.id, l.titulo, l.isbn, l.ano_publicacao, l.descricao,
              ARRAY(SELECT a.nome FROM livro_autor la JOIN autor a ON a.id = la.autor_id
                    WHERE la.livro_id = l.id ORDER BY a.nome, a.id) AS autores,
              ARRAY(SELECT c.nome FROM livro_categoria lc JOIN categoria c ON c.id = lc.categoria_id
                    WHERE lc.livro_id = l.id ORDER BY c.nome, c.id) AS categorias,
              (SELECT count(*) FROM exemplar e WHERE e.livro_id = l.id AND e.status = 'disponivel'
                 AND NOT EXISTS (SELECT 1 FROM emprestimo em WHERE em.exemplar_id = e.id AND em.data_devolucao IS NULL)
                 AND NOT EXISTS (SELECT 1 FROM reserva r WHERE r.exemplar_id = e.id AND r.status = 'disponivel'))
                 AS disponiveis
            """ + FILTRO + " ORDER BY lower(l.titulo), l.id LIMIT :limite OFFSET :offset", params,
            (rs, n) -> new AcervoResponse(rs.getLong("id"), rs.getString("titulo"), rs.getString("isbn"),
                rs.getObject("ano_publicacao", Integer.class), rs.getString("descricao"),
                strings(rs.getArray("autores")), strings(rs.getArray("categorias")), rs.getLong("disponiveis")));
        return new PaginaResponse<>(itens, pagina, tamanho, total);
    }

    private static List<String> strings(Array array) throws SQLException {
        try { return Arrays.asList((String[]) array.getArray()); }
        finally { array.free(); }
    }
}
