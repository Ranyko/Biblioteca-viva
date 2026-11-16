package br.com.bibliotecaviva.dto;

public record SugestaoLivroResponse(
        Long livroId,
        String titulo,
        String descricao,
        String categoria,
        Long exemplaresDisponiveis) {
}