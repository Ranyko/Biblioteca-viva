package br.com.bibliotecaviva.dto;

import java.time.OffsetDateTime;

public record ParticipanteEncontroResponse(
        long leitorId,
        String nome,
        String statusInscricao,
        OffsetDateTime inscritoEm,
        Boolean presente,
        OffsetDateTime presencaRegistradaEm) { }
