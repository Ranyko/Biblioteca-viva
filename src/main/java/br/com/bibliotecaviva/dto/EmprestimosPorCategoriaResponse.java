package br.com.bibliotecaviva.dto;

public record EmprestimosPorCategoriaResponse(
        Long categoriaId,
        String categoria,
        Long totalEmprestimos) {
}