package com.kolab.calificacion;

import com.kolab.perfil.Persona;

public record ResenaResumen(Persona autor,
                            int puntaje,
                            String comentario,
                            String servicio,
                            String fecha) {

    public boolean tieneComentario() {
        return comentario != null && !comentario.isBlank();
    }
}
