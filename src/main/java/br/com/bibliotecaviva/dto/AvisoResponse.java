package br.com.bibliotecaviva.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record AvisoResponse(
        Long id,
        String tipo,
        String titulo,
        String mensagem,
        String referenciaTipo,
        Long referenciaId,
        LocalDate diaReferencia,
        OffsetDateTime criadoEm,
        OffsetDateTime lidoEm
) {
    public boolean isLido() {
        return lidoEm != null;
    }
}