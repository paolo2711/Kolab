package com.kolab.solicitud;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class SolicitudTest {

    private final Solicitud recien = new Solicitud(null, null, "Clases de guitarra", "Desde cero",
            Modalidad.VIRTUAL, new BigDecimal("80"));

    @Test
    void esNuevaDuranteSusPrimeras48Horas() {
        LocalDateTime ahora = LocalDateTime.now();

        assertThat(recien.esNueva(ahora)).isTrue();
        assertThat(recien.esNueva(ahora.plusHours(47))).isTrue();
    }

    @Test
    void pasadasLas48HorasYaNoEsNueva() {
        assertThat(recien.esNueva(LocalDateTime.now().plusHours(49))).isFalse();
    }
}
