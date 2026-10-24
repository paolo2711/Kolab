package com.kolab.perfil;

import java.math.BigDecimal;

/**
 * Lo que dice la plataforma de una persona: su promedio, cuántas veces la calificaron y cuántos
 * servicios terminó dando.
 */
public record Reputacion(BigDecimal promedio, int calificaciones, long serviciosDados) {

    public static final Reputacion NUEVA = new Reputacion(BigDecimal.ZERO, 0, 0);

    public boolean sinCalificar() {
        return calificaciones == 0;
    }
}
