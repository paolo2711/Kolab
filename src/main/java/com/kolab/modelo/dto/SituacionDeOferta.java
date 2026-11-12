package com.kolab.modelo.dto;

import com.kolab.modelo.entidad.EstadoOferta;
import com.kolab.modelo.entidad.EstadoSolicitud;

/**
 * Qué está pasando con una oferta propia. No siempre es lo que dice su estado: una oferta enviada a
 * una solicitud que se canceló ya no espera nada.
 */
public enum SituacionDeOferta {

    ESPERANDO("Esperando que quien pidió elija"),
    EN_CURSO("Te eligieron, el servicio está en curso"),
    CERRADO("Te eligieron y el servicio ya se cerró"),
    NO_ELEGIDA("No te eligieron"),
    CANCELADA("Quien pidió canceló la solicitud");

    private final String texto;

    SituacionDeOferta(String texto) {
        this.texto = texto;
    }

    public String getTexto() {
        return texto;
    }

    // aceptar abre el servicio y cerrarlo cierra la solicitud, así que con los dos estados alcanza
    public static SituacionDeOferta de(EstadoOferta oferta, EstadoSolicitud solicitud) {
        if (solicitud == EstadoSolicitud.CANCELADA && oferta != EstadoOferta.ACEPTADA) {
            return CANCELADA;
        }
        return switch (oferta) {
            case ENVIADA -> ESPERANDO;
            case RECHAZADA -> NO_ELEGIDA;
            case ACEPTADA -> solicitud == EstadoSolicitud.CERRADA ? CERRADO : EN_CURSO;
        };
    }
}
