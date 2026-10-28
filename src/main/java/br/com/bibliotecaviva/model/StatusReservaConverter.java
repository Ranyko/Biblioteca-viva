package br.com.bibliotecaviva.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusReservaConverter
        implements AttributeConverter<StatusReserva, String> {

    @Override
    public String convertToDatabaseColumn(StatusReserva status) {
        return status == null ? null : status.valor();
    }

    @Override
    public StatusReserva convertToEntityAttribute(String valor) {
        return valor == null ? null : StatusReserva.from(valor);
    }
}