package br.com.bibliotecaviva.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import br.com.bibliotecaviva.dto.EmprestimoResponse;
import br.com.bibliotecaviva.dto.RenovacaoResponse;
import br.com.bibliotecaviva.exception.RecursoNaoEncontradoException;

/** SQL de circulação sobre o mesmo schema usado pelos cadastros H4/H5. */
@Repository
public class CirculacaoRepository {
    private final JdbcTemplate jdbc;
    public CirculacaoRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public record ExemplarDados(long id, long livroId, boolean livroAtivo, String status) { }
    public record LeitorDados(long id, boolean ativo, String perfil) { }
    public record Regras(int prazoDias, int limite, BigDecimal diaria, BigDecimal teto, int prazoReservaDias,
            int limiteRenovacoes) { }
    public record ReservaDados(long id, long leitorId, OffsetDateTime limiteRetirada) { }
    public record EmprestimoRenovacaoDados(long id, long leitorId, long livroId,
            LocalDate dataPrevistaDevolucao, OffsetDateTime dataDevolucao, int limiteRenovacoesSnapshot) { }

    private static final RowMapper<EmprestimoResponse> EMPRESTIMO = (rs, n) -> new EmprestimoResponse(
        rs.getLong("id"), rs.getLong("leitor_id"), rs.getLong("exemplar_id"), rs.getLong("atendente_retirada_id"),
        rs.getObject("atendente_devolucao_id", Long.class), rs.getObject("data_retirada", OffsetDateTime.class),
        rs.getObject("data_prevista_devolucao", LocalDate.class), rs.getObject("data_devolucao", OffsetDateTime.class));
    private static final RowMapper<RenovacaoResponse> RENOVACAO = (rs, n) -> new RenovacaoResponse(
        rs.getLong("id"), rs.getLong("emprestimo_id"), rs.getInt("numero_renovacao"),
        rs.getInt("limite_renovacoes_snapshot"), rs.getObject("data_prevista_anterior", LocalDate.class),
        rs.getObject("data_prevista_nova", LocalDate.class), rs.getObject("atendente_renovacao_id", Long.class),
        rs.getObject("renovada_em", OffsetDateTime.class));


    public ExemplarDados travarExemplar(long exemplarId) {
        List<Long> livros = jdbc.query("""
            SELECT l.id FROM livro l JOIN exemplar e ON e.livro_id = l.id
            WHERE e.id = ? FOR UPDATE OF l
            """, (rs, n) -> rs.getLong(1), exemplarId);
        if (livros.isEmpty()) throw new RecursoNaoEncontradoException("Exemplar não encontrado");
        return jdbc.query("""
            SELECT e.id, e.livro_id, e.status, l.ativo FROM exemplar e JOIN livro l ON l.id = e.livro_id
            WHERE e.id = ? FOR UPDATE OF e
            """, (rs, n) -> new ExemplarDados(rs.getLong("id"), rs.getLong("livro_id"),
                rs.getBoolean("ativo"), rs.getString("status")), exemplarId).stream().findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Exemplar não encontrado"));
    }

    public LeitorDados travarLeitor(long leitorId) {
        List<Long> usuarios = jdbc.query("SELECT usuario_id FROM leitor WHERE id = ?",
            (rs, n) -> rs.getLong(1), leitorId);
        if (usuarios.isEmpty()) throw new RecursoNaoEncontradoException("Leitor não encontrado");
        jdbc.query("SELECT id FROM usuario WHERE id = ? FOR UPDATE", (rs, n) -> rs.getLong(1), usuarios.get(0));
        return jdbc.query("""
            SELECT le.id, u.ativo, u.perfil FROM leitor le JOIN usuario u ON u.id = le.usuario_id
            WHERE le.id = ? FOR UPDATE OF le
            """, (rs, n) -> new LeitorDados(rs.getLong("id"), rs.getBoolean("ativo"), rs.getString("perfil")),
            leitorId).stream().findFirst().orElseThrow(() -> new RecursoNaoEncontradoException("Leitor não encontrado"));
    }

