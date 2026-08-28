package com.kolab.common;

public class EmailYaRegistradoException extends RuntimeException {

    private final String email;

    public EmailYaRegistradoException(String email) {
        super("Ya existe una cuenta con el correo " + email);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
