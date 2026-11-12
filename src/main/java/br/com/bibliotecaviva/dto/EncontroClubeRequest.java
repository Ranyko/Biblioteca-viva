package br.com.bibliotecaviva.dto;

import java.time.OffsetDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EncontroClubeRequest(
        @NotNull @Positive Long livroId,
        @NotNull OffsetDateTime dataHora,
        @NotBlank @Size(max = 1000) String localOuAcesso) { }
