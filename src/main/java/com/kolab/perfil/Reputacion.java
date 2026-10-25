package com.kolab.perfil;

import java.math.BigDecimal;

/**
 * Lo que dice la plataforma de una persona: su promedio, cuántas veces la calificaron y cuántos
 * servicios terminó dando.
 */
public record Reputacion(BigDecimal promedio, int calificaciones, long serviciosDados) {

    public boolean sinCalificar() {
        return calificaciones == 0;
    }
}
