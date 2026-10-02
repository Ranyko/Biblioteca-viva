package br.com.bibliotecaviva.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusExemplar {

    DISPONIVEL("disponivel"),
    EMPRESTADO("emprestado"),
    RESERVADO("reservado"),
    INATIVO("inativo");

    private final String valor;

    StatusExemplar(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String valor() {
        return valor;
    }

    @JsonCreator
    public static StatusExemplar from(String valor) {
        for (StatusExemplar status : values()) {
            if (status.valor.equalsIgnoreCase(valor)
                    || status.name().equalsIgnoreCase(valor)) {
                return status;
            }
        }

        throw new IllegalArgumentException(
                "Status de exemplar inválido: " + valor);
    }
}