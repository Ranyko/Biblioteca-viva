package br.com.bibliotecaviva.dto;

import java.time.OffsetDateTime;

public record ReservaLeitorResponse(
        Long id,
        String livroTitulo,
        String situacao,
        Integer posicaoFila,
        OffsetDateTime dataLimiteRetirada) {
}
