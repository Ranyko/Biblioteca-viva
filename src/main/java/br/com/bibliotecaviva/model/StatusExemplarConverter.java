package br.com.bibliotecaviva.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusExemplarConverter
        implements AttributeConverter<StatusExemplar, String> {

    @Override
    public String convertToDatabaseColumn(StatusExemplar status) {
        return status == null ? null : status.valor();
    }

    @Override
    public StatusExemplar convertToEntityAttribute(String valor) {
        return valor == null ? null : StatusExemplar.from(valor);
    }
}