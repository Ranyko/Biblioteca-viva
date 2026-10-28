package br.com.bibliotecaviva.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.bibliotecaviva.dto.FilaReservaResponse;
import br.com.bibliotecaviva.dto.ReservaCreateRequest;
import br.com.bibliotecaviva.dto.ReservaResponse;
import br.com.bibliotecaviva.exception.ConflitoException;
import br.com.bibliotecaviva.exception.RecursoNaoEncontradoException;
import br.com.bibliotecaviva.dto.ReservaCancelamentoResponse;
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

                Leitor leitor = leitorService.buscarEntidade(request.leitorId());

                if (!leitor.getUsuario().isAtivo()) {
                        throw new ConflitoException(
                                        "Leitor inativo não pode realizar reservas");
                }

                Livro livro = livroService.buscarEntidade(request.livroId());

                long exemplaresDisponiveis = exemplarRepository.countByLivroIdAndStatus(
                                livro.getId(),
                                StatusExemplar.DISPONIVEL);

                if (exemplaresDisponiveis > 0) {
                        throw new ConflitoException(
                                        "Não é possível reservar uma obra que possui exemplar disponível");
                }

                boolean reservaDuplicada = reservaRepository.existsByLeitorIdAndLivroIdAndStatusIn(
                                leitor.getId(),
                                livro.getId(),
                                List.of(StatusReserva.ATIVA,
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

                int posicaoFila = reservaRepository
                                .findByLivroIdAndStatusOrderByDataSolicitacaoAsc(
                                                livro.getId(),
                                                StatusReserva.ATIVA)
                                .indexOf(salva) + 1;

                return ReservaResponse.from(
                                salva,
                                posicaoFila);
        }

        @Transactional(readOnly = true)
        public List<FilaReservaResponse> listarFila(Long livroId) {

                List<Reserva> reservas = reservaRepository
                                .findByLivroIdAndStatusOrderByDataSolicitacaoAsc(
                                                livroId,
                                                StatusReserva.ATIVA);

                List<FilaReservaResponse> resposta = new java.util.ArrayList<>();

                for (int i = 0; i < reservas.size(); i++) {

                        Reserva reserva = reservas.get(i);

                        resposta.add(
                                        new FilaReservaResponse(
                                                        i + 1,
                                                        reserva.getId(),
                                                        reserva.getLeitor().getId(),
                                                        reserva.getLeitor().getUsuario().getNome()));
                }

                return resposta;
        }

        @Transactional
        public ReservaCancelamentoResponse cancelar(Long reservaId) {

                Reserva reserva = reservaRepository.findById(reservaId)
                                .orElseThrow(() -> new RecursoNaoEncontradoException(
                                                "Reserva não encontrada"));

                reserva.setStatus(StatusReserva.CANCELADA);

                reservaRepository.save(reserva);

                return new ReservaCancelamentoResponse(
                                reserva.getId(),
                                reserva.getStatus().valor());
        }
}