package br.com.bibliotecaviva.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PerfilUsuarioConverter implements AttributeConverter<PerfilUsuario, String> {

    @Override
    public String convertToDatabaseColumn(PerfilUsuario perfil) {
        return perfil == null ? null : perfil.valor();
    }

    @Override
    public PerfilUsuario convertToEntityAttribute(String valor) {
        return valor == null ? null : PerfilUsuario.from(valor);
    }
}
