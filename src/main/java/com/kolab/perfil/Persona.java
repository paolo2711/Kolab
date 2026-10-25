package com.kolab.perfil;

// quien está del otro lado: el que ofertó, el que contrató, el que escribió
public record Persona(Long id, String codigo, String nombre, String iniciales, String foto) {

    public boolean tieneFoto() {
        return foto != null;
    }

    public String retrato() {
        return foto;
    }

    public int getFranja() {
        return Personas.franjaDe(nombre);
    }

    // sin esto una lista de gente sin foto es una fila de círculos idénticos
    public int getTono() {
        return (int) (id % 5);
    }
}
