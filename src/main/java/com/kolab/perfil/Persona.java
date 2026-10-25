package com.kolab.perfil;

// quien esta del otro lado: el que oferto, el que contrato, el que escribio
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

    // sin esto una lista de gente sin foto son puros circulos iguales
    public int getTono() {
        return (int) (id % 5);
    }
}