    public Regras regras() {
        return jdbc.query("SELECT * FROM configuracao_biblioteca WHERE id = 1", (rs, n) -> new Regras(
            rs.getInt("prazo_emprestimo_dias"), rs.getInt("limite_emprestimos"), rs.getBigDecimal("valor_multa_dia"),
            rs.getBigDecimal("valor_maximo_multa"), rs.getInt("prazo_retirada_reserva_dias"),
            rs.getInt("limite_renovacoes"))).stream().findFirst()
            .orElseThrow(() -> new IllegalStateException("Configuração da biblioteca ausente"));
    }

    public long ativosDoLeitor(long leitorId) {
        return jdbc.queryForObject("SELECT count(*) FROM emprestimo WHERE leitor_id = ? AND data_devolucao IS NULL",
            Long.class, leitorId);
    }

    public boolean temEmprestimoAtivo(long exemplarId) {
        return Boolean.TRUE.equals(jdbc.queryForObject("""
            SELECT EXISTS (SELECT 1 FROM emprestimo WHERE exemplar_id = ? AND data_devolucao IS NULL)
            """, Boolean.class, exemplarId));
    }

    public Optional<ReservaDados> reservaDoExemplar(long exemplarId) {
        return jdbc.query("SELECT id, leitor_id, data_limite_retirada FROM reserva WHERE exemplar_id = ? AND status = 'disponivel' FOR UPDATE",
            (rs, n) -> new ReservaDados(rs.getLong("id"), rs.getLong("leitor_id"),
                rs.getObject("data_limite_retirada", OffsetDateTime.class)), exemplarId).stream().findFirst();
    }

    public void travarLeitoresDaFila(long livroId) {
        List<Long> leitores = jdbc.query("""
            SELECT le.id FROM leitor le
            WHERE EXISTS (
                SELECT 1 FROM reserva r
                WHERE r.leitor_id = le.id AND r.livro_id = ? AND r.status = 'ativa'
            )
            ORDER BY le.usuario_id, le.id
            """, (rs, n) -> rs.getLong("id"), livroId);
        // Mesma ordem de usuários em filas diferentes; usuário antes do leitor, como na retirada.
        leitores.forEach(this::travarLeitor);
    }

    public Optional<Long> primeiraReservaElegivel(long livroId, int limite) {
        return jdbc.query("""
            SELECT r.id FROM reserva r JOIN leitor le ON le.id = r.leitor_id JOIN usuario u ON u.id = le.usuario_id
            WHERE r.livro_id = ? AND r.status = 'ativa' AND u.ativo = TRUE AND u.perfil = 'leitor'
              AND (SELECT count(*) FROM emprestimo em WHERE em.leitor_id = le.id AND em.data_devolucao IS NULL) < ?
            ORDER BY r.data_solicitacao, r.id LIMIT 1 FOR UPDATE OF r
            """, (rs, n) -> rs.getLong(1), livroId, limite).stream().findFirst();
    }

    public EmprestimoResponse criar(long leitorId, long exemplarId, long atendenteId,
            OffsetDateTime retirada, LocalDate prevista, int limiteRenovacoes) {
        return jdbc.query("""
            INSERT INTO emprestimo (
                leitor_id, exemplar_id, atendente_retirada_id, data_retirada,
                data_prevista_devolucao, limite_renovacoes_snapshot
            )
            VALUES (?, ?, ?, ?, ?, ?) RETURNING *
            """, EMPRESTIMO, leitorId, exemplarId, atendenteId, retirada, prevista, limiteRenovacoes).get(0);
    }

    public long travarLivroDoEmprestimo(long emprestimoId, long usuarioId) {
        return jdbc.query("""
            SELECT l.id
            FROM emprestimo em
            JOIN exemplar e ON e.id = em.exemplar_id
            JOIN livro l ON l.id = e.livro_id
            JOIN leitor le ON le.id = em.leitor_id
            WHERE em.id = ? AND le.usuario_id = ?
            FOR UPDATE OF l
            """, (rs, n) -> rs.getLong(1), emprestimoId, usuarioId).stream().findFirst()
            .orElseThrow(() -> new RecursoNaoEncontradoException("Empréstimo não encontrado"));
    }

    public long travarLivroDoEmprestimo(long emprestimoId) {
        return jdbc.query("""
            SELECT l.id
            FROM emprestimo em
            JOIN exemplar e ON e.id = em.exemplar_id
            JOIN livro l ON l.id = e.livro_id
            WHERE em.id = ?
            FOR UPDATE OF l
            """, (rs, n) -> rs.getLong(1), emprestimoId).stream().findFirst()
            .orElseThrow(() -> new RecursoNaoEncontradoException("Empréstimo não encontrado"));
    }

