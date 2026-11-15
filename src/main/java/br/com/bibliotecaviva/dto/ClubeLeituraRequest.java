package br.com.bibliotecaviva.dto;

import java.util.List;
import java.util.ArrayList;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ClubeLeituraRequest(
        @NotBlank @Size(max = 160) String nome,
        String descricao,
        @NotNull @Positive @Max(32767) Integer capacidadeMaxima,
        @Size(max = 80) String periodicidade,
        @Size(max = 50) List<@NotNull @Positive Long> categoriaIds) {
    public ClubeLeituraRequest {
        categoriaIds = categoriaIds == null ? List.of() : new ArrayList<>(categoriaIds);
    }
}
