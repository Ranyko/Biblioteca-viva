package br.com.bibliotecaviva.dto;

import java.time.OffsetDateTime;

import br.com.bibliotecaviva.model.Reserva;

public record ReservaResponse(

        Long id,
        Long leitorId,
        String leitorNome,
        Long livroId,
        String livroTitulo,
        String status,
        OffsetDateTime dataSolicitacao,
        Integer posicaoFila) {

    public static ReservaResponse from(
            Reserva reserva,
            Integer posicaoFila) {

        return new ReservaResponse(
                reserva.getId(),
                reserva.getLeitor().getId(),
                reserva.getLeitor().getUsuario().getNome(),
                reserva.getLivro().getId(),
                reserva.getLivro().getTitulo(),
                reserva.getStatus().valor(),
                reserva.getDataSolicitacao(),
                posicaoFila);
    }
}