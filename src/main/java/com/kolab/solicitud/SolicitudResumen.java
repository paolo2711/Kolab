package com.kolab.solicitud;

import com.kolab.common.Foto;
import com.kolab.perfil.Persona;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

public record SolicitudResumen(Long id,
                               String titulo,
                               Long idCategoria,
                               String categoria,
                               String icono,
                               String tema,
                               Modalidad modalidad,
                               String distrito,
                               BigDecimal precioPropuesto,
                               BigDecimal mejorOferta,
                               List<Persona> ofertantes,
                               String publicada,
                               Double latitud,
                               Double longitud,
                               Double kilometros) {

    public SolicitudResumen a(double kilometros) {
        return new SolicitudResumen(id, titulo, idCategoria, categoria, icono, tema, modalidad,
                distrito, precioPropuesto, mejorOferta, ofertantes, publicada, latitud, longitud,
                kilometros);
    }

    public String foto(int ancho, int alto) {
        return Foto.de(tema, ancho, alto);
    }

    public boolean tieneOfertas() {
        return !ofertantes.isEmpty();
    }

    public int getOfertas() {
        return ofertantes.size();
    }

    public boolean enElMapa() {
        return latitud != null && longitud != null;
    }

    public boolean sabemosLaDistancia() {
        return kilometros != null;
    }

    public String getDistancia() {
        if (kilometros == null) {
            return null;
        }
        if (kilometros < 1) {
            return Math.round(kilometros * 1000) + " m";
        }
        return String.format(Locale.ROOT, "%.1f km", kilometros);
    }

    public String getUbicacion() {
        return modalidad == Modalidad.VIRTUAL ? modalidad.getEtiqueta() : modalidad.getEtiqueta() + " · " + distrito;
    }
}
