package br.com.bibliotecaviva.dto;

import java.time.OffsetDateTime;

public record AvaliacaoResponse(
        long id,
        long livroId,
        int nota,
        String resenha,
        OffsetDateTime atualizadaEm) {
}
