package br.com.bibliotecaviva.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import br.com.bibliotecaviva.dto.ClubeLeituraResponse;
import br.com.bibliotecaviva.dto.EncontroClubeResponse;
import br.com.bibliotecaviva.dto.InscricaoEncontroResponse;
import br.com.bibliotecaviva.dto.MinhaInscricaoResponse;
import br.com.bibliotecaviva.dto.PaginaResponse;
import br.com.bibliotecaviva.dto.ParticipanteEncontroResponse;
import br.com.bibliotecaviva.dto.ReferenciaResponse;
import br.com.bibliotecaviva.exception.RecursoNaoEncontradoException;

@Repository
public class ClubeLeituraRepository {
    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate named;

    public ClubeLeituraRepository(JdbcTemplate jdbc, NamedParameterJdbcTemplate named) {
        this.jdbc = jdbc;
        this.named = named;
    }

    public record ClubeDados(long id, String nome, String descricao, String status, int capacidadeMaxima,
            String periodicidade, long responsavelId, OffsetDateTime criadoEm) { }
    public record LeitorDados(long id, boolean ativo, String perfil) { }
    public record EncontroDados(long id, long clubeId, long livroId, OffsetDateTime dataHora, String status) { }

    private static final RowMapper<ClubeDados> CLUBE = (rs, n) -> new ClubeDados(
        rs.getLong("id"), rs.getString("nome"), rs.getString("descricao"), rs.getString("status"),
        rs.getInt("capacidade_maxima"), rs.getString("periodicidade"), rs.getLong("responsavel_id"),
        rs.getObject("criado_em", OffsetDateTime.class));

    private static final RowMapper<EncontroClubeResponse> ENCONTRO = (rs, n) -> {
        int capacidade = rs.getInt("capacidade_maxima");
        long inscritos = rs.getLong("inscricoes_confirmadas");
        return new EncontroClubeResponse(rs.getLong("id"), rs.getLong("clube_id"),
            rs.getString("clube_nome"), rs.getLong("livro_id"), rs.getString("livro_titulo"),
            rs.getObject("data_hora", OffsetDateTime.class), rs.getString("local_ou_acesso"),
            rs.getString("status"), capacidade, inscritos, (int) Math.max(0, capacidade - inscritos),
            rs.getObject("criado_em", OffsetDateTime.class));
    };

    private static final String ENCONTRO_SELECT = """
        SELECT ec.id, ec.clube_id, c.nome AS clube_nome, ec.livro_id, l.titulo AS livro_titulo,
               ec.data_hora, ec.local_ou_acesso, ec.status, c.capacidade_maxima, ec.criado_em,
               (SELECT count(*) FROM inscricao_encontro ie
                WHERE ie.encontro_id = ec.id AND ie.status = 'confirmada') AS inscricoes_confirmadas
        FROM encontro_clube ec
        JOIN clube_leitura c ON c.id = ec.clube_id
        JOIN livro l ON l.id = ec.livro_id
        """;

    private static final RowMapper<InscricaoEncontroResponse> INSCRICAO = (rs, n) ->
        new InscricaoEncontroResponse(rs.getLong("id"), rs.getLong("encontro_id"),
            rs.getLong("leitor_id"), rs.getString("status"),
            rs.getObject("inscrito_em", OffsetDateTime.class),
            rs.getObject("cancelado_em", OffsetDateTime.class),
            rs.getObject("presente", Boolean.class),
            rs.getObject("presenca_registrada_em", OffsetDateTime.class));

    private static final RowMapper<ParticipanteEncontroResponse> PARTICIPANTE = (rs, n) ->
        new ParticipanteEncontroResponse(rs.getLong("leitor_id"), rs.getString("nome"),
            rs.getString("status"), rs.getObject("inscrito_em", OffsetDateTime.class),
            rs.getObject("presente", Boolean.class),
            rs.getObject("presenca_registrada_em", OffsetDateTime.class));

