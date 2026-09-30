package com.kolab.desarrollo;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

// en una máquina de desarrollo la aplicación arranca con ejemplos si la base está vacía
@Component
@ConditionalOnProperty(name = "kolab.desarrollo", havingValue = "true")
class ArranqueConEjemplos implements ApplicationRunner {

    private final CargaDeEjemplos carga;

    ArranqueConEjemplos(CargaDeEjemplos carga) {
        this.carga = carga;
    }

    @Override
    public void run(ApplicationArguments args) {
        carga.cargarSiVacia();
    }
}
