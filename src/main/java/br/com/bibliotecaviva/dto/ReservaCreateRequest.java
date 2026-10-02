package br.com.bibliotecaviva.dto;

import jakarta.validation.constraints.NotNull;

public record ReservaCreateRequest(

        @NotNull(message = "O leitor é obrigatório")
        Long leitorId,

        @NotNull(message = "O livro é obrigatório")
        Long livroId) {

}