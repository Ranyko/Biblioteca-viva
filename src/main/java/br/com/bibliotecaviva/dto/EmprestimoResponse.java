package br.com.bibliotecaviva.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record EmprestimoResponse(Long id, Long leitorId, Long exemplarId,
        Long atendenteRetiradaId, Long atendenteDevolucaoId, OffsetDateTime dataRetirada,
        LocalDate dataPrevistaDevolucao, OffsetDateTime dataDevolucao) { }
