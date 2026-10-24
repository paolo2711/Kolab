package com.kolab.solicitud;

import com.kolab.common.Hito;
import com.kolab.oferta.EstadoOferta;
import com.kolab.oferta.OfertaResumen;
import com.kolab.perfil.Persona;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Una solicitud con sus ofertas, tal como se ve al abrirla. {@code destinatario} es {@code null}
 * si está abierta a todos, e {@code idServicio} si todavía no se aceptó ninguna oferta.
 */
public record SolicitudDetalle(Long id,
                               String titulo,
                               String descripcion,
                               Long idCategoria,
                               String categoria,
                               String foto,
                               Modalidad modalidad,
                               String distrito,
                               LocalDate fechaDeseada,
                               BigDecimal precioPropuesto,
                               String publicada,
                               EstadoSolicitud estado,
                               Persona autor,
                               Persona destinatario,
                               List<OfertaResumen> ofertas,
                               Long idServicio) {

    public boolean esPropuestaDirecta() {
        return destinatario != null;
    }

    public boolean admiteOfertas() {
        return estado == EstadoSolicitud.ABIERTA;
    }

    public List<OfertaResumen> getEnJuego() {
        return ofertas.stream().filter(o -> o.estado() != EstadoOferta.RECHAZADA).toList();
    }

    // los hitos describen en qué va esta solicitud. no dicen dónde está el usuario.
    public List<Hito> hitos() {
        List<Hito> linea = new ArrayList<>();
        linea.add(Hito.cumplido("Publicada", "Con tu precio de S/ " + precioPropuesto + ", " + publicada));
        if (estado == EstadoSolicitud.CANCELADA) {
            linea.add(Hito.cumplido("Cancelada", "Ya no aparece en Explorar"));
            return linea;
        }

        List<OfertaResumen> enJuego = getEnJuego();
        if (ofertas.isEmpty()) {
            linea.add(Hito.enCurso("Esperando ofertas", esPropuestaDirecta()
                    ? "Le llegó solo a " + destinatario.nombre()
                    : "Quienes saben de " + categoria + " ya la ven"));
            linea.add(Hito.pendiente("Aceptas una oferta", "Eliges con quién trabajar"));
        } else if (estado == EstadoSolicitud.ABIERTA) {
            linea.add(Hito.cumplido(ofertas.size() == 1 ? "1 oferta recibida" : ofertas.size() + " ofertas recibidas",
                    enJuego.isEmpty() ? "Ninguna sigue en juego" : "Desde S/ " + enJuego.get(0).monto()));
            linea.add(Hito.enCurso("Te toca elegir", "Acepta una oferta para cerrar el trato"));
        } else {
            linea.add(Hito.cumplido("Oferta aceptada", "El servicio quedó registrado con su comisión"));
        }

        if (estado == EstadoSolicitud.CERRADA) {
            linea.add(Hito.cumplido("Cerrada y calificada", "El servicio terminó"));
        } else {
            linea.add(Hito.pendiente("Cierre y calificación", "Cuando el servicio termine"));
        }
        return linea;
    }
}
