package br.com.bibliotecaviva.dto;

import jakarta.validation.constraints.*;

public record ExemplarUpdateRequest(
        @NotBlank @Size(max = 50) String codigoTombo,
        @NotBlank @Pattern(regexp = "novo|bom|regular|danificado",
                message = "Use novo, bom, regular ou danificado") String estadoConservacao) { }
