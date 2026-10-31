package br.com.bibliotecaviva.repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import br.com.bibliotecaviva.dto.AvisoPreferencias;
import br.com.bibliotecaviva.dto.AvisoResponse;
import br.com.bibliotecaviva.dto.PaginaResponse;
import br.com.bibliotecaviva.dto.PendenciasResponse;

@Repository
public class AvisoRepository {

    private final NamedParameterJdbcTemplate jdbc;
    /** Fuso da biblioteca: usado no SQL para que `::date` não dependa do TimeZone da sessão JDBC (default da JVM). */
    private final String zona;

    public AvisoRepository(NamedParameterJdbcTemplate jdbc,
            @Value("${app.biblioteca.zone-id:America/Sao_Paulo}") String zona) {
        this.jdbc = jdbc;
        this.zona = zona;
    }

    // `lido` é tri-estado (doc §7: filtro `lido?`): null = todos, false = só não lidos, true = só lidos.
    private static final String FILTRO_PESQUISA = """
        FROM aviso a
        WHERE
            a.usuario_id = :usuarioId
            AND (:lido::boolean IS NULL
                 OR (:lido = TRUE  AND a.lido_em IS NOT NULL)
                 OR (:lido = FALSE AND a.lido_em IS NULL))
    """;

    public PaginaResponse<AvisoResponse> pesquisar(Long usuarioId, Boolean lido, int pagina, int tamanho) {

        Map<String, Object> params = new HashMap<>();
        params.put("usuarioId", usuarioId);
        params.put("lido", lido);
        params.put("limit", tamanho);
        params.put("offset", (long) pagina * tamanho);

        // Contagem separada: `COUNT(*) OVER()` some quando o OFFSET passa do fim (devolveria total = 0).
        Long total = jdbc.queryForObject("SELECT count(*) " + FILTRO_PESQUISA, params, Long.class);

        List<AvisoResponse> itens = jdbc.query("""
            SELECT a.* """ + FILTRO_PESQUISA + """
            ORDER BY a.criado_em DESC
            LIMIT :limit OFFSET :offset
            """, params, AVISO_MAPPER);

        return new PaginaResponse<>(itens, pagina, tamanho, total != null ? total : 0L);
    }

    private static final String MARCAR_LIDO_SQL = """
       UPDATE aviso
       SET lido_em = :lidoEm
       WHERE
           id = :avisoId
           AND usuario_id = :usuarioId
           AND lido_em IS NULL
    """;

    public int marcarLido(Long avisoId, Long usuarioId, OffsetDateTime lidoEm) {
        return jdbc.update(MARCAR_LIDO_SQL,
                Map.of("avisoId", avisoId, "usuarioId", usuarioId, "lidoEm", lidoEm));
    }

    private static final String MARCAR_TODOS_LIDOS_SQL = """
        UPDATE aviso
        SET lido_em = :lidoEm
        WHERE
            usuario_id = :usuarioId
            AND lido_em IS NULL
    """;

    public int marcarTodosLidos(Long usuarioId, OffsetDateTime lidoEm) {
        return jdbc.update(MARCAR_TODOS_LIDOS_SQL, Map.of("usuarioId", usuarioId, "lidoEm", lidoEm));
    }

    private static  final String CONTAR_NAO_LIDOS_QUERY = """
        SELECT count(*) 
        FROM aviso 
        WHERE 
            usuario_id = :usuarioId 
            AND lido_em IS NULL
    """;

    public long contarNaoLidos(Long usuarioId) {
        Long count = jdbc.queryForObject(CONTAR_NAO_LIDOS_QUERY, Map.of("usuarioId", usuarioId), Long.class);
        return count != null ? count : 0L;
    }

    private static final RowMapper<AvisoResponse> AVISO_MAPPER = (rs, n) -> new AvisoResponse(
            rs.getLong("id"),
            rs.getString("tipo"),
            rs.getString("titulo"),
            rs.getString("mensagem"),
            rs.getString("referencia_tipo"),
            rs.getObject("referencia_id", Long.class),
            rs.getObject("dia_referencia", LocalDate.class),
            rs.getObject("criado_em", OffsetDateTime.class),
            rs.getObject("lido_em", OffsetDateTime.class));

