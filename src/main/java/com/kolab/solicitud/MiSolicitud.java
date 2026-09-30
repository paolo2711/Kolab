package com.kolab.solicitud;

import java.math.BigDecimal;

/**
 * Una solicitud propia en la lista de Mi actividad, con lo que hace falta para saber si hay que
 * hacer algo: ofertas recibidas, la más baja y mensajes sin leer.
 */
public record MiSolicitud(Long id,
                          String titulo,
                          String categoria,
                          String icono,
                          String foto,
                          Modalidad modalidad,
                          BigDecimal precioPropuesto,
                          long ofertas,
                          BigDecimal mejorOferta,
                          long mensajesSinLeer,
                          EstadoSolicitud estado,
                          String publicada) {

    public boolean tieneFoto() {
        return foto != null;
    }

    public boolean tieneOfertas() {
        return ofertas > 0;
    }

    public boolean tieneMensajes() {
        return mensajesSinLeer > 0;
    }

    public String getResumenOfertas() {
        if (estado != EstadoSolicitud.ABIERTA) {
            return estado.getEtiqueta();
        }
        if (ofertas == 0) {
            return "Todavía sin ofertas";
        }
        return ofertas == 1 ? "1 oferta recibida" : ofertas + " ofertas recibidas";
    }
}
