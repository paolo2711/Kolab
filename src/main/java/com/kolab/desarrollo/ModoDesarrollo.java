package com.kolab.desarrollo;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// las vistas preguntan por esto para mostrar los enlaces de desarrollo; fuera de él no existe
@ControllerAdvice
@ConditionalOnProperty(name = "kolab.desarrollo", havingValue = "true")
class ModoDesarrollo {

    @ModelAttribute("modoDesarrollo")
    boolean modoDesarrollo() {
        return true;
    }
}
