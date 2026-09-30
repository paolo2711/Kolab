package com.kolab.desarrollo;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Deja la base como recién creada: borra todo, vuelve a correr las migraciones y carga los
 * ejemplos. Usa la conexión de las migraciones, que es la única con permiso para borrar tablas.
 */
@Component
@ConditionalOnProperty(name = "kolab.desarrollo", havingValue = "true")
class ReinicioDeBase {

    private static final Logger log = LoggerFactory.getLogger(ReinicioDeBase.class);

    private final Flyway flyway;
    private final CargaDeEjemplos carga;

    ReinicioDeBase(Flyway flyway, CargaDeEjemplos carga) {
        this.flyway = flyway;
        this.carga = carga;
    }

    void reiniciar() {
        // el borrado está apagado en la configuración normal; solo esta copia lo permite
        Flyway conBorrado = Flyway.configure()
                .configuration(flyway.getConfiguration())
                .cleanDisabled(false)
                .load();
        conBorrado.clean();
        conBorrado.migrate();
        carga.cargarSiVacia();
        log.warn("base reiniciada desde la pantalla de ingreso");
    }
}
