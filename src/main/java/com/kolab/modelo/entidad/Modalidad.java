package com.kolab.modelo.entidad;

public enum Modalidad {

    PRESENCIAL("Presencial"),
    VIRTUAL("Virtual");

    private final String etiqueta;

    Modalidad(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