    private static final RowMapper<MinhaInscricaoResponse> MINHA_INSCRICAO = (rs, n) ->
        new MinhaInscricaoResponse(rs.getLong("id"), rs.getLong("clube_id"), rs.getString("clube_nome"),
            rs.getLong("encontro_id"), rs.getLong("livro_id"), rs.getString("livro_titulo"),
            rs.getObject("data_hora", OffsetDateTime.class), rs.getString("local_ou_acesso"),
            rs.getString("status_encontro"), rs.getString("status_inscricao"),
            rs.getObject("inscrito_em", OffsetDateTime.class), rs.getObject("cancelado_em", OffsetDateTime.class),
            rs.getObject("presente", Boolean.class), rs.getObject("presenca_registrada_em", OffsetDateTime.class));

    public PaginaResponse<ClubeLeituraResponse> listarClubes(boolean incluirEncerrados, String nome, Long categoriaId,
            int pagina, int tamanho) {
        var parametros = new MapSqlParameterSource()
            .addValue("incluirEncerrados", incluirEncerrados)
            .addValue("nome", nome)
            .addValue("categoriaId", categoriaId, java.sql.Types.BIGINT)
            .addValue("tamanho", tamanho)
            .addValue("offset", (long) pagina * tamanho);
        String filtro = """
            WHERE (:incluirEncerrados = TRUE OR c.status = 'ativo')
              AND (CAST(:nome AS VARCHAR) IS NULL OR c.nome ILIKE '%' || :nome || '%')
              AND (CAST(:categoriaId AS BIGINT) IS NULL OR EXISTS (
                    SELECT 1 FROM clube_leitura_categoria cc
                    WHERE cc.clube_id = c.id AND cc.categoria_id = :categoriaId))
            """;
        List<ClubeDados> clubes = named.query("""
            SELECT c.* FROM clube_leitura c
            """ + filtro + " ORDER BY c.nome, c.id LIMIT :tamanho OFFSET :offset", parametros, CLUBE);
        long total = named.queryForObject("SELECT count(*) FROM clube_leitura c " + filtro, parametros, Long.class);
        return new PaginaResponse<>(completarClubes(clubes), pagina, tamanho, total);
    }

    public ClubeLeituraResponse clube(long id, boolean travar) {
        ClubeDados clube = buscarClubeDados(id, travar);
        return completarClube(clube);
    }

    public ClubeDados buscarClubeDados(long id, boolean travar) {
        return jdbc.query("SELECT * FROM clube_leitura WHERE id = ?" + (travar ? " FOR UPDATE" : ""),
            CLUBE, id).stream().findFirst()
            .orElseThrow(() -> new RecursoNaoEncontradoException("Clube de leitura não encontrado"));
    }

    private List<ClubeLeituraResponse> completarClubes(List<ClubeDados> clubes) {
        return clubes.stream().map(this::completarClube).toList();
    }

    private ClubeLeituraResponse completarClube(ClubeDados clube) {
        List<ReferenciaResponse> categorias = jdbc.query("""
            SELECT c.id, c.nome FROM clube_leitura_categoria cc
            JOIN categoria c ON c.id = cc.categoria_id
            WHERE cc.clube_id = ? ORDER BY c.nome, c.id
            """, (rs, n) -> new ReferenciaResponse(rs.getLong("id"), rs.getString("nome")), clube.id());
        return new ClubeLeituraResponse(clube.id(), clube.nome(), clube.descricao(), clube.status(),
            clube.capacidadeMaxima(), clube.periodicidade(), clube.responsavelId(), clube.criadoEm(), categorias);
    }

    public void conferirCategorias(List<Long> ids) {
        if (ids.isEmpty()) return;
        Long existentes = named.queryForObject("SELECT count(*) FROM categoria WHERE id IN (:ids)",
            Map.of("ids", ids), Long.class);
        if (existentes != ids.size())
            throw new RecursoNaoEncontradoException("Há categoria não encontrada nos vínculos");
    }

    public long criarClube(String nome, String descricao, int capacidade, String periodicidade, long responsavelId) {
        return jdbc.queryForObject("""
            INSERT INTO clube_leitura (nome, descricao, capacidade_maxima, periodicidade, responsavel_id)
            VALUES (?, ?, ?, ?, ?) RETURNING id
            """, Long.class, nome, descricao, capacidade, periodicidade, responsavelId);
    }

