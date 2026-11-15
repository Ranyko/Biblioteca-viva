package br.com.bibliotecaviva.dto;

import java.time.OffsetDateTime;

public record AvisoPreferencias(
    boolean devidaAmanha,
    boolean vencido,
    boolean reservaDisponivel,
    boolean reservaExpirando,
    boolean reservaExpirada,
    boolean multaGerada,
    boolean filaPosicao,
        OffsetDateTime atualizadoEm
) {
}