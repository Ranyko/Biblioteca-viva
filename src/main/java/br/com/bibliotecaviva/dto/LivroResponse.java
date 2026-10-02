package br.com.bibliotecaviva.dto;

import java.util.List;

public record LivroResponse(long id, String titulo, String isbn, Integer anoPublicacao,
        String descricao, boolean ativo, List<ReferenciaResponse> autores,
        List<ReferenciaResponse> categorias) { }
