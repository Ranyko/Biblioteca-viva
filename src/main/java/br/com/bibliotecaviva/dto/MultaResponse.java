package br.com.bibliotecaviva.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record MultaResponse(Long id, Long emprestimoId, Long leitorId, Long exemplarId,
        int diasAtraso, BigDecimal valorDiarioAplicado, BigDecimal valorMaximoAplicado,
        BigDecimal valorTotal, String status, OffsetDateTime geradaEm, OffsetDateTime pagaEm) { }
