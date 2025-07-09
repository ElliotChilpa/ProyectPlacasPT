package com.placaspt.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EventoEstado {
    INGRESO,
    SALIDA,
    DENEGADO;

    /**
     * Este método se usa para deserializar la propiedad "gate" del JSON.
     * Convierte la cadena (case-insensitive) a INGRESO o SALIDA.
     * Si viene algo desconocido, devuelves null (o podrías devolver DENEGADO si prefieres).
     */
    @JsonCreator
    public static EventoEstado fromString(String s) {
        if (s == null) return null;
        try {
            return EventoEstado.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;  // o: return DENEGADO;
        }
    }

    /**
     * (Opcional) si luego serializas este enum de vuelta, siempre escribirás
     * el nombre en mayúsculas:
     */
    @JsonValue
    public String toValue() {
        return this.name();
    }
}
