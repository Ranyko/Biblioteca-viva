package br.com.bibliotecaviva.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record StatusClubeLeituraRequest(
        @NotBlank @Pattern(regexp = "ativo|encerrado") String status) { }