    public void atualizarClube(long id, String nome, String descricao, int capacidade, String periodicidade) {
        jdbc.update("""
            UPDATE clube_leitura SET nome = ?, descricao = ?, capacidade_maxima = ?, periodicidade = ?
            WHERE id = ?
            """, nome, descricao, capacidade, periodicidade, id);
    }

    public void substituirCategorias(long clubeId, List<Long> categoriaIds) {
        jdbc.update("DELETE FROM clube_leitura_categoria WHERE clube_id = ?", clubeId);
        for (long categoriaId : categoriaIds) {
            jdbc.update("INSERT INTO clube_leitura_categoria (clube_id, categoria_id) VALUES (?, ?)",
                clubeId, categoriaId);
        }
    }

    public long maiorLotacaoEmEncontrosAbertos(long clubeId) {
        return jdbc.queryForObject("""
            SELECT COALESCE(max(quantidade), 0) FROM (
                SELECT count(*) AS quantidade
                FROM encontro_clube ec
                JOIN inscricao_encontro ie ON ie.encontro_id = ec.id AND ie.status = 'confirmada'
                WHERE ec.clube_id = ? AND ec.status = 'aberto'
                GROUP BY ec.id
            ) lotacoes
            """, Long.class, clubeId);
    }

    public void alterarStatusClube(long id, String status) {
        jdbc.update("UPDATE clube_leitura SET status = ? WHERE id = ?", status, id);
    }

