package br.com.bibliotecaviva.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import br.com.bibliotecaviva.dto.MultaResponse;
import br.com.bibliotecaviva.dto.PaginaResponse;
import br.com.bibliotecaviva.exception.ConflitoException;
import br.com.bibliotecaviva.repository.MultaRepository;

@Service
public class MultaService {
    private static final Set<String> SITUACOES = Set.of("pendente", "quitada", "cancelada");
    private final MultaRepository repository;
    private final Clock clock;

    public MultaService(MultaRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public MultaResponse buscar(long id) {
        positivo(id);
        return repository.buscar(id);
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public PaginaResponse<MultaResponse> listar(String status, Long leitorId, Long emprestimoId,
            int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 100)
            throw new IllegalArgumentException("Página deve ser >= 0 e tamanho deve estar entre 1 e 100");
        if (leitorId != null) positivo(leitorId);
        if (emprestimoId != null) positivo(emprestimoId);
        String situacao = status == null || status.isBlank() ? null : status.trim().toLowerCase(Locale.ROOT);
        if (situacao != null && !SITUACOES.contains(situacao))
            throw new IllegalArgumentException("Situação deve ser pendente, quitada ou cancelada");
        return repository.listar(situacao, leitorId, emprestimoId, pagina, tamanho);
    }

    @Transactional
    public MultaResponse baixar(long id) {
        positivo(id);
        if (!repository.baixarPendente(id, OffsetDateTime.now(clock))) {
            repository.buscar(id);
            throw new ConflitoException("Somente multa pendente e sem pagamento registrado pode receber baixa");
        }
        return repository.buscar(id);
    }

    private void positivo(long id) {
        if (id < 1) throw new IllegalArgumentException("O identificador deve ser positivo");
    }
}
