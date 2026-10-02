package br.com.bibliotecaviva.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.bibliotecaviva.dto.DevolucaoResponse;
import br.com.bibliotecaviva.dto.EmprestimoCreateRequest;
import br.com.bibliotecaviva.dto.EmprestimoResponse;
import br.com.bibliotecaviva.exception.ConflitoException;
import br.com.bibliotecaviva.repository.CirculacaoRepository;

@Service
public class CirculacaoService {
    private final CirculacaoRepository repository;
    private final Clock clock;

    public CirculacaoService(CirculacaoRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public EmprestimoResponse emprestar(EmprestimoCreateRequest request, long atendenteId) {
        var exemplar = repository.travarExemplar(request.exemplarId());
        var leitor = repository.travarLeitor(request.leitorId());
        var regras = repository.regras();
        var agora = OffsetDateTime.now(clock);
        if (!exemplar.livroAtivo()) throw new ConflitoException("Obra inativa não pode ser emprestada");
        if (!leitor.ativo() || !"leitor".equals(leitor.perfil())) throw new ConflitoException("Leitor deve estar ativo e possuir perfil leitor");
        if (repository.ativosDoLeitor(leitor.id()) >= regras.limite()) throw new ConflitoException("Limite de empréstimos atingido");
        if (repository.temEmprestimoAtivo(exemplar.id())) throw new ConflitoException("Exemplar já possui empréstimo ativo");

        var reserva = repository.reservaDoExemplar(exemplar.id());
        if ("reservado".equals(exemplar.status())) {
            var alocada = reserva.orElseThrow(() -> new ConflitoException("Exemplar reservado sem alocação válida"));
            if (alocada.leitorId() != leitor.id()) throw new ConflitoException("Exemplar reservado para outro leitor");
            if (alocada.limiteRetirada() == null || !agora.isBefore(alocada.limiteRetirada()))
                throw new ConflitoException("Prazo de retirada da reserva expirado");
        } else if (!"disponivel".equals(exemplar.status()) || reserva.isPresent()) {
            throw new ConflitoException("Exemplar indisponível para empréstimo");
        } else if (repository.primeiraReservaElegivel(exemplar.livroId(), regras.limite()).isPresent()) {
            throw new ConflitoException("Há fila de reservas prioritária; aloque o exemplar antes da retirada");
        }

        var emprestimo = repository.criar(leitor.id(), exemplar.id(), atendenteId,
            agora, agora.toLocalDate().plusDays(regras.prazoDias()));
        reserva.ifPresent(r -> repository.atenderReserva(r.id()));
        repository.statusExemplar(exemplar.id(), "emprestado");
        return emprestimo;
    }

    @Transactional
    public DevolucaoResponse devolver(long emprestimoId, long atendenteId) {
        var referencia = repository.buscar(emprestimoId);
        var exemplar = repository.travarExemplar(referencia.exemplarId());
        var emprestimo = repository.travarEmprestimo(emprestimoId);
        if (emprestimo.dataDevolucao() != null) throw new ConflitoException("Empréstimo já devolvido");
        if (!"emprestado".equals(exemplar.status())) throw new ConflitoException("Situação do exemplar incompatível com o empréstimo ativo");
        var agora = OffsetDateTime.now(clock);
        if (agora.isBefore(emprestimo.dataRetirada())) throw new ConflitoException("Devolução não pode ser anterior à retirada");
        var regras = repository.regras();
        long atraso = Math.max(0, ChronoUnit.DAYS.between(emprestimo.dataPrevistaDevolucao(), agora.toLocalDate()));
        BigDecimal valor = regras.diaria().multiply(BigDecimal.valueOf(atraso)).min(regras.teto()).setScale(2);
        var devolvido = repository.devolver(emprestimoId, atendenteId, agora);
        if (valor.signum() > 0) repository.gerarMulta(emprestimoId, Math.toIntExact(atraso), regras, agora);

        Optional<Long> proxima = exemplar.livroAtivo()
            ? repository.primeiraReservaElegivel(exemplar.livroId(), regras.limite()) : Optional.empty();
        String status = proxima.isPresent() ? "reservado" : "disponivel";
        proxima.ifPresent(id -> repository.disponibilizarReserva(id, exemplar.id(), agora,
            agora.plusDays(regras.prazoReservaDias())));
        repository.statusExemplar(exemplar.id(), status);
        return new DevolucaoResponse(devolvido, atraso, valor, status, proxima.orElse(null));
    }

    @Transactional(readOnly = true)
    public EmprestimoResponse buscar(long id) { return repository.buscar(id); }

    @Transactional(readOnly = true)
    public List<EmprestimoResponse> listar(boolean ativos, int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 100) throw new IllegalArgumentException("Paginação inválida");
        return repository.listar(ativos, pagina, tamanho);
    }
}