    public boolean livroExiste(long id) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
            "SELECT EXISTS (SELECT 1 FROM livro WHERE id = ?)", Boolean.class, id));
    }

    public PaginaResponse<EncontroClubeResponse> listarEncontros(Long clubeId, boolean apenasClubeAtivo,
            int pagina, int tamanho) {
        var parametros = new MapSqlParameterSource()
            .addValue("clubeId", clubeId, java.sql.Types.BIGINT)
            .addValue("apenasClubeAtivo", apenasClubeAtivo)
            .addValue("tamanho", tamanho)
            .addValue("offset", (long) pagina * tamanho);
        String filtro = """
            WHERE (CAST(:clubeId AS BIGINT) IS NULL OR ec.clube_id = :clubeId)
              AND (:apenasClubeAtivo = FALSE OR c.status = 'ativo')
            """;
        List<EncontroClubeResponse> encontros = named.query(ENCONTRO_SELECT + filtro
            + " ORDER BY ec.data_hora DESC, ec.id DESC LIMIT :tamanho OFFSET :offset", parametros, ENCONTRO);
        long total = named.queryForObject("""
            SELECT count(*) FROM encontro_clube ec
            JOIN clube_leitura c ON c.id = ec.clube_id
            """ + filtro, parametros, Long.class);
        return new PaginaResponse<>(encontros, pagina, tamanho, total);
    }

    public EncontroClubeResponse encontro(long clubeId, long encontroId, boolean apenasClubeAtivo) {
        String filtro = apenasClubeAtivo ? " AND c.status = 'ativo'" : "";
        return jdbc.query(ENCONTRO_SELECT + """
            WHERE ec.clube_id = ? AND ec.id = ?
            """ + filtro, ENCONTRO, clubeId, encontroId).stream().findFirst()
            .orElseThrow(() -> new RecursoNaoEncontradoException("Encontro não encontrado"));
    }

    public EncontroDados encontroDados(long encontroId) {
        return jdbc.query("""
            SELECT id, clube_id, livro_id, data_hora, status
            FROM encontro_clube WHERE id = ?
            """, (rs, n) -> new EncontroDados(rs.getLong("id"), rs.getLong("clube_id"),
                rs.getLong("livro_id"), rs.getObject("data_hora", OffsetDateTime.class), rs.getString("status")),
            encontroId).stream().findFirst()
            .orElseThrow(() -> new RecursoNaoEncontradoException("Encontro não encontrado"));
    }

    public EncontroDados travarEncontro(long clubeId, long encontroId) {
        return jdbc.query("""
            SELECT id, clube_id, livro_id, data_hora, status
            FROM encontro_clube WHERE clube_id = ? AND id = ? FOR UPDATE
            """, (rs, n) -> new EncontroDados(rs.getLong("id"), rs.getLong("clube_id"),
                rs.getLong("livro_id"), rs.getObject("data_hora", OffsetDateTime.class), rs.getString("status")),
            clubeId, encontroId).stream().findFirst()
            .orElseThrow(() -> new RecursoNaoEncontradoException("Encontro não encontrado"));
    }

    public EncontroClubeResponse criarEncontro(long clubeId, long livroId, OffsetDateTime dataHora, String local) {
        long id = jdbc.queryForObject("""
            INSERT INTO encontro_clube (clube_id, livro_id, data_hora, local_ou_acesso)
            VALUES (?, ?, ?, ?) RETURNING id
            """, Long.class, clubeId, livroId, dataHora, local);
        return encontro(clubeId, id, false);
    }

    public EncontroClubeResponse atualizarEncontro(long clubeId, long encontroId, long livroId,
            OffsetDateTime dataHora, String local) {
        jdbc.update("""
            UPDATE encontro_clube SET livro_id = ?, data_hora = ?, local_ou_acesso = ?
            WHERE clube_id = ? AND id = ?
            """, livroId, dataHora, local, clubeId, encontroId);
        return encontro(clubeId, encontroId, false);
    }

    public void alterarStatusEncontro(long clubeId, long encontroId, String status) {
        jdbc.update("UPDATE encontro_clube SET status = ? WHERE clube_id = ? AND id = ?",
            status, clubeId, encontroId);
    }

    public LeitorDados travarLeitorDoUsuario(long usuarioId) {
        return leitorDoUsuario(usuarioId, true);
    }

    public LeitorDados leitorDoUsuario(long usuarioId, boolean travar) {
        return jdbc.query("""
            SELECT le.id, u.ativo, u.perfil
            FROM leitor le JOIN usuario u ON u.id = le.usuario_id
            WHERE u.id = ?
            """ + (travar ? " FOR UPDATE OF u, le" : ""), (rs, n) -> new LeitorDados(rs.getLong("id"),
                rs.getBoolean("ativo"), rs.getString("perfil")), usuarioId).stream().findFirst()
            .orElseThrow(() -> new RecursoNaoEncontradoException("Cadastro de leitor não encontrado"));
    }

    public Optional<InscricaoEncontroResponse> inscricao(long encontroId, long leitorId) {
        return jdbc.query("""
            SELECT * FROM inscricao_encontro WHERE encontro_id = ? AND leitor_id = ?
            """, INSCRICAO, encontroId, leitorId).stream().findFirst();
    }

    public long contarInscricoesConfirmadas(long encontroId) {
        return jdbc.queryForObject("""
            SELECT count(*) FROM inscricao_encontro
            WHERE encontro_id = ? AND status = 'confirmada'
            """, Long.class, encontroId);
    }

    public InscricaoEncontroResponse criarInscricao(long encontroId, long leitorId) {
        return jdbc.query("""
            INSERT INTO inscricao_encontro (encontro_id, leitor_id)
            VALUES (?, ?) RETURNING *
            """, INSCRICAO, encontroId, leitorId).get(0);
    }

    public InscricaoEncontroResponse cancelarInscricao(long encontroId, long leitorId, OffsetDateTime agora) {
        return jdbc.query("""
            UPDATE inscricao_encontro SET status = 'cancelada', cancelado_em = ?
            WHERE encontro_id = ? AND leitor_id = ? AND status = 'confirmada'
            RETURNING *
            """, INSCRICAO, agora, encontroId, leitorId).stream().findFirst()
            .orElseThrow(() -> new RecursoNaoEncontradoException("Inscrição ativa não encontrada"));
    }

    public PaginaResponse<ParticipanteEncontroResponse> listarParticipantes(long encontroId,
            int pagina, int tamanho) {
        List<ParticipanteEncontroResponse> participantes = jdbc.query("""
            SELECT le.id AS leitor_id, u.nome, ie.status, ie.inscrito_em,
                   ie.presente, ie.presenca_registrada_em
            FROM inscricao_encontro ie
            JOIN leitor le ON le.id = ie.leitor_id
            JOIN usuario u ON u.id = le.usuario_id
            WHERE ie.encontro_id = ?
            ORDER BY u.nome, le.id LIMIT ? OFFSET ?
            """, PARTICIPANTE, encontroId, tamanho, (long) pagina * tamanho);
        long total = jdbc.queryForObject(
            "SELECT count(*) FROM inscricao_encontro WHERE encontro_id = ?", Long.class, encontroId);
        return new PaginaResponse<>(participantes, pagina, tamanho, total);
    }

    public ParticipanteEncontroResponse registrarPresenca(long encontroId, long leitorId,
            boolean presente, OffsetDateTime agora) {
        int atualizados = jdbc.update("""
            UPDATE inscricao_encontro SET presente = ?, presenca_registrada_em = ?
            WHERE encontro_id = ? AND leitor_id = ? AND status = 'confirmada'
            """, presente, agora, encontroId, leitorId);
        if (atualizados == 0) throw new RecursoNaoEncontradoException("Inscrição ativa não encontrada");
        return participante(encontroId, leitorId);
    }

    public ParticipanteEncontroResponse participante(long encontroId, long leitorId) {
        return jdbc.query("""
            SELECT le.id AS leitor_id, u.nome, ie.status, ie.inscrito_em,
                   ie.presente, ie.presenca_registrada_em
            FROM inscricao_encontro ie
            JOIN leitor le ON le.id = ie.leitor_id
            JOIN usuario u ON u.id = le.usuario_id
            WHERE ie.encontro_id = ? AND ie.leitor_id = ?
            """, PARTICIPANTE, encontroId, leitorId).stream().findFirst()
            .orElseThrow(() -> new RecursoNaoEncontradoException("Inscrição não encontrada"));
    }

    public boolean existemPresencasPendentes(long encontroId) {
        return Boolean.TRUE.equals(jdbc.queryForObject("""
            SELECT EXISTS (
                SELECT 1 FROM inscricao_encontro
                WHERE encontro_id = ? AND status = 'confirmada' AND presente IS NULL
            )
            """, Boolean.class, encontroId));
    }

    public PaginaResponse<MinhaInscricaoResponse> minhasInscricoes(long leitorId, boolean historico,
            OffsetDateTime agora, int pagina, int tamanho) {
        var parametros = new MapSqlParameterSource()
            .addValue("leitorId", leitorId)
            .addValue("historico", historico)
            .addValue("agora", agora)
            .addValue("tamanho", tamanho)
            .addValue("offset", (long) pagina * tamanho);
        String filtro = """
            WHERE ie.leitor_id = :leitorId
              AND ((:historico = FALSE AND ie.status = 'confirmada'
                    AND ec.status = 'aberto' AND c.status = 'ativo' AND ec.data_hora >= :agora)
                OR (:historico = TRUE AND
                    (ie.status = 'cancelada' OR ec.status IN ('cancelado', 'concluido') OR ec.data_hora < :agora)))
            """;
        String select = """
            SELECT ie.id, c.id AS clube_id, c.nome AS clube_nome, ec.id AS encontro_id,
                   l.id AS livro_id, l.titulo AS livro_titulo, ec.data_hora, ec.local_ou_acesso,
                   ec.status AS status_encontro, ie.status AS status_inscricao, ie.inscrito_em,
                   ie.cancelado_em, ie.presente, ie.presenca_registrada_em
            FROM inscricao_encontro ie
            JOIN encontro_clube ec ON ec.id = ie.encontro_id
            JOIN clube_leitura c ON c.id = ec.clube_id
            JOIN livro l ON l.id = ec.livro_id
            """;
        List<MinhaInscricaoResponse> itens = named.query(select + filtro
            + " ORDER BY ec.data_hora " + (historico ? "DESC" : "ASC")
            + ", ec.id LIMIT :tamanho OFFSET :offset", parametros, MINHA_INSCRICAO);
        long total = named.queryForObject("""
            SELECT count(*) FROM inscricao_encontro ie
            JOIN encontro_clube ec ON ec.id = ie.encontro_id
            JOIN clube_leitura c ON c.id = ec.clube_id
            """ + filtro, parametros, Long.class);
        return new PaginaResponse<>(itens, pagina, tamanho, total);
    }
}
