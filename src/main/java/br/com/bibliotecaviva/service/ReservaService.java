package br.com.bibliotecaviva.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.bibliotecaviva.dto.FilaReservaResponse;
import br.com.bibliotecaviva.dto.ReservaCreateRequest;
import br.com.bibliotecaviva.dto.ReservaResponse;
import br.com.bibliotecaviva.exception.ConflitoException;
import br.com.bibliotecaviva.model.Leitor;
import br.com.bibliotecaviva.model.Livro;
import br.com.bibliotecaviva.model.Reserva;
import br.com.bibliotecaviva.model.StatusExemplar;
import br.com.bibliotecaviva.model.StatusReserva;
import br.com.bibliotecaviva.repository.ExemplarRepository;
import br.com.bibliotecaviva.repository.ReservaRepository;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final ExemplarRepository exemplarRepository;
    private final LeitorService leitorService;
    private final LivroService livroService;

    public ReservaService(
            ReservaRepository reservaRepository,
            ExemplarRepository exemplarRepository,
            LeitorService leitorService,
            LivroService livroService) {

        this.reservaRepository = reservaRepository;
        this.exemplarRepository = exemplarRepository;
        this.leitorService = leitorService;
        this.livroService = livroService;
    }

    @Transactional
    public ReservaResponse criar(ReservaCreateRequest request) {

        Leitor leitor =
                leitorService.buscarEntidade(request.leitorId());

        Livro livro =
                livroService.buscarEntidade(request.livroId());

        long exemplaresDisponiveis =
                exemplarRepository.countByLivroIdAndStatus(
                        livro.getId(),
                        StatusExemplar.DISPONIVEL);

        if (exemplaresDisponiveis > 0) {
            throw new ConflitoException(
                    "Não é possível reservar uma obra que possui exemplar disponível");
        }

        boolean reservaDuplicada =
                reservaRepository.existsByLeitorIdAndLivroIdAndStatusIn(
                        leitor.getId(),
                        livro.getId(),
                        List.of(
                                StatusReserva.ATIVA,
                                StatusReserva.DISPONIVEL));

        if (reservaDuplicada) {
            throw new ConflitoException(
                    "O leitor já possui uma reserva para esta obra");
        }

        Reserva reserva = new Reserva();

        reserva.setLeitor(leitor);
        reserva.setLivro(livro);
        reserva.setStatus(StatusReserva.ATIVA);
        reserva.setDataSolicitacao(OffsetDateTime.now());

        Reserva salva = reservaRepository.save(reserva);

        int posicaoFila =
                reservaRepository
                        .findByLivroIdAndStatusOrderByDataSolicitacaoAsc(
                                livro.getId(),
                                StatusReserva.ATIVA)
                        .indexOf(salva) + 1;

        return ReservaResponse.from(
                salva,
                posicaoFila);
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> consultarFila(Long livroId) {

        List<Reserva> reservas =
                reservaRepository
                        .findByLivroIdAndStatusOrderByDataSolicitacaoAsc(
                                livroId,
                                StatusReserva.ATIVA);

        List<ReservaResponse> resposta =
                new java.util.ArrayList<>();

        int posicao = 1;

        for (Reserva reserva : reservas) {

                resposta.add(
                        ReservaResponse.from(
                                reserva,
                                posicao));

                posicao++;
        }

        return resposta;
    }

    @Transactional(readOnly = true)
    public List<FilaReservaResponse> listarFila(Long livroId) {
        return reservaRepository
                .findByLivroIdAndStatusOrderByDataSolicitacaoAsc(
                        livroId,
                        StatusReserva.ATIVA)
                .stream()
                .map(reserva -> new FilaReservaResponse(
                        reservaRepository
                                .findByLivroIdAndStatusOrderByDataSolicitacaoAsc(
                                        livroId,
                                        StatusReserva.ATIVA)
                                .indexOf(reserva) + 1,
                        reserva.getId(),
                        reserva.getLeitor().getId(),
                        reserva.getLeitor().getUsuario().getNome()))
                .toList();
    }
}