    private static final String BUSCAR_SQL = """
        SELECT *
        FROM aviso
        WHERE id = :avisoId AND usuario_id = :usuarioId
    """;

    public Optional<AvisoResponse> buscar(Long avisoId, Long usuarioId) {
        return jdbc.query(BUSCAR_SQL, Map.of("avisoId", avisoId, "usuarioId", usuarioId), AVISO_MAPPER)
                .stream().findFirst();
    }

    private static final String INSERIR_SE_AUSENTE_SQL = """
        INSERT INTO aviso (usuario_id, tipo, titulo, mensagem, referencia_tipo, referencia_id, dia_referencia, criado_em)
        VALUES (:usuarioId, :tipo, :titulo, :mensagem, :referenciaTipo, :referenciaId, :diaReferencia, :criadoEm)
        ON CONFLICT DO NOTHING
        RETURNING id
    """;

    public Optional<Long> inserirSeAusente(Long usuarioId, String tipo, String titulo, String mensagem,
            String referenciaTipo, Long referenciaId, LocalDate diaReferencia, OffsetDateTime criadoEm) {

        Map<String, Object> params = new HashMap<>();
        params.put("usuarioId", usuarioId);
        params.put("tipo", tipo);
        params.put("titulo", titulo);
        params.put("mensagem", mensagem);
        params.put("referenciaTipo", referenciaTipo);
        params.put("referenciaId", referenciaId);
        params.put("diaReferencia", diaReferencia);
        params.put("criadoEm", criadoEm);

        return jdbc.query(INSERIR_SE_AUSENTE_SQL, params, (rs, n) -> rs.getLong("id")).stream().findFirst();
    }

    private static final RowMapper<AvisoPreferencias> PREFERENCIAS_MAPPER = (rs, n) -> new AvisoPreferencias(
            rs.getBoolean("devida_amanha"),
            rs.getBoolean("vencido"),
            rs.getBoolean("reserva_disponivel"),
            rs.getBoolean("reserva_expirando"),
            rs.getBoolean("reserva_expirada"),
            rs.getBoolean("multa_gerada"),
            rs.getBoolean("fila_posicao"),
            rs.getObject("atualizado_em", OffsetDateTime.class));

    private static final String PREFERENCIAS_QUERY = """
        SELECT
            devida_amanha,
            vencido,
            reserva_disponivel,
            reserva_expirando,
            reserva_expirada,
            multa_gerada,
            fila_posicao,
            atualizado_em
        FROM aviso_preferencia
        WHERE id = 1
    """;

    public AvisoPreferencias preferencias() {
        return jdbc.query(PREFERENCIAS_QUERY, Map.of(), PREFERENCIAS_MAPPER).stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Preferências de aviso ausentes"));
    }

    private static final String ATUALIZAR_PREFERENCIAS_SQL = """
        UPDATE aviso_preferencia
        SET
            devida_amanha = :devidaAmanha,
            vencido = :vencido,
            reserva_disponivel = :reservaDisponivel,
            reserva_expirando = :reservaExpirando,
            reserva_expirada = :reservaExpirada,
            multa_gerada = :multaGerada,
            fila_posicao = :filaPosicao,
            atualizado_em = CURRENT_TIMESTAMP
        WHERE id = 1
        RETURNING
            devida_amanha,
            vencido,
            reserva_disponivel,
            reserva_expirando,
            reserva_expirada,
            multa_gerada,
            fila_posicao,
            atualizado_em
    """;

    public AvisoPreferencias atualizarPreferencias(AvisoPreferencias novas) {

        Map<String, Object> params = Map.of(
                "devidaAmanha", novas.devidaAmanha(),
                "vencido", novas.vencido(),
                "reservaDisponivel", novas.reservaDisponivel(),
                "reservaExpirando", novas.reservaExpirando(),
                "reservaExpirada", novas.reservaExpirada(),
                "multaGerada", novas.multaGerada(),
                "filaPosicao", novas.filaPosicao());

        return jdbc.query(ATUALIZAR_PREFERENCIAS_SQL, params, PREFERENCIAS_MAPPER).stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Preferências de aviso ausentes"));
    }