    public void travarLivroParaAlterarReservas(long livroId) {
        jdbc.query("SELECT id FROM livro WHERE id = ? FOR UPDATE",
            (rs, n) -> rs.getLong(1), livroId).stream().findFirst()
            .orElseThrow(() -> new RecursoNaoEncontradoException("Livro não encontrado"));
    }

    public EmprestimoRenovacaoDados travarEmprestimoDoLeitor(long emprestimoId, long usuarioId) {
        return jdbc.query("""
            SELECT em.id, em.leitor_id, e.livro_id, em.data_prevista_devolucao,
                   em.data_devolucao, em.limite_renovacoes_snapshot
            FROM emprestimo em
            JOIN exemplar e ON e.id = em.exemplar_id
            JOIN leitor le ON le.id = em.leitor_id
            WHERE em.id = ? AND le.usuario_id = ?
            FOR UPDATE OF em
            """, (rs, n) -> new EmprestimoRenovacaoDados(
                rs.getLong("id"), rs.getLong("leitor_id"), rs.getLong("livro_id"),
                rs.getObject("data_prevista_devolucao", LocalDate.class),
                rs.getObject("data_devolucao", OffsetDateTime.class),
                rs.getInt("limite_renovacoes_snapshot")), emprestimoId, usuarioId).stream().findFirst()
            .orElseThrow(() -> new RecursoNaoEncontradoException("Empréstimo não encontrado"));
    }

    public EmprestimoRenovacaoDados travarEmprestimoParaRenovacao(long emprestimoId) {
        return jdbc.query("""
            SELECT em.id, em.leitor_id, e.livro_id, em.data_prevista_devolucao,
                   em.data_devolucao, em.limite_renovacoes_snapshot
            FROM emprestimo em
            JOIN exemplar e ON e.id = em.exemplar_id
            WHERE em.id = ?
            FOR UPDATE OF em
            """, (rs, n) -> new EmprestimoRenovacaoDados(
                rs.getLong("id"), rs.getLong("leitor_id"), rs.getLong("livro_id"),
                rs.getObject("data_prevista_devolucao", LocalDate.class),
                rs.getObject("data_devolucao", OffsetDateTime.class),
                rs.getInt("limite_renovacoes_snapshot")), emprestimoId).stream().findFirst()
            .orElseThrow(() -> new RecursoNaoEncontradoException("Empréstimo não encontrado"));
    }

    public boolean temReservaAtiva(long livroId) {
        return Boolean.TRUE.equals(jdbc.queryForObject("""
            SELECT EXISTS (
                SELECT 1 FROM reserva
                WHERE livro_id = ? AND status IN ('ativa', 'disponivel')
            )
            """, Boolean.class, livroId));
    }

    public int quantidadeRenovacoes(long emprestimoId) {
        Long quantidade = jdbc.queryForObject(
            "SELECT count(*) FROM renovacao_emprestimo WHERE emprestimo_id = ?", Long.class, emprestimoId);
        return Math.toIntExact(quantidade);
    }

    public RenovacaoResponse registrarRenovacao(long emprestimoId, long leitorId, int numero,
            LocalDate dataAnterior, LocalDate dataNova, Long atendenteRenovacaoId, OffsetDateTime renovadaEm) {
        return jdbc.query("""
            WITH renovacao AS (
                INSERT INTO renovacao_emprestimo (
                    emprestimo_id, leitor_id, numero_renovacao,
                    data_prevista_anterior, data_prevista_nova, atendente_renovacao_id, renovada_em
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                RETURNING *
            )
            SELECT renovacao.*, emprestimo.limite_renovacoes_snapshot
            FROM renovacao
            JOIN emprestimo ON emprestimo.id = renovacao.emprestimo_id
            """, RENOVACAO, emprestimoId, leitorId, numero,
            dataAnterior, dataNova, atendenteRenovacaoId, renovadaEm).get(0);
    }

    public boolean atualizarDataPrevista(long emprestimoId, LocalDate dataAnterior, LocalDate dataNova) {
        return jdbc.update("""
            UPDATE emprestimo
            SET data_prevista_devolucao = ?
            WHERE id = ? AND data_prevista_devolucao = ? AND data_devolucao IS NULL
            """, dataNova, emprestimoId, dataAnterior) == 1;
    }

