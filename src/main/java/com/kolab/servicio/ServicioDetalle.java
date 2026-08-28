package com.kolab.servicio;

import com.kolab.common.Hito;
import com.kolab.mensaje.MensajeResumen;
import com.kolab.perfil.Persona;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public record ServicioDetalle(Long id,
                              Long idSolicitud,
                              String titulo,
                              String categoria,
                              Persona contraparte,
                              BigDecimal calificacionContraparte,
                              int serviciosContraparte,
                              String miembroDesde,
                              BigDecimal montoFinal,
                              BigDecimal comision,
                              EstadoServicio estado,
                              String fechaInicio,
                              List<MensajeResumen> mensajes) {

    public BigDecimal getNeto() {
        return montoFinal.subtract(comision);
    }

    public boolean enCurso() {
        return estado == EstadoServicio.EN_CURSO;
    }

    public List<Hito> hitos() {
        List<Hito> linea = new ArrayList<>();
        linea.add(Hito.cumplido("Oferta aceptada", "Se acordó S/ " + montoFinal + " con " + contraparte.nombre()));
        linea.add(Hito.cumplido("Comisión registrada", "S/ " + comision + " sobre el monto acordado"));
        if (enCurso()) {
            linea.add(Hito.enCurso("Coordinando", "Se ponen de acuerdo por el chat"));
            linea.add(Hito.pendiente("Cierre y calificación", "Cuando el servicio termine"));
        } else {
            linea.add(Hito.cumplido("Servicio ejecutado", fechaInicio));
            linea.add(Hito.cumplido("Cerrado y calificado", "Ambos se calificaron"));
        }
        return linea;
    }
}
