package br.com.bibliotecaviva.dto;

import java.util.List;
import jakarta.validation.constraints.*;

public record LivroRequest(
        @NotBlank @Size(max = 200) String titulo,
        @NotBlank @Size(max = 30) String isbn,
        @Min(1000) @Max(32767) Integer anoPublicacao,
        @Size(max = 10000) String descricao,
        @NotNull Boolean ativo,
        @NotNull @Size(max = 50) List<@NotNull @Positive Long> autorIds,
        @NotNull @Size(max = 50) List<@NotNull @Positive Long> categoriaIds) {
    public LivroRequest {
        ativo = ativo == null ? true : ativo;
        autorIds = autorIds == null ? List.of() : autorIds;
        categoriaIds = categoriaIds == null ? List.of() : categoriaIds;
    }
}
