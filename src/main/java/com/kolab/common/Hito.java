package com.kolab.common;

public record Hito(String titulo, String detalle, boolean hecho, boolean actual) {

    public static Hito cumplido(String titulo, String detalle) {
        return new Hito(titulo, detalle, true, false);
    }

    public static Hito enCurso(String titulo, String detalle) {
        return new Hito(titulo, detalle, false, true);
    }

    public static Hito pendiente(String titulo, String detalle) {
        return new Hito(titulo, detalle, false, false);
    }
}
