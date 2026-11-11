package br.com.bibliotecaviva.dto;

import jakarta.validation.constraints.NotNull;

public record PresencaEncontroRequest(@NotNull Boolean presente) { }
