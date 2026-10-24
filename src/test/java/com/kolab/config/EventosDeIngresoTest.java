package com.kolab.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.kolab.common.BitacoraDeSeguridad;
import com.kolab.usuario.ControlDeIntentos;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationFailureDisabledEvent;

class EventosDeIngresoTest {

    private final ControlDeIntentos intentos = new ControlDeIntentos(5, 15);
    private final EventosDeIngreso eventos = new EventosDeIngreso(Mockito.mock(BitacoraDeSeguridad.class), intentos);

    @Test
    void unaClaveEquivocadaCuentaComoIntento() {
        for (int i = 0; i < 5; i++) {
            eventos.noEntro(new AuthenticationFailureBadCredentialsEvent(intento(), new BadCredentialsException("mal")));
        }

        assertThat(intentos.estaBloqueado("luis@kolab.pe")).isTrue();
    }

    @Test
    void unaCuentaSuspendidaNoSeBloqueaPorInsistir() {
        for (int i = 0; i < 5; i++) {
            eventos.noEntro(new AuthenticationFailureDisabledEvent(intento(), new DisabledException("suspendida")));
        }

        assertThat(intentos.estaBloqueado("luis@kolab.pe")).isFalse();
    }

    private UsernamePasswordAuthenticationToken intento() {
        return UsernamePasswordAuthenticationToken.unauthenticated("luis@kolab.pe", "kolab1234");
    }
}
