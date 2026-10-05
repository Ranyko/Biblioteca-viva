package br.com.bibliotecaviva.dto;

import jakarta.validation.constraints.NotNull;

public record ExemplarSituacaoRequest(@NotNull Boolean ativo) { }
