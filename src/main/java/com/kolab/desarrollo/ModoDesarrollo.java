package com.kolab.desarrollo;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// las vistas preguntan por esto para mostrar los enlaces de desarrollo; fuera de él no existe
@ControllerAdvice
@ConditionalOnProperty(name = "kolab.desarrollo", havingValue = "true")
class ModoDesarrollo {

    // las dos primeras personas de los ejemplos, que son las que tienen de todo
    private static final int CUENTAS_A_LA_VISTA = 2;

    private final List<String> cuentas;
    private final String clave;

    ModoDesarrollo(Ejemplos ejemplos, @Value("${kolab.clave-de-ejemplo}") String clave) {
        this.cuentas = ejemplos.personas().stream()
                .limit(CUENTAS_A_LA_VISTA)
                .map(Ejemplos.Persona::email)
                .toList();
        this.clave = clave;
    }

    @ModelAttribute("modoDesarrollo")
    boolean modoDesarrollo() {
        return true;
    }

    @ModelAttribute("cuentasDeEjemplo")
    List<String> cuentasDeEjemplo() {
        return cuentas;
    }

    @ModelAttribute("claveDeEjemplo")
    String claveDeEjemplo() {
        return clave;
    }
}