    public List<RenovacaoResponse> historicoRenovacoes(long emprestimoId, long usuarioId) {
        boolean pertenceAoLeitor = Boolean.TRUE.equals(jdbc.queryForObject("""
            SELECT EXISTS (
                SELECT 1 FROM emprestimo em
                JOIN leitor le ON le.id = em.leitor_id
                WHERE em.id = ? AND le.usuario_id = ?
            )
            """, Boolean.class, emprestimoId, usuarioId));
        if (!pertenceAoLeitor) throw new RecursoNaoEncontradoException("Empréstimo não encontrado");
        return jdbc.query("""
            SELECT r.*, em.limite_renovacoes_snapshot
            FROM renovacao_emprestimo r
            JOIN emprestimo em ON em.id = r.emprestimo_id
            WHERE r.emprestimo_id = ?
            ORDER BY r.numero_renovacao
            """, RENOVACAO, emprestimoId);
    }

    public List<RenovacaoResponse> historicoRenovacoesAtendente(long emprestimoId) {
        buscar(emprestimoId);
        return jdbc.query("""
            SELECT r.*, em.limite_renovacoes_snapshot
            FROM renovacao_emprestimo r
            JOIN emprestimo em ON em.id = r.emprestimo_id
            WHERE r.emprestimo_id = ?
            ORDER BY r.numero_renovacao
            """, RENOVACAO, emprestimoId);
    }

    public EmprestimoResponse buscar(long id) {
        return jdbc.query("SELECT * FROM emprestimo WHERE id = ?", EMPRESTIMO, id).stream().findFirst()
            .orElseThrow(() -> new RecursoNaoEncontradoException("Empréstimo não encontrado"));
    }

    public EmprestimoResponse travarEmprestimo(long id) {
        return jdbc.query("SELECT * FROM emprestimo WHERE id = ? FOR UPDATE", EMPRESTIMO, id).stream().findFirst()
            .orElseThrow(() -> new RecursoNaoEncontradoException("Empréstimo não encontrado"));
    }

    public List<EmprestimoResponse> listar(boolean somenteAtivos, int pagina, int tamanho) {
        return jdbc.query("SELECT * FROM emprestimo WHERE (? = FALSE OR data_devolucao IS NULL) ORDER BY id DESC LIMIT ? OFFSET ?",
            EMPRESTIMO, somenteAtivos, tamanho, (long) pagina * tamanho);
    }

    public EmprestimoResponse devolver(long id, long atendenteId, OffsetDateTime agora) {
        return jdbc.query("UPDATE emprestimo SET data_devolucao = ?, atendente_devolucao_id = ? WHERE id = ? RETURNING *",
            EMPRESTIMO, agora, atendenteId, id).get(0);
    }

    public void statusExemplar(long id, String status) {
        jdbc.update("UPDATE exemplar SET status = ? WHERE id = ?", status, id);
    }

    public void atenderReserva(long id) {
        travarLivroDaReserva(id);
        jdbc.update("UPDATE reserva SET status = 'atendida' WHERE id = ?", id);
    }

    public void disponibilizarReserva(long id, long exemplarId, OffsetDateTime agora, OffsetDateTime limite) {
        travarLivroDaReserva(id);
        jdbc.update("""
            UPDATE reserva SET status = 'disponivel', exemplar_id = ?, data_disponibilizacao = ?, data_limite_retirada = ?
            WHERE id = ?
            """, exemplarId, agora, limite, id);
    }

    private void travarLivroDaReserva(long reservaId) {
        long livroId = jdbc.query("SELECT livro_id FROM reserva WHERE id = ?",
            (rs, n) -> rs.getLong(1), reservaId).stream().findFirst()
            .orElseThrow(() -> new RecursoNaoEncontradoException("Reserva não encontrada"));
        travarLivroParaAlterarReservas(livroId);
    }

    public void gerarMulta(long emprestimoId, int atraso, Regras regras, OffsetDateTime agora) {
        jdbc.update("""
            INSERT INTO multa (emprestimo_id, dias_atraso, valor_diario_aplicado, valor_maximo_aplicado, gerada_em)
            VALUES (?, ?, ?, ?, ?)
            """, emprestimoId, atraso, regras.diaria(), regras.teto(), agora);
    }
}
