package br.com.bibliotecaviva.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PerfilUsuario {
    LEITOR("leitor"),
    ATENDENTE("atendente"),
    ADMINISTRADOR("administrador");

    private final String valor;

    PerfilUsuario(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String valor() {
        return valor;
    }

    public String authority() {
        return "ROLE_" + name();
    }

    @JsonCreator
    public static PerfilUsuario from(String valor) {
        for (PerfilUsuario perfil : values()) {
            if (perfil.valor.equalsIgnoreCase(valor) || perfil.name().equalsIgnoreCase(valor)) {
                return perfil;
            }
        }
        throw new IllegalArgumentException("Perfil inválido: " + valor);
    }
}
