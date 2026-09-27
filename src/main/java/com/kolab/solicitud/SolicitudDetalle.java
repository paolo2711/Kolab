package com.kolab.solicitud;

import com.kolab.common.Hito;
import com.kolab.oferta.OfertaResumen;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public record SolicitudDetalle(Long id,
                               String titulo,
                               String descripcion,
                               Long idCategoria,
                               String categoria,
                               Modalidad modalidad,
                               String distrito,
                               LocalDate fechaDeseada,
                               BigDecimal precioPropuesto,
                               String publicada,
                               EstadoSolicitud estado,
                               List<OfertaResumen> ofertas) {

    // los hitos describen en qué va esta solicitud. no dicen dónde está el usuario.
    public List<Hito> hitos() {
        List<Hito> linea = new ArrayList<>();
        linea.add(Hito.cumplido("Publicada", "Con tu precio de S/ " + precioPropuesto + ", " + publicada));

        if (ofertas.isEmpty()) {
            linea.add(Hito.enCurso("Esperando ofertas", "Los expertos de " + categoria + " ya la ven"));
            linea.add(Hito.pendiente("Aceptas una oferta", "Eliges con quién trabajar"));
        } else {
            linea.add(Hito.cumplido(ofertas.size() + " ofertas recibidas",
                    "Desde S/ " + ofertas.stream().map(OfertaResumen::monto).min(BigDecimal::compareTo).orElseThrow()));
            if (estado == EstadoSolicitud.ABIERTA) {
                linea.add(Hito.enCurso("Te toca elegir", "Acepta una oferta para cerrar el trato"));
            } else {
                linea.add(Hito.cumplido("Oferta aceptada", "El servicio quedó registrado con su comisión"));
            }
        }

        if (estado == EstadoSolicitud.CERRADA) {
            linea.add(Hito.cumplido("Cerrada y calificada", "El servicio terminó"));
        } else {
            linea.add(Hito.pendiente("Cierre y calificación", "Cuando el servicio termine"));
        }
        return linea;
    }

    public int pasoDelCliente() {
        return switch (estado) {
            case CERRADA -> 4;
            case EN_CURSO -> 3;
            case ABIERTA -> ofertas.isEmpty() ? 2 : 3;
        };
    }

    public int pasoDelExperto() {
        return switch (estado) {
            case CERRADA -> 4;
            case EN_CURSO -> 3;
            case ABIERTA -> 2;
        };
    }

    public boolean admiteOfertas() {
        return estado == EstadoSolicitud.ABIERTA;
    }

    public OfertaResumen ofertaAceptada() {
        return ofertas.isEmpty() ? null : ofertas.get(0);
    }
}