    // ------------------------------------------------------------------
    // Queries da varredura (doc §6/§8) — somente leitura sobre o domínio.
    // ------------------------------------------------------------------

    public record AlvoEmprestimo(long emprestimoId, long usuarioId, LocalDate dataPrevista) { }

    public record AlvoReserva(long reservaId, long usuarioId, OffsetDateTime dataLimite) { }

    private static final String EMPRESTIMOS_COM_PREVISTA_SQL = """
        SELECT em.id AS emprestimo_id, le.usuario_id, em.data_prevista_devolucao
        FROM emprestimo em
        JOIN leitor le ON le.id = em.leitor_id
        JOIN usuario u ON u.id = le.usuario_id
        WHERE em.data_devolucao IS NULL
          AND u.ativo = TRUE
          AND em.data_prevista_devolucao = :data
    """;

    public List<AlvoEmprestimo> emprestimosComPrevista(LocalDate data) {
        return jdbc.query(EMPRESTIMOS_COM_PREVISTA_SQL, Map.of("data", data),
                (rs, n) -> new AlvoEmprestimo(rs.getLong("emprestimo_id"), rs.getLong("usuario_id"),
                        rs.getObject("data_prevista_devolucao", LocalDate.class)));
    }

    private static final String EMPRESTIMOS_ATRASADOS_SQL = """
        SELECT em.id AS emprestimo_id, le.usuario_id, em.data_prevista_devolucao
        FROM emprestimo em
        JOIN leitor le ON le.id = em.leitor_id
        JOIN usuario u ON u.id = le.usuario_id
        WHERE em.data_devolucao IS NULL
          AND u.ativo = TRUE
          AND em.data_prevista_devolucao < :hoje
    """;

    public List<AlvoEmprestimo> emprestimosAtrasados(LocalDate hoje) {
        return jdbc.query(EMPRESTIMOS_ATRASADOS_SQL, Map.of("hoje", hoje),
                (rs, n) -> new AlvoEmprestimo(rs.getLong("emprestimo_id"), rs.getLong("usuario_id"),
                        rs.getObject("data_prevista_devolucao", LocalDate.class)));
    }

    private static final String RESERVAS_COM_LIMITE_SQL = """
        SELECT r.id AS reserva_id, le.usuario_id, r.data_limite_retirada
        FROM reserva r
        JOIN leitor le ON le.id = r.leitor_id
        JOIN usuario u ON u.id = le.usuario_id
        WHERE r.status = 'disponivel'
          AND u.ativo = TRUE
          AND (r.data_limite_retirada AT TIME ZONE :zona)::date = :data
    """;

    public List<AlvoReserva> reservasDisponiveisComLimite(LocalDate data) {
        return jdbc.query(RESERVAS_COM_LIMITE_SQL, Map.of("data", data, "zona", zona),
                (rs, n) -> new AlvoReserva(rs.getLong("reserva_id"), rs.getLong("usuario_id"),
                        rs.getObject("data_limite_retirada", OffsetDateTime.class)));
    }

    private static final String RESERVAS_EXPIRADAS_SQL = """
        SELECT r.id AS reserva_id, le.usuario_id, r.data_limite_retirada
        FROM reserva r
        JOIN leitor le ON le.id = r.leitor_id
        JOIN usuario u ON u.id = le.usuario_id
        WHERE r.status = 'expirada'
          AND u.ativo = TRUE
    """;

    public List<AlvoReserva> reservasExpiradas() {
        return jdbc.query(RESERVAS_EXPIRADAS_SQL, Map.of(),
                (rs, n) -> new AlvoReserva(rs.getLong("reserva_id"), rs.getLong("usuario_id"),
                        rs.getObject("data_limite_retirada", OffsetDateTime.class)));
    }

    public record PosicaoFila(long reservaId, long usuarioId, int posicao, Integer ultimaObservada) { }

