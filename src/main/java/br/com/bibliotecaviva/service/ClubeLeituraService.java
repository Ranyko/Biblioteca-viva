package br.com.bibliotecaviva.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.bibliotecaviva.dto.ClubeLeituraRequest;
import br.com.bibliotecaviva.dto.ClubeLeituraResponse;
import br.com.bibliotecaviva.dto.EncontroClubeRequest;
import br.com.bibliotecaviva.dto.EncontroClubeResponse;
import br.com.bibliotecaviva.dto.InscricaoEncontroResponse;
import br.com.bibliotecaviva.dto.MinhaInscricaoResponse;
import br.com.bibliotecaviva.dto.PaginaResponse;
import br.com.bibliotecaviva.dto.ParticipanteEncontroResponse;
import br.com.bibliotecaviva.exception.ConflitoException;
import br.com.bibliotecaviva.exception.RecursoNaoEncontradoException;
import br.com.bibliotecaviva.repository.ClubeLeituraRepository;
import br.com.bibliotecaviva.repository.ClubeLeituraRepository.ClubeDados;
import br.com.bibliotecaviva.repository.ClubeLeituraRepository.EncontroDados;

@Service
public class ClubeLeituraService {
    private final ClubeLeituraRepository repository;
    private final Clock clock;

    public ClubeLeituraService(ClubeLeituraRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<ClubeLeituraResponse> listarClubes(boolean equipe, String nome, Long categoriaId,
            int pagina, int tamanho) {
        validarPaginacao(pagina, tamanho);
        if (categoriaId != null) positivo(categoriaId);
        String filtro = nome == null || nome.isBlank() ? null : nome.trim();
        return repository.listarClubes(equipe, filtro, categoriaId, pagina, tamanho);
    }

    @Transactional(readOnly = true)
    public ClubeLeituraResponse buscarClube(long id) {
        positivo(id);
        return repository.clube(id, false);
    }

    @Transactional
    public ClubeLeituraResponse criarClube(ClubeLeituraRequest request, long responsavelId) {
        validarCapacidade(request.capacidadeMaxima());
        validarIdsCategorias(request.categoriaIds());
        repository.conferirCategorias(request.categoriaIds());
        long id = repository.criarClube(nome(request.nome()), request.descricao(),
            request.capacidadeMaxima(), opcional(request.periodicidade()), responsavelId);
        repository.substituirCategorias(id, request.categoriaIds());
        return repository.clube(id, false);
    }

    @Transactional
    public ClubeLeituraResponse atualizarClube(long id, ClubeLeituraRequest request) {
        positivo(id);
        validarCapacidade(request.capacidadeMaxima());
        ClubeDados clube = repository.buscarClubeDados(id, true);
        validarIdsCategorias(request.categoriaIds());
        repository.conferirCategorias(request.categoriaIds());
        if (request.capacidadeMaxima() < repository.maiorLotacaoEmEncontrosAbertos(id))
            throw new ConflitoException("Capacidade não pode ser menor que as inscrições confirmadas em encontros abertos");
        repository.atualizarClube(id, nome(request.nome()), request.descricao(),
            request.capacidadeMaxima(), opcional(request.periodicidade()));
        repository.substituirCategorias(clube.id(), request.categoriaIds());
        return repository.clube(id, false);
    }

    @Transactional
    public ClubeLeituraResponse alterarStatusClube(long id, String status) {
        positivo(id);
        if (!List.of("ativo", "encerrado").contains(status))
            throw new IllegalArgumentException("Situação do clube inválida");
        repository.buscarClubeDados(id, true);
        repository.alterarStatusClube(id, status);
        return repository.clube(id, false);
    }

    @Transactional(readOnly = true)
    public PaginaResponse<EncontroClubeResponse> listarEncontros(Long clubeId, boolean apenasClubeAtivo,
            int pagina, int tamanho) {
        validarPaginacao(pagina, tamanho);
        if (clubeId != null) {
            ClubeDados clube = repository.buscarClubeDados(positivo(clubeId), false);
            if (apenasClubeAtivo && !"ativo".equals(clube.status()))
                throw new RecursoNaoEncontradoException("Clube de leitura não encontrado");
        }
        return repository.listarEncontros(clubeId, apenasClubeAtivo, pagina, tamanho);
    }

    @Transactional(readOnly = true)
    public PaginaResponse<EncontroClubeResponse> listarEncontrosDoClube(long clubeId,
            int pagina, int tamanho) {
        positivo(clubeId);
        validarPaginacao(pagina, tamanho);
        ClubeDados clube = repository.buscarClubeDados(clubeId, false);
        if (!"ativo".equals(clube.status()))
            throw new RecursoNaoEncontradoException("Clube de leitura não encontrado");
        return repository.listarEncontros(clubeId, true, pagina, tamanho);
    }

    @Transactional(readOnly = true)
    public EncontroClubeResponse buscarEncontroDoLeitor(long clubeId, long encontroId) {
        positivo(clubeId);
        positivo(encontroId);
        return repository.encontro(clubeId, encontroId, true);
    }

    @Transactional
    public EncontroClubeResponse criarEncontro(long clubeId, EncontroClubeRequest request) {
        positivo(clubeId);
        ClubeDados clube = repository.buscarClubeDados(clubeId, true);
        exigirClubeAtivo(clube);
        validarDataFutura(request.dataHora());
        validarLivro(request.livroId());
        return repository.criarEncontro(clubeId, request.livroId(), request.dataHora(),
            local(request.localOuAcesso()));
    }

    @Transactional
    public EncontroClubeResponse atualizarEncontro(long clubeId, long encontroId,
            EncontroClubeRequest request) {
        positivo(clubeId);
        positivo(encontroId);
        ClubeDados clube = repository.buscarClubeDados(clubeId, true);
        exigirClubeAtivo(clube);
        EncontroDados encontro = repository.travarEncontro(clubeId, encontroId);
        exigirEncontroAberto(encontro);
        if (!OffsetDateTime.now(clock).isBefore(encontro.dataHora()))
            throw new ConflitoException("Encontro iniciado não pode ser alterado");
        validarDataFutura(request.dataHora());
        validarLivro(request.livroId());
        return repository.atualizarEncontro(clubeId, encontroId, request.livroId(),
            request.dataHora(), local(request.localOuAcesso()));
    }

    @Transactional
    public EncontroClubeResponse cancelarEncontro(long clubeId, long encontroId) {
        ClubeDados clube = repository.buscarClubeDados(positivo(clubeId), true);
        EncontroDados encontro = repository.travarEncontro(clube.id(), positivo(encontroId));
        exigirEncontroAberto(encontro);
        if (!OffsetDateTime.now(clock).isBefore(encontro.dataHora()))
            throw new ConflitoException("Encontro iniciado não pode ser cancelado");
        repository.alterarStatusEncontro(clubeId, encontroId, "cancelado");
        return repository.encontro(clubeId, encontroId, false);
    }

    @Transactional
    public ParticipanteEncontroResponse registrarPresenca(long clubeId, long encontroId,
            long leitorId, boolean presente) {
        ClubeDados clube = repository.buscarClubeDados(positivo(clubeId), true);
        EncontroDados encontro = repository.travarEncontro(clube.id(), positivo(encontroId));
        positivo(leitorId);
        exigirEncontroAberto(encontro);
        OffsetDateTime agora = OffsetDateTime.now(clock);
        if (agora.isBefore(encontro.dataHora()))
            throw new ConflitoException("Presença só pode ser registrada após o início do encontro");
        return repository.registrarPresenca(encontroId, leitorId, presente, agora);
    }

    @Transactional
    public EncontroClubeResponse concluirEncontro(long clubeId, long encontroId) {
        ClubeDados clube = repository.buscarClubeDados(positivo(clubeId), true);
        EncontroDados encontro = repository.travarEncontro(clube.id(), positivo(encontroId));
        exigirEncontroAberto(encontro);
        if (OffsetDateTime.now(clock).isBefore(encontro.dataHora()))
            throw new ConflitoException("Encontro futuro não pode ser concluído");
        if (repository.existemPresencasPendentes(encontroId))
            throw new ConflitoException("Registre a presença de todos os inscritos antes de concluir o encontro");
        repository.alterarStatusEncontro(clubeId, encontroId, "concluido");
        return repository.encontro(clubeId, encontroId, false);
    }

    @Transactional(readOnly = true)
    public PaginaResponse<ParticipanteEncontroResponse> listarParticipantes(long clubeId, long encontroId,
            int pagina, int tamanho) {
        positivo(clubeId);
        positivo(encontroId);
        validarPaginacao(pagina, tamanho);
        repository.encontro(clubeId, encontroId, false);
        return repository.listarParticipantes(encontroId, pagina, tamanho);
    }

    @Transactional
    public InscricaoEncontroResponse inscrever(long encontroId, long usuarioId) {
        positivo(encontroId);
        ClubeDados clube;
        EncontroDados referencia = repository.encontroDados(encontroId);
        var leitor = leitorAtivo(usuarioId);
        clube = repository.buscarClubeDados(referencia.clubeId(), true);
        EncontroDados encontro = repository.travarEncontro(referencia.clubeId(), encontroId);
        exigirClubeAtivo(clube);
        exigirEncontroAberto(encontro);
        if (!OffsetDateTime.now(clock).isBefore(encontro.dataHora()))
            throw new ConflitoException("Inscrições para encontro iniciado não são permitidas");
        if (repository.inscricao(encontroId, leitor.id()).isPresent())
            throw new ConflitoException("Leitor já possui inscrição registrada para este encontro");
        if (repository.contarInscricoesConfirmadas(encontroId) >= clube.capacidadeMaxima())
            throw new ConflitoException("Encontro sem vagas disponíveis");
        return repository.criarInscricao(encontroId, leitor.id());
    }

    @Transactional
    public InscricaoEncontroResponse cancelarInscricao(long encontroId, long usuarioId) {
        positivo(encontroId);
        EncontroDados referencia = repository.encontroDados(encontroId);
        var leitor = leitorAtivo(usuarioId);
        repository.buscarClubeDados(referencia.clubeId(), true);
        EncontroDados encontro = repository.travarEncontro(referencia.clubeId(), encontroId);
        if (!OffsetDateTime.now(clock).isBefore(encontro.dataHora()))
            throw new ConflitoException("Inscrição não pode ser cancelada após o início do encontro");
        InscricaoEncontroResponse inscricao = repository.inscricao(encontroId, leitor.id())
            .orElseThrow(() -> new RecursoNaoEncontradoException("Inscrição não encontrada"));
        if (!"confirmada".equals(inscricao.status()))
            throw new ConflitoException("Inscrição já foi cancelada");
        return repository.cancelarInscricao(encontroId, leitor.id(), OffsetDateTime.now(clock));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<MinhaInscricaoResponse> minhasInscricoes(long usuarioId, boolean historico,
            int pagina, int tamanho) {
        validarPaginacao(pagina, tamanho);
        var leitor = repository.leitorDoUsuario(usuarioId, false);
        if (!leitor.ativo() || !"leitor".equals(leitor.perfil()))
            throw new ConflitoException("Leitor deve estar ativo e possuir perfil leitor");
        return repository.minhasInscricoes(leitor.id(), historico, OffsetDateTime.now(clock), pagina, tamanho);
    }

    private ClubeLeituraRepository.LeitorDados leitorAtivo(long usuarioId) {
        var leitor = repository.travarLeitorDoUsuario(usuarioId);
        if (!leitor.ativo() || !"leitor".equals(leitor.perfil()))
            throw new ConflitoException("Leitor deve estar ativo e possuir perfil leitor");
        return leitor;
    }

    private void exigirClubeAtivo(ClubeDados clube) {
        if (!"ativo".equals(clube.status()))
            throw new ConflitoException("Clube encerrado não aceita atividades ou inscrições");
    }

    private void exigirEncontroAberto(EncontroDados encontro) {
        if (!"aberto".equals(encontro.status()))
            throw new ConflitoException("Encontro não está aberto");
    }

    private void validarLivro(long livroId) {
        positivo(livroId);
        if (!repository.livroExiste(livroId))
            throw new RecursoNaoEncontradoException("Livro não encontrado");
    }

    private void validarIdsCategorias(List<Long> ids) {
        if (ids == null || ids.size() > 50 || ids.stream().anyMatch(id -> id == null || id <= 0)
                || new HashSet<>(ids).size() != ids.size())
            throw new IllegalArgumentException("Categorias devem ter IDs positivos e sem repetição, no máximo 50");
    }

    private void validarCapacidade(Integer capacidade) {
        if (capacidade == null || capacidade <= 0 || capacidade > Short.MAX_VALUE)
            throw new IllegalArgumentException("Capacidade máxima deve estar entre 1 e 32767");
    }

    private String nome(String valor) {
        String nome = valor.trim();
        if (nome.isEmpty() || nome.length() > 160)
            throw new IllegalArgumentException("Nome do clube inválido");
        return nome;
    }

    private String opcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private String local(String valor) {
        String local = valor.trim();
        if (local.isEmpty())
            throw new IllegalArgumentException("Local ou instrução de acesso é obrigatório");
        return local;
    }

    private void validarDataFutura(OffsetDateTime dataHora) {
        if (dataHora == null || !dataHora.isAfter(OffsetDateTime.now(clock)))
            throw new IllegalArgumentException("Data e hora do encontro devem estar no futuro");
    }

    private long positivo(long id) {
        if (id <= 0) throw new IllegalArgumentException("ID deve ser positivo");
        return id;
    }

    private void validarPaginacao(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 100)
            throw new IllegalArgumentException("Paginação inválida");
    }
}
