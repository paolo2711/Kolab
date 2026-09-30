package com.kolab.perfil;

import com.kolab.common.Foto;

// quien está del otro lado: el que ofertó, el que contrató, el que escribió. es la misma gente de
// datos/personas.json reutilizada, no una copia por pantalla
public record Persona(Long id, String nombre, String iniciales, int cara) {

    public boolean tieneFoto() {
        return cara > 0;
    }

    public String retrato() {
        return Foto.retrato(cara);
    }

    // sin esto una lista de gente sin foto es una fila de círculos idénticos
    public int getTono() {
        return (int) (id % 5);
    }
}
