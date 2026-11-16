package br.com.bibliotecaviva.dto;

/** Contagem de avisos efetivamente criados pela varredura (idempotente: repetir no mesmo dia dá 0). */
public record VarreduraResponse(
        int devidaAmanha,
        int vencido,
        int reservaExpirando,
        int reservaExpirada,
        int filaPosicao,
        int total) {
}