package br.com.bibliotecaviva.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusReserva {

    ATIVA("ativa"),
    DISPONIVEL("disponivel"),
    ATENDIDA("atendida"),
    CANCELADA("cancelada"),
    EXPIRADA("expirada");

    private final String valor;

    StatusReserva(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String valor() {
        return valor;
    }

    @JsonCreator
    public static StatusReserva from(String valor) {
        for (StatusReserva status : values()) {
            if (status.valor.equalsIgnoreCase(valor)
                    || status.name().equalsIgnoreCase(valor)) {
                return status;
            }
        }

        throw new IllegalArgumentException(
                "Status de reserva inválido: " + valor);
    }
}