package br.com.bibliotecaviva.dto;

import jakarta.validation.constraints.NotNull;

public record PreferenciaRequest(
        @NotNull(message = "Informe a preferência de devida_amanha") Boolean devidaAmanha,
        @NotNull(message = "Informe a preferência de vencido") Boolean vencido,
        @NotNull(message = "Informe a preferência de reserva_disponivel") Boolean reservaDisponivel,
        @NotNull(message = "Informe a preferência de reserva_expirando") Boolean reservaExpirando,
        @NotNull(message = "Informe a preferência de reserva_expirada") Boolean reservaExpirada,
        @NotNull(message = "Informe a preferência de multa_gerada") Boolean multaGerada,
        @NotNull(message = "Informe a preferência de fila_posicao") Boolean filaPosicao) {
}