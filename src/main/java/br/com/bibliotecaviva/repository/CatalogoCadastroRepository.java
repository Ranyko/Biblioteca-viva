package br.com.bibliotecaviva.repository;

import java.util.*;
import java.time.OffsetDateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import br.com.bibliotecaviva.dto.*;
import br.com.bibliotecaviva.exception.RecursoNaoEncontradoException;

@Repository
public class CatalogoCadastroRepository {
    public enum Tipo { AUTOR("autor"), CATEGORIA("categoria");
        final String tabela;
        Tipo(String tabela) { this.tabela = tabela; }
    }
    private final NamedParameterJdbcTemplate named;
    private final JdbcTemplate jdbc;
    public CatalogoCadastroRepository(NamedParameterJdbcTemplate named) {
        this.named = named;
        this.jdbc = named.getJdbcTemplate();
    }
    private static final RowMapper<ReferenciaResponse> REFERENCIA =
        (rs, n) -> new ReferenciaResponse(rs.getLong("id"), rs.getString("nome"));
    private static final RowMapper<LivroResponse> LIVRO = (rs, n) -> new LivroResponse(
        rs.getLong("id"), rs.getString("titulo"), rs.getString("isbn"),
        rs.getObject("ano_publicacao", Integer.class), rs.getString("descricao"),
        rs.getBoolean("ativo"), List.of(), List.of());
    private static final RowMapper<ExemplarResponse> EXEMPLAR = (rs, n) -> new ExemplarResponse(
        rs.getLong("id"), rs.getLong("livro_id"), rs.getString("codigo_tombo"),
        rs.getString("estado_conservacao"), rs.getString("status"),
        rs.getObject("cadastrado_em", OffsetDateTime.class));

