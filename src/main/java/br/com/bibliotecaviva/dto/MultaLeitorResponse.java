package br.com.bibliotecaviva.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record MultaLeitorResponse(
        Long id,
        BigDecimal valorTotal,
        String situacao,
        OffsetDateTime geradaEm,
        OffsetDateTime pagaEm) {
}
