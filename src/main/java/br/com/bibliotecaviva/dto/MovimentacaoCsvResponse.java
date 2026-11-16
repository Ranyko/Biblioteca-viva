package br.com.bibliotecaviva.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record MovimentacaoCsvResponse(
        String tituloObra,
        String nomeLeitor,
        OffsetDateTime dataRetirada,
        LocalDate dataPrevistaDevolucao,
        OffsetDateTime dataDevolucao) {
}