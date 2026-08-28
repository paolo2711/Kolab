package com.kolab.solicitud;

import com.kolab.common.Foto;
import java.math.BigDecimal;

public record MiSolicitud(Long id,
                          String titulo,
                          String categoria,
                          String icono,
                          String tema,
                          Modalidad modalidad,
                          String distrito,
                          BigDecimal precioPropuesto,
                          int ofertas,
                          BigDecimal mejorOferta,
                          int mensajesSinLeer,
                          EstadoSolicitud estado,
                          String publicada) {

    public String foto(int ancho, int alto) {
        return Foto.de(tema, ancho, alto);
    }

    public boolean tieneOfertas() {
        return ofertas > 0;
    }

    public boolean tieneMensajes() {
        return mensajesSinLeer > 0;
    }

    public int getPaso() {
        return switch (estado) {
            case CERRADA -> 4;
            case EN_CURSO -> 3;
            case ABIERTA -> ofertas == 0 ? 2 : 3;
        };
    }

    public String getResumenOfertas() {
        if (estado != EstadoSolicitud.ABIERTA) {
            return estado.getEtiqueta();
        }
        return ofertas == 0 ? "Todavía sin ofertas" : ofertas + " ofertas recibidas";
    }
}
