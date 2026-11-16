package br.com.bibliotecaviva.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NomeCadastroRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 160, message = "O nome deve ter no máximo 160 caracteres") String nome) { }
