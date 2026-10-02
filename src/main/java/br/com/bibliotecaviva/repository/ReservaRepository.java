package br.com.bibliotecaviva.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.bibliotecaviva.model.Reserva;
import br.com.bibliotecaviva.model.StatusReserva;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    boolean existsByLeitorIdAndLivroIdAndStatusIn(
            Long leitorId,
            Long livroId,
            List<StatusReserva> status);

    List<Reserva> findByLivroIdAndStatusOrderByDataSolicitacaoAsc(
            Long livroId,
            StatusReserva status);

    long countByLivroIdAndStatus(
            Long livroId,
            StatusReserva status);

    List<Reserva> findByLivroIdAndStatusInOrderByDataSolicitacaoAsc(
            Long livroId,
            List<StatusReserva> status);
}