package br.com.bibliotecaviva.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.bibliotecaviva.dto.AvisoPreferencias;
import br.com.bibliotecaviva.dto.AvisoResponse;
import br.com.bibliotecaviva.dto.PaginaResponse;
import br.com.bibliotecaviva.dto.PendenciasResponse;
import br.com.bibliotecaviva.exception.RecursoNaoEncontradoException;
import br.com.bibliotecaviva.model.Aviso.ReferenciaTipo;
import br.com.bibliotecaviva.model.Aviso.TipoAviso;
import br.com.bibliotecaviva.model.Usuario;
import br.com.bibliotecaviva.repository.AvisoRepository;
import br.com.bibliotecaviva.repository.UsuarioRepository;

@Service
public class AvisoService {

    private final AvisoRepository avisoRepository;
    private final UsuarioRepository usuarioRepository;
    private final Clock clock;

    public AvisoService(AvisoRepository avisoRepository, UsuarioRepository usuarioRepository, Clock clock) {
        this.avisoRepository = avisoRepository;
        this.usuarioRepository = usuarioRepository;
        this.clock = clock;
    }

    /**
     * Geração pontual. Suprime o aviso quando o tipo está desligado nas preferências
     * ou quando o destinatário não está ativo (RNF10). A idempotência é do banco:
     * {@code ON CONFLICT DO NOTHING} devolve vazio quando já existe aviso idêntico.
     */
    @Transactional
    public Optional<Long> criar(Long usuarioId, TipoAviso tipo, String titulo, String mensagem,
            ReferenciaTipo referenciaTipo, Long referenciaId, LocalDate diaReferencia) {

        if (!preferenciaHabilitada(tipo)) return Optional.empty();

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
        if (!usuario.isAtivo()) return Optional.empty();

        return avisoRepository.inserirSeAusente(usuarioId, tipo.name(), titulo, mensagem,
                referenciaTipo == null ? null : referenciaTipo.name(), referenciaId, diaReferencia,
                OffsetDateTime.now(clock));
    }

    private boolean preferenciaHabilitada(TipoAviso tipo) {
        AvisoPreferencias p = avisoRepository.preferencias();
        return switch (tipo) {
            case DEVIDA_AMANHA -> p.devidaAmanha();
            case VENCIDO -> p.vencido();
            case RESERVA_DISPONIVEL -> p.reservaDisponivel();
            case RESERVA_EXPIRANDO -> p.reservaExpirando();
            case RESERVA_EXPIRADA -> p.reservaExpirada();
            case MULTA_GERADA -> p.multaGerada();
            case FILA_POSICAO -> p.filaPosicao();
        };
    }

    @Transactional(readOnly = true)
    public PaginaResponse<AvisoResponse> listar(Long usuarioId, Boolean lido, int pagina, int tamanho) {
        validarPaginacao(pagina, tamanho);
        return avisoRepository.pesquisar(usuarioId, lido, pagina, tamanho);
    }

    @Transactional(readOnly = true)
    public long contarNaoLidos(Long usuarioId) {
        return avisoRepository.contarNaoLidos(usuarioId);
    }

    /**
     * Marca o aviso como lido. Aviso inexistente ou de outro usuário → 404.
     * Re-marcar um aviso já lido é idempotente (devolve o estado atual, sem 404).
     */
    @Transactional
    public AvisoResponse marcarLido(Long avisoId, Long usuarioId) {
        avisoRepository.marcarLido(avisoId, usuarioId, OffsetDateTime.now(clock));
        return avisoRepository.buscar(avisoId, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aviso não encontrado"));
    }

    @Transactional
    public int marcarTodosLidos(Long usuarioId) {
        return avisoRepository.marcarTodosLidos(usuarioId, OffsetDateTime.now(clock));
    }

    @Transactional(readOnly = true)
    public PendenciasResponse pendencias() {
        LocalDate hoje = LocalDate.now(clock);
        return new PendenciasResponse(
                avisoRepository.atrasados(hoje),
                avisoRepository.reservasExpirando(hoje.plusDays(1)));
    }

    @Transactional(readOnly = true)
    public AvisoPreferencias preferencias() {
        return avisoRepository.preferencias();
    }

    @Transactional
    public AvisoPreferencias atualizarPreferencias(AvisoPreferencias novas) {
        return avisoRepository.atualizarPreferencias(novas);
    }

    private void validarPaginacao(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 100) {
            throw new IllegalArgumentException("Página deve ser >= 0 e tamanho deve estar entre 1 e 100");
        }
    }
}