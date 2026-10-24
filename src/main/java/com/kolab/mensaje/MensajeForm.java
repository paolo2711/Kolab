package com.kolab.mensaje;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MensajeForm {

    @NotBlank(message = "Escribe algo antes de enviar")
    @Size(max = 1000, message = "Máximo 1000 caracteres")
    private String contenido;

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }
}
