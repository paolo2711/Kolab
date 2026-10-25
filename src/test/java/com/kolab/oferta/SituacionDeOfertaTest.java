package com.kolab.oferta;

import static org.assertj.core.api.Assertions.assertThat;

import com.kolab.solicitud.EstadoSolicitud;
import org.junit.jupiter.api.Test;

class SituacionDeOfertaTest {

    @Test
    void enviadaASolicitudAbiertaEstaEsperando() {
        assertThat(SituacionDeOferta.de(EstadoOferta.ENVIADA, EstadoSolicitud.ABIERTA))
                .isEqualTo(SituacionDeOferta.ESPERANDO);
    }

    @Test
    void aceptadaEsElServicioEnCursoHastaQueSeCierra() {
        assertThat(SituacionDeOferta.de(EstadoOferta.ACEPTADA, EstadoSolicitud.EN_CURSO))
                .isEqualTo(SituacionDeOferta.EN_CURSO);
        assertThat(SituacionDeOferta.de(EstadoOferta.ACEPTADA, EstadoSolicitud.CERRADA))
                .isEqualTo(SituacionDeOferta.CERRADO);
    }

    @Test
    void laQueNoEligieronLoDiceAunqueLaSolicitudSigaEnCurso() {
        assertThat(SituacionDeOferta.de(EstadoOferta.RECHAZADA, EstadoSolicitud.EN_CURSO))
                .isEqualTo(SituacionDeOferta.NO_ELEGIDA);
        assertThat(SituacionDeOferta.de(EstadoOferta.RECHAZADA, EstadoSolicitud.ABIERTA))
                .isEqualTo(SituacionDeOferta.NO_ELEGIDA);
    }

    @Test
    void siCancelaronLaSolicitudYaNoEsperaNada() {
        assertThat(SituacionDeOferta.de(EstadoOferta.ENVIADA, EstadoSolicitud.CANCELADA))
                .isEqualTo(SituacionDeOferta.CANCELADA);
        assertThat(SituacionDeOferta.de(EstadoOferta.RECHAZADA, EstadoSolicitud.CANCELADA))
                .isEqualTo(SituacionDeOferta.CANCELADA);
    }
}