    // Mesma elegibilidade de `CirculacaoRepository.primeiraReservaElegivel` (perfil leitor + limite de
    // empréstimos), para que a posição do aviso reflita a fila real usada na alocação.
    private static final String FILA_POSICOES_SQL = """
        SELECT
            r.id AS reserva_id,
            le.usuario_id,
            ROW_NUMBER() OVER (PARTITION BY r.livro_id ORDER BY r.data_solicitacao, r.id) AS posicao,
            r.ultima_posicao_observada
        FROM reserva r
        JOIN leitor le ON le.id = r.leitor_id
        JOIN usuario u ON u.id = le.usuario_id
        JOIN configuracao_biblioteca c ON c.id = 1
        WHERE r.status = 'ativa'
          AND u.ativo = TRUE
          AND u.perfil = 'leitor'
          AND (SELECT count(*) FROM emprestimo em
               WHERE em.leitor_id = le.id AND em.data_devolucao IS NULL) < c.limite_emprestimos
    """;

    public List<PosicaoFila> posicoesDaFila() {
        return jdbc.query(FILA_POSICOES_SQL, Map.of(),
                (rs, n) -> new PosicaoFila(rs.getLong("reserva_id"), rs.getLong("usuario_id"),
                        rs.getInt("posicao"), rs.getObject("ultima_posicao_observada", Integer.class)));
    }

    public void gravarUltimaPosicao(long reservaId, int posicao) {
        jdbc.update("UPDATE reserva SET ultima_posicao_observada = :posicao WHERE id = :reservaId",
                Map.of("reservaId", reservaId, "posicao", posicao));
    }

    // ------------------------------------------------------------------
    // Pendências do atendente (doc §7: /atendente/avisos/pendencias)
    // ------------------------------------------------------------------

    private static final String ATRASADOS_PENDENCIA_SQL = """
        SELECT
            em.id AS emprestimo_id,
            u.nome AS leitor_nome,
            l.titulo AS livro_titulo,
            em.data_prevista_devolucao,
            (:hoje::date - em.data_prevista_devolucao) AS dias_atraso
        FROM emprestimo em
        JOIN leitor le ON le.id = em.leitor_id
        JOIN usuario u ON u.id = le.usuario_id
        JOIN exemplar e ON e.id = em.exemplar_id
        JOIN livro l ON l.id = e.livro_id
        WHERE em.data_devolucao IS NULL
          AND em.data_prevista_devolucao < :hoje
        ORDER BY em.data_prevista_devolucao
    """;

    public List<PendenciasResponse.Atrasado> atrasados(LocalDate hoje) {
        return jdbc.query(ATRASADOS_PENDENCIA_SQL, Map.of("hoje", hoje),
                (rs, n) -> new PendenciasResponse.Atrasado(
                        rs.getLong("emprestimo_id"),
                        rs.getString("leitor_nome"),
                        rs.getString("livro_titulo"),
                        rs.getObject("data_prevista_devolucao", LocalDate.class),
                        rs.getLong("dias_atraso")));
    }

    private static final String RESERVAS_EXPIRANDO_PENDENCIA_SQL = """
        SELECT
            r.id AS reserva_id,
            u.nome AS leitor_nome,
            l.titulo AS livro_titulo,
            r.data_limite_retirada
        FROM reserva r
        JOIN leitor le ON le.id = r.leitor_id
        JOIN usuario u ON u.id = le.usuario_id
        JOIN livro l ON l.id = r.livro_id
        WHERE r.status = 'disponivel'
          AND r.data_limite_retirada IS NOT NULL
          AND (r.data_limite_retirada AT TIME ZONE :zona)::date <= :limite
        ORDER BY r.data_limite_retirada
    """;

    public List<PendenciasResponse.ReservaExpirando> reservasExpirando(LocalDate limite) {
        return jdbc.query(RESERVAS_EXPIRANDO_PENDENCIA_SQL, Map.of("limite", limite, "zona", zona),
                (rs, n) -> new PendenciasResponse.ReservaExpirando(
                        rs.getLong("reserva_id"),
                        rs.getString("leitor_nome"),
                        rs.getString("livro_titulo"),
                        rs.getObject("data_limite_retirada", OffsetDateTime.class)));
    }
}