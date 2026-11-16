package br.com.bibliotecaviva.dto;

import java.time.OffsetDateTime;

public record ExemplarResponse(long id, long livroId, String codigoTombo,
        String estadoConservacao, String status, OffsetDateTime cadastradoEm) { }
