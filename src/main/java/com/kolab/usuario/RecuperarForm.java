package com.kolab.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class RecuperarForm {

    @NotBlank(message = "Escribe tu correo")
    @Email(message = "Ese correo no tiene un formato válido")
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
