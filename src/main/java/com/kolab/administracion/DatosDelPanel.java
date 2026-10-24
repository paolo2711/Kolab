package com.kolab.administracion;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// las pantallas del panel llevan su propio menú y no el del mercado; el layout mira esto para elegir
@ControllerAdvice(assignableTypes = AdministracionController.class)
public class DatosDelPanel {

    @ModelAttribute("panel")
    public boolean panel() {
        return true;
    }
}