    // Nomes SQL só vêm do enum fechado, nunca do corpo/parâmetros de uma requisição.
    public List<ReferenciaResponse> listarReferencias(Tipo tipo, int pagina, int tamanho) {
        return jdbc.query("SELECT id,nome FROM " + tipo.tabela + " ORDER BY nome,id LIMIT ? OFFSET ?",
            REFERENCIA, tamanho, (long) pagina * tamanho);
    }
    public ReferenciaResponse criarReferencia(Tipo tipo, String nome) {
        return jdbc.query("INSERT INTO " + tipo.tabela + "(nome) VALUES (?) RETURNING id,nome", REFERENCIA, nome).get(0);
    }
    public ReferenciaResponse referencia(Tipo tipo, long id) {
        return jdbc.query("SELECT id,nome FROM " + tipo.tabela + " WHERE id=?", REFERENCIA, id)
            .stream().findFirst().orElseThrow(() -> new RecursoNaoEncontradoException("Cadastro não encontrado"));
    }
    public ReferenciaResponse atualizarReferencia(Tipo tipo, long id, String nome) {
        return jdbc.query("UPDATE " + tipo.tabela + " SET nome=? WHERE id=? RETURNING id,nome", REFERENCIA, nome, id)
            .stream().findFirst().orElseThrow(() -> new RecursoNaoEncontradoException("Cadastro não encontrado"));
    }
    public void conferirReferencias(Tipo tipo, List<Long> ids) {
        if (ids.isEmpty()) return;
        List<Long> existentes = named.query("SELECT id FROM " + tipo.tabela + " WHERE id IN (:ids) ORDER BY id FOR KEY SHARE",
            Map.of("ids", ids), (rs, n) -> rs.getLong(1));
        if (existentes.size() != ids.size()) throw new RecursoNaoEncontradoException("Há " + tipo.tabela + " não encontrado nos vínculos");
    }
    public boolean isbnOcupado(String isbn, long excetoId) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
            "SELECT EXISTS(SELECT 1 FROM livro WHERE isbn=? AND id<>?)", Boolean.class, isbn, excetoId));
    }
    public boolean tomboOcupado(String tombo, long excetoId) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
            "SELECT EXISTS(SELECT 1 FROM exemplar WHERE codigo_tombo=? AND id<>?)", Boolean.class, tombo, excetoId));
    }
    public LivroResponse livro(long id, boolean travar) {
        var base = jdbc.query("SELECT * FROM livro WHERE id=?" + (travar ? " FOR UPDATE" : ""), LIVRO, id)
            .stream().findFirst().orElseThrow(() -> new RecursoNaoEncontradoException("Livro não encontrado"));
        return completarLivros(List.of(base)).get(0);
    }
    public PaginaResponse<LivroResponse> listarLivros(int pagina, int tamanho) {
        var livros = jdbc.query("SELECT * FROM livro ORDER BY titulo,id LIMIT ? OFFSET ?", LIVRO, tamanho, (long) pagina * tamanho);
        long total = jdbc.queryForObject("SELECT count(*) FROM livro", Long.class);
        return new PaginaResponse<>(completarLivros(livros), pagina, tamanho, total);
    }
    private List<LivroResponse> completarLivros(List<LivroResponse> livros) {
        if (livros.isEmpty()) return livros;
        var ids = livros.stream().map(LivroResponse::id).toList();
        var autores = vinculos(Tipo.AUTOR, ids);
        var categorias = vinculos(Tipo.CATEGORIA, ids);
        return livros.stream().map(l -> new LivroResponse(l.id(),l.titulo(),l.isbn(),l.anoPublicacao(),l.descricao(),l.ativo(),
            autores.getOrDefault(l.id(),List.of()),categorias.getOrDefault(l.id(),List.of()))).toList();
    }
    private Map<Long,List<ReferenciaResponse>> vinculos(Tipo tipo, List<Long> ids) {
        Map<Long,List<ReferenciaResponse>> resultado = new HashMap<>();
        named.query("SELECT la.livro_id,a.id,a.nome FROM livro_" + tipo.tabela + " la JOIN " + tipo.tabela +
            " a ON a.id=la." + tipo.tabela + "_id WHERE la.livro_id IN (:ids) ORDER BY a.nome,a.id",
            Map.of("ids",ids), (org.springframework.jdbc.core.RowCallbackHandler) rs ->
                resultado.computeIfAbsent(rs.getLong("livro_id"), x -> new ArrayList<>())
                    .add(new ReferenciaResponse(rs.getLong("id"),rs.getString("nome"))));
        return resultado;
    }
    public long criarLivro(LivroRequest r, String isbn) {
        return jdbc.queryForObject("""
            INSERT INTO livro(titulo,isbn,ano_publicacao,descricao,ativo) VALUES (?,?,?,?,?) RETURNING id
            """, Long.class, r.titulo().trim(),isbn,r.anoPublicacao(),r.descricao(),r.ativo());
    }
    public void atualizarLivro(long id, LivroRequest r, String isbn) {
        jdbc.update("UPDATE livro SET titulo=?,isbn=?,ano_publicacao=?,descricao=?,ativo=? WHERE id=?",
            r.titulo().trim(),isbn,r.anoPublicacao(),r.descricao(),r.ativo(),id);
    }
    public void substituirVinculos(long livroId, Tipo tipo, List<Long> ids) {
        jdbc.update("DELETE FROM livro_" + tipo.tabela + " WHERE livro_id=?",livroId);
        for (long id : ids) jdbc.update("INSERT INTO livro_" + tipo.tabela + "(livro_id," + tipo.tabela + "_id) VALUES (?,?)",livroId,id);
    }
    public ExemplarResponse exemplar(long id, boolean travar) {
        return jdbc.query("SELECT * FROM exemplar WHERE id=?" + (travar ? " FOR UPDATE" : ""), EXEMPLAR, id)
            .stream().findFirst().orElseThrow(() -> new RecursoNaoEncontradoException("Exemplar não encontrado"));
    }
    public PaginaResponse<ExemplarResponse> listarExemplares(Long livroId, int pagina, int tamanho) {
        var p = new HashMap<String,Object>();
        p.put("livro",livroId); p.put("tamanho",tamanho); p.put("offset",(long)pagina*tamanho);
        String filtro = livroId == null ? "" : " WHERE livro_id=:livro";
        var itens = named.query("SELECT * FROM exemplar"+filtro+" ORDER BY id LIMIT :tamanho OFFSET :offset",p,EXEMPLAR);
        long total = named.queryForObject("SELECT count(*) FROM exemplar"+filtro,p,Long.class);
        return new PaginaResponse<>(itens,pagina,tamanho,total);
    }
    public ExemplarResponse criarExemplar(ExemplarCreateRequest r, String tombo) {
        return jdbc.query("INSERT INTO exemplar(livro_id,codigo_tombo,estado_conservacao,status) VALUES (?,?,?,'disponivel') RETURNING *",
            EXEMPLAR,r.livroId(),tombo,r.estadoConservacao()).get(0);
    }
    public ExemplarResponse atualizarExemplar(long id, ExemplarUpdateRequest r, String tombo) {
        return jdbc.query("UPDATE exemplar SET codigo_tombo=?,estado_conservacao=? WHERE id=? RETURNING *",
            EXEMPLAR,tombo,r.estadoConservacao(),id).get(0);
    }
    public boolean movimentacaoAtiva(long id) {
        return Boolean.TRUE.equals(jdbc.queryForObject("""
            SELECT EXISTS(SELECT 1 FROM emprestimo WHERE exemplar_id=? AND data_devolucao IS NULL)
            OR EXISTS(SELECT 1 FROM reserva WHERE exemplar_id=? AND status='disponivel')
            """,Boolean.class,id,id));
    }
    public ExemplarResponse situacao(long id, boolean ativo) {
        return jdbc.query("UPDATE exemplar SET status=? WHERE id=? RETURNING *",EXEMPLAR,ativo?"disponivel":"inativo",id).get(0);
    }
}
