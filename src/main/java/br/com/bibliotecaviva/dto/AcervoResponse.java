package br.com.bibliotecaviva.dto;

import java.util.List;

public record AcervoResponse(Long id, String titulo, String isbn, Integer anoPublicacao,
        String descricao, List<String> autores, List<String> categorias, long exemplaresDisponiveis) { }
