package com.kolab.usuario;

public enum TipoPerfil {

    CLIENTE("Cliente", "Publica solicitudes y pone el precio"),
    EXPERTO("Experto", "Revisa solicitudes y hace ofertas"),
    ADMIN("Administrador", "Gestiona la plataforma");

    private final String etiqueta;
    private final String detalle;

    TipoPerfil(String etiqueta, String detalle) {
        this.etiqueta = etiqueta;
        this.detalle = detalle;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getDetalle() {
        return detalle;
    }

    public String getRol() {
        return "ROLE_" + name();
    }
}
