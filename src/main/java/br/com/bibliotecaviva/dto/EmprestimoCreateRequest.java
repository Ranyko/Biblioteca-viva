package br.com.bibliotecaviva.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EmprestimoCreateRequest(@NotNull @Positive Long leitorId,
        @NotNull @Positive Long exemplarId) { }
