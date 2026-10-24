package com.kolab.usuario;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ControlDeIntentosTest {

    private final ControlDeIntentos control = new ControlDeIntentos(5, 15);

    @Test
    void alQuintoFalloSeguidoBloquea() {
        for (int i = 1; i < 5; i++) {
            assertThat(control.fallo("experto@kolab.pe")).isFalse();
            assertThat(control.estaBloqueado("experto@kolab.pe")).isFalse();
        }

        assertThat(control.fallo("experto@kolab.pe")).isTrue();
        assertThat(control.estaBloqueado("experto@kolab.pe")).isTrue();
    }

    @Test
    void unIngresoCorrectoReiniciaLaCuenta() {
        control.fallo("experto@kolab.pe");
        control.fallo("experto@kolab.pe");
        control.fallo("experto@kolab.pe");
        control.fallo("experto@kolab.pe");

        control.entro("experto@kolab.pe");

        assertThat(control.fallo("experto@kolab.pe")).isFalse();
        assertThat(control.estaBloqueado("experto@kolab.pe")).isFalse();
    }

    @Test
    void elBloqueoEsDeUnaCuentaYNoDeTodas() {
        for (int i = 0; i < 5; i++) {
            control.fallo("experto@kolab.pe");
        }

        assertThat(control.estaBloqueado("experto@kolab.pe")).isTrue();
        assertThat(control.estaBloqueado("cliente@kolab.pe")).isFalse();
    }

    @Test
    void elCorreoSeComparaSinEspaciosNiMayusculas() {
        for (int i = 0; i < 5; i++) {
            control.fallo("  Experto@Kolab.PE  ");
        }

        assertThat(control.estaBloqueado("experto@kolab.pe")).isTrue();
    }
}
