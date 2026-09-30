package com.kolab.perfil;

// quien está del otro lado: el que ofertó, el que contrató, el que escribió
public record Persona(Long id, String nombre, String iniciales, String foto) {

    public boolean tieneFoto() {
        return foto != null;
    }

    public String retrato() {
        return foto;
    }

    // sin esto una lista de gente sin foto es una fila de círculos idénticos
    public int getTono() {
        return (int) (id % 5);
    }
}
