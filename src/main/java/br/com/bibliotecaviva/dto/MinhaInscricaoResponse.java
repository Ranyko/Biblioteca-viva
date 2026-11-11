package br.com.bibliotecaviva.dto;

import java.time.OffsetDateTime;

public record MinhaInscricaoResponse(
        long id,
        long clubeId,
        String clubeNome,
        long encontroId,
        long livroId,
        String livroTitulo,
        OffsetDateTime dataHora,
        String localOuAcesso,
        String statusEncontro,
        String statusInscricao,
        OffsetDateTime inscritoEm,
        OffsetDateTime canceladoEm,
        Boolean presente,
        OffsetDateTime presencaRegistradaEm) { }
