package com.kolab.calificacion;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CalificacionForm {

    @NotNull(message = "Elige cuántas estrellas le pones")
    @Min(value = 1, message = "Mínimo una estrella")
    @Max(value = 5, message = "Máximo cinco estrellas")
    private Integer puntaje;

    @Size(max = 500, message = "Máximo 500 caracteres")
    private String comentario;

    public Integer getPuntaje() {
        return puntaje;
    }

    public void setPuntaje(Integer puntaje) {
        this.puntaje = puntaje;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
