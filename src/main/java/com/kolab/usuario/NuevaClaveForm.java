package com.kolab.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class NuevaClaveForm {

    @NotBlank(message = "Escribe una contraseña")
    @Size(min = 8, max = 64, message = "Entre 8 y 64 caracteres")
    private String password;

    @NotBlank(message = "Repite la contraseña")
    private String confirmacion;

    public boolean coinciden() {
        return password != null && password.equals(confirmacion);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmacion() {
        return confirmacion;
    }

    public void setConfirmacion(String confirmacion) {
        this.confirmacion = confirmacion;
    }
}
