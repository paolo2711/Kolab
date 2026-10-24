package com.kolab.config;

import com.kolab.common.BitacoraDeSeguridad;
import com.kolab.usuario.ControlDeIntentos;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
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
    private final ControlDeIntentos intentos;

    EventosDeIngreso(BitacoraDeSeguridad bitacora, ControlDeIntentos intentos) {
        this.bitacora = bitacora;
        this.intentos = intentos;
    }

    @EventListener
    void entro(AuthenticationSuccessEvent evento) {
        String email = evento.getAuthentication().getName();
        intentos.entro(email);
        bitacora.ingreso(email, ipDe(evento.getAuthentication().getDetails()));
    }

    @EventListener
    void noEntro(AbstractAuthenticationFailureEvent evento) {
        String email = evento.getAuthentication().getName();
        String ip = ipDe(evento.getAuthentication().getDetails());
        bitacora.ingresoFallido(email, ip, evento.getException().getClass().getSimpleName());
        // ni un bloqueo ya puesto ni una cuenta suspendida (que dio la clave bien) cuentan como intento
        if (!(evento.getException() instanceof LockedException || evento.getException() instanceof DisabledException)
                && intentos.fallo(email)) {
            bitacora.cuentaBloqueada(email, ip, intentos.minutosDeBloqueo());
        }
    }

    private String ipDe(Object detalles) {
        return detalles instanceof WebAuthenticationDetails web ? web.getRemoteAddress() : "desconocida";
    }
}
