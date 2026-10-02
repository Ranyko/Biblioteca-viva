package br.com.bibliotecaviva.dto;

import java.util.List;

public record PaginaResponse<T>(List<T> itens, int pagina, int tamanho, long total) { }
