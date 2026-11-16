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
import br.com.bibliotecaviva.model.Exemplar;
import br.com.bibliotecaviva.model.StatusExemplar;
import br.com.bibliotecaviva.model.StatusReserva;
import br.com.bibliotecaviva.repository.CirculacaoRepository;
import br.com.bibliotecaviva.repository.ExemplarRepository;
import br.com.bibliotecaviva.repository.ReservaRepository;

@Service
public class ReservaService {

        private final ReservaRepository reservaRepository;
        private final ExemplarRepository exemplarRepository;
        private final LeitorService leitorService;
        private final LivroService livroService;
        private final CirculacaoRepository circulacaoRepository;
        private final CirculacaoService circulacaoService;

        public ReservaService(
                        ReservaRepository reservaRepository,
                        ExemplarRepository exemplarRepository,
                        LeitorService leitorService,
                        LivroService livroService,
                        CirculacaoRepository circulacaoRepository,
                        CirculacaoService circulacaoService) {

                this.reservaRepository = reservaRepository;
                this.exemplarRepository = exemplarRepository;
                this.leitorService = leitorService;
                this.livroService = livroService;
                this.circulacaoRepository = circulacaoRepository;
                this.circulacaoService = circulacaoService;
        }

        @Transactional
        public ReservaResponse criar(ReservaCreateRequest request) {
                Leitor leitor = leitorService.buscarEntidade(request.leitorId());
                return criarReserva(leitor, request.livroId());
        }

        private ReservaResponse criarReserva(Leitor leitor, Long livroId) {

                if (!leitor.getUsuario().isAtivo()) {
                        throw new ConflitoException(
                                        "Leitor inativo não pode realizar reservas");
                }

                circulacaoRepository.travarLivroParaAlterarReservas(livroId);
                Livro livro = livroService.buscarEntidade(livroId);

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
                                List.of(StatusReserva.ATIVA, StatusReserva.DISPONIVEL));

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

                return ReservaResponse.from(salva, posicaoFila);
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
                circulacaoRepository.travarLivroDaReserva(reservaId);
                Reserva reserva = reservaRepository.findById(reservaId)
                                .orElseThrow(() -> new RecursoNaoEncontradoException(
                                                "Reserva não encontrada"));

                StatusReserva status = reserva.getStatus();
                if (status != StatusReserva.ATIVA && status != StatusReserva.DISPONIVEL) {
                        throw new ConflitoException("A reserva já foi encerrada");
                }

                Exemplar exemplar = reserva.getExemplar();
                var separado = status == StatusReserva.DISPONIVEL && exemplar != null
                                ? circulacaoRepository.travarExemplar(exemplar.getId()) : null;
                reserva.setStatus(StatusReserva.CANCELADA);
                reserva.setExemplar(null);
                // Retira a alocação anterior antes de vincular o exemplar a outra reserva.
                reservaRepository.saveAndFlush(reserva);
                if (separado != null && "reservado".equals(separado.status())) {
                        circulacaoService.encaminharExemplarCancelado(separado);
                }

                return new ReservaCancelamentoResponse(
                                reserva.getId(),
                                reserva.getStatus().valor());
        }

        @Transactional
        public ReservaResponse criarParaLeitor(Leitor leitor, Long livroId) {
                return criarReserva(leitor, livroId);
        }

}
