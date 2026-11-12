package com.kolab.modelo.dto;

import com.kolab.modelo.dto.Persona;

public record ResenaResumen(Persona autor,
                            int puntaje,
                            String comentario,
                            String servicio,
                            String fecha) {

    public boolean tieneComentario() {
        return comentario != null && !comentario.isBlank();
    }
}
