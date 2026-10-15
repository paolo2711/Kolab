package com.kolab.config;

import com.kolab.common.BitacoraDeSeguridad;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

/**
 * Escucha lo que publica Spring Security al entrar o al fallar el ingreso y lo manda a la bitácora.
 * Se hace con eventos y no dentro del controlador porque el ingreso lo resuelve el filtro de
 * Spring, que nunca pasa por un método nuestro.
 */
@Component
class EventosDeIngreso {

    private final BitacoraDeSeguridad bitacora;

    EventosDeIngreso(BitacoraDeSeguridad bitacora) {
        this.bitacora = bitacora;
    }

    @EventListener
    void entro(AuthenticationSuccessEvent evento) {
        bitacora.ingreso(evento.getAuthentication().getName(), ipDe(evento.getAuthentication().getDetails()));
    }

    @EventListener
    void noEntro(AbstractAuthenticationFailureEvent evento) {
        bitacora.ingresoFallido(evento.getAuthentication().getName(),
                ipDe(evento.getAuthentication().getDetails()),
                evento.getException().getClass().getSimpleName());
    }

    private String ipDe(Object detalles) {
        return detalles instanceof WebAuthenticationDetails web ? web.getRemoteAddress() : "desconocida";
    }
}
