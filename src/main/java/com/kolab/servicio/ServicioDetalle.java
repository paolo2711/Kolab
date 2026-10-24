package com.kolab.servicio;

import com.kolab.common.Hito;
import com.kolab.mensaje.MensajeResumen;
import com.kolab.perfil.Persona;
import com.kolab.perfil.Reputacion;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Un servicio visto por una de sus dos partes. {@code pido} dice de qué lado está quien mira: lo
 * cierra quien lo pidió y después cada uno califica al otro.
 */
public record ServicioDetalle(Long id,
                              Long idOferta,
                              Long idSolicitud,
                              String titulo,
                              String categoria,
                              Persona contraparte,
                              Reputacion reputacionContraparte,
                              String miembroDesde,
                              BigDecimal montoFinal,
                              BigDecimal comision,
                              EstadoServicio estado,
                              String fechaInicio,
                              String fechaCierre,
                              boolean pido,
                              boolean yaCalifique,
                              List<MensajeResumen> mensajes) {

    public BigDecimal getNeto() {
        return montoFinal.subtract(comision);
    }

    public boolean enCurso() {
        return estado == EstadoServicio.EN_CURSO;
    }

    public boolean puedoCerrar() {
        return enCurso() && pido;
    }

    public boolean puedoCalificar() {
        return !enCurso() && !yaCalifique;
    }

    public List<Hito> hitos() {
        List<Hito> linea = new ArrayList<>();
        linea.add(Hito.cumplido("Oferta aceptada", "Se acordó S/ " + montoFinal + " con " + contraparte.nombre()));
        linea.add(Hito.cumplido("Comisión registrada", "S/ " + comision + " sobre el monto acordado"));
        if (enCurso()) {
            linea.add(Hito.enCurso("Coordinando", "Se ponen de acuerdo por el chat"));
            linea.add(Hito.pendiente("Cierre y calificación", pido
                    ? "Lo cierras tú cuando se haya hecho"
                    : "Lo cierra quien pidió el servicio"));
        } else {
            linea.add(Hito.cumplido("Servicio cerrado", fechaCierre));
            linea.add(yaCalifique
                    ? Hito.cumplido("Calificaste", "Tu nota ya está en su perfil")
                    : Hito.enCurso("Te toca calificar", "Tu nota entra en su promedio"));
        }
        return linea;
    }
}
