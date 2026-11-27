package com.kolab.configuracion;

import com.kolab.servicio.RevisionDeSaludService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * las tareas que kolab corre solo, sin que nadie entre: los cron jobs del mantenimiento
 */
@Configuration
@EnableScheduling
public class TareasProgramadas {

    private final RevisionDeSaludService revision;

    public TareasProgramadas(RevisionDeSaludService revision) {
        this.revision = revision;
    }

    // segundo 0, minuto 0, cada hora, en hora de lima. se cambia con kolab.revision-cron sin tocar el codigo
    @Scheduled(cron = "${kolab.revision-cron:0 0 * * * *}", zone = "America/Lima")
    public void revisarSalud() {
        revision.revisar();
    }
}
