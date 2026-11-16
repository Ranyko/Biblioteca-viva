package br.com.bibliotecaviva.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record EmprestimoLeitorResponse(
        Long id,
        String livroTitulo,
        OffsetDateTime dataRetirada,
        LocalDate dataPrevistaDevolucao,
        OffsetDateTime dataDevolucao,
        String situacao) {
}
