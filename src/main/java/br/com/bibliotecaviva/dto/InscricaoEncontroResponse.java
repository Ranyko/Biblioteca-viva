package br.com.bibliotecaviva.dto;

import java.time.OffsetDateTime;

public record InscricaoEncontroResponse(
        long id,
        long encontroId,
        long leitorId,
        String status,
        OffsetDateTime inscritoEm,
        OffsetDateTime canceladoEm,
        Boolean presente,
        OffsetDateTime presencaRegistradaEm) { }
