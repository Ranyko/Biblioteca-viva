package br.com.bibliotecaviva.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.bibliotecaviva.dto.EmprestimoLeitorResponse;
import br.com.bibliotecaviva.dto.MultaLeitorResponse;
import br.com.bibliotecaviva.dto.PainelLeitorResponse;
import br.com.bibliotecaviva.dto.ReservaLeitorResponse;
import br.com.bibliotecaviva.model.Reserva;
import br.com.bibliotecaviva.model.StatusReserva;
import br.com.bibliotecaviva.repository.PainelLeitorRepository;
import br.com.bibliotecaviva.repository.ReservaRepository;

@Service
public class PainelLeitorService {

    private final PainelLeitorRepository painelLeitorRepository;
    private final ReservaRepository reservaRepository;

    public PainelLeitorService(
            PainelLeitorRepository painelLeitorRepository,
            ReservaRepository reservaRepository) {
        this.painelLeitorRepository = painelLeitorRepository;
        this.reservaRepository = reservaRepository;
    }

    @Transactional(readOnly = true)
    public PainelLeitorResponse consultar(Long leitorId) {
        List<EmprestimoLeitorResponse> emprestimos =
                painelLeitorRepository.listarEmprestimos(leitorId);

        List<MultaLeitorResponse> multas =
                painelLeitorRepository.listarMultas(leitorId);

        List<ReservaLeitorResponse> reservas = new ArrayList<>();

        List<Reserva> reservasDoLeitor =
                reservaRepository.findByLeitorIdOrderByDataSolicitacaoDesc(
                        leitorId);

        for (Reserva reserva : reservasDoLeitor) {
            Integer posicaoFila = calcularPosicaoFila(reserva);

            reservas.add(new ReservaLeitorResponse(
                    reserva.getId(),
                    reserva.getLivro().getTitulo(),
                    reserva.getStatus().valor(),
                    posicaoFila,
                    reserva.getDataLimiteRetirada()
            ));
        }

        return new PainelLeitorResponse(emprestimos, reservas, multas);
    }

    private Integer calcularPosicaoFila(Reserva reserva) {
        if (reserva.getStatus() != StatusReserva.ATIVA) {
            return null;
        }

        List<Reserva> fila = reservaRepository
                .findByLivroIdAndStatusOrderByDataSolicitacaoAsc(
                        reserva.getLivro().getId(),
                        StatusReserva.ATIVA);

        for (int i = 0; i < fila.size(); i++) {
            if (fila.get(i).getId().equals(reserva.getId())) {
                return i + 1;
            }
        }

        return null;
    }
}
