package com.kolab.usuario;

import com.kolab.common.Fotos;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// ingresar, crear cuenta y recuperar comparten el panel de la derecha: la foto
@ControllerAdvice(assignableTypes = {CuentaController.class, RecuperacionController.class})
public class PantallasDeAcceso {

    private final Fotos fotos;

    public PantallasDeAcceso(Fotos fotos) {
        this.fotos = fotos;
    }

    @ModelAttribute("fotoAcceso")
    public String fotoAcceso() {
        return fotos.suelta("acceso");
    }
}
