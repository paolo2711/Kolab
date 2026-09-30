package com.kolab.solicitud;

import com.kolab.perfil.Persona;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

/**
 * Una fila de solicitud tal como sale en Explorar, Inicio y la página de categoría. {@code foto} es
 * {@code null} si la categoría no tiene foto, y {@code kilometros} si no se sabe la distancia.
 */
public record SolicitudResumen(Long id,
                               String titulo,
                               Long idCategoria,
                               String categoria,
                               String icono,
                               String foto,
                               Modalidad modalidad,
                               String distrito,
                               BigDecimal precioPropuesto,
                               BigDecimal mejorOferta,
                               List<Persona> ofertantes,
                               String publicada,
                               boolean directa,
                               Double kilometros) {

    public boolean tieneFoto() {
        return foto != null;
    }

    public boolean tieneOfertas() {
        return !ofertantes.isEmpty();
    }

    public int getOfertas() {
        return ofertantes.size();
    }

    public boolean sabemosLaDistancia() {
        return kilometros != null;
    }

    public String getDistancia() {
        if (kilometros == null) {
            return null;
        }
        if (kilometros < 0.1) {
            return "menos de 100 m";
        }
        if (kilometros < 1) {
            return Math.round(kilometros * 1000) + " m";
        }
        return String.format(Locale.ROOT, "%.1f km", kilometros);
    }

    public String getUbicacion() {
        if (modalidad == Modalidad.VIRTUAL || distrito == null) {
            return modalidad.getEtiqueta();
        }
        return modalidad.getEtiqueta() + " · " + distrito;
    }
}
