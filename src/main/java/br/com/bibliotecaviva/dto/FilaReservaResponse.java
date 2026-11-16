package br.com.bibliotecaviva.dto;

public record FilaReservaResponse(

        Integer posicao,
        Long reservaId,
        Long leitorId,
        String leitorNome) {
}