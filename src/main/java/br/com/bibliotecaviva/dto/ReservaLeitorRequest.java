
package br.com.bibliotecaviva.dto;

import jakarta.validation.constraints.NotNull;

public record ReservaLeitorRequest(

        @NotNull(message = "O livro é obrigatório")
        Long livroId) {
}
