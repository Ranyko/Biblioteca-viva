package br.com.bibliotecaviva.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/** Painel do atendente: empréstimos atrasados + reservas com prazo de retirada em ≤ 1 dia. */
public record PendenciasResponse(
        List<Atrasado> atrasados,
        List<ReservaExpirando> reservasExpirando) {

    public record Atrasado(
            long emprestimoId,
            String leitorNome,
            String livroTitulo,
            LocalDate dataPrevistaDevolucao,
            long diasAtraso) {
    }

    public record ReservaExpirando(
            long reservaId,
            String leitorNome,
            String livroTitulo,
            OffsetDateTime dataLimiteRetirada) {
    }
}