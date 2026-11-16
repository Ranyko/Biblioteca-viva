package br.com.bibliotecaviva.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record RenovacaoResponse(Long id, Long emprestimoId, int numeroRenovacao,
        int limiteRenovacoesSnapshot, LocalDate dataPrevistaAnterior, LocalDate dataPrevistaNova,
        Long atendenteRenovacaoId, OffsetDateTime renovadaEm) { }
