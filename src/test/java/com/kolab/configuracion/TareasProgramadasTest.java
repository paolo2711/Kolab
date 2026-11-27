package com.kolab.configuracion;

import static org.assertj.core.api.Assertions.assertThat;

import com.kolab.servicio.RevisionDeSaludService;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.scheduling.annotation.Scheduled;

@Tag("unitaria")
class TareasProgramadasTest {

    @Test
    void cadaHoraRevisaLaSalud() throws Exception {
        Method tarea = TareasProgramadas.class.getMethod("revisarSalud");
        Scheduled programacion = tarea.getAnnotation(Scheduled.class);

        // si nadie la cambia con kolab.revision-cron, corre en el minuto 0 de cada hora
        assertThat(programacion.cron()).isEqualTo("${kolab.revision-cron:0 0 * * * *}");
        assertThat(programacion.zone()).isEqualTo("America/Lima");
    }

    @Test
    void laTareaLlamaALaRevision() {
        RevisionDeSaludService revision = Mockito.mock(RevisionDeSaludService.class);

        new TareasProgramadas(revision).revisarSalud();

        Mockito.verify(revision).revisar();
    }
}
