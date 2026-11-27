package com.kolab.servicio.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.kolab.modelo.dto.Monitoreo;
import com.kolab.servicio.MonitoreoService;
import com.kolab.servicio.RevisionDeSaludService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@Tag("unitaria")
class RevisionDeSaludServiceImplTest {

    private final MonitoreoService monitoreo = Mockito.mock(MonitoreoService.class);
    private final RevisionDeSaludService revision = new RevisionDeSaludServiceImpl(monitoreo);

    @Test
    void siTodoEstaBienNoHayAvisos() {
        Mockito.when(monitoreo.leer()).thenReturn(lectura("UP", 40, 30));

        assertThat(revision.revisar()).isEmpty();
    }

    @Test
    void avisaSiLaMemoriaDelServidorPasaDel80() {
        Mockito.when(monitoreo.leer()).thenReturn(lectura("UP", 85, 30));

        assertThat(revision.revisar()).singleElement().asString().contains("memoria").contains("85");
    }

    @Test
    void avisaSiElDiscoPasaDel80() {
        Mockito.when(monitoreo.leer()).thenReturn(lectura("UP", 40, 91));

        assertThat(revision.revisar()).singleElement().asString().contains("disco").contains("91");
    }

    @Test
    void avisaSiLaBaseNoResponde() {
        Mockito.when(monitoreo.leer()).thenReturn(lectura("DOWN", 40, 30));

        assertThat(revision.revisar()).singleElement().asString().contains("base");
    }

    @Test
    void justoEn80TodaviaNoAvisa() {
        Mockito.when(monitoreo.leer()).thenReturn(lectura("UP", 80, 80));

        assertThat(revision.revisar()).isEmpty();
    }

    private static Monitoreo lectura(String base, int memoria, int disco) {
        return new Monitoreo("27/11/2026 03:00:00",
                new Monitoreo.Aplicacion(1520, 35, 410, 1400, 110, 0, 0, 0, 120, 256, 47, "2 h 13 min"),
                new Monitoreo.BaseDeDatos(base, 1, 9, 10, 10, "12 MB"),
                new Monitoreo.Sistema(12, 3, 1024, 324, memoria, 30, 22, disco, "solo en el servidor Linux"));
    }
}
