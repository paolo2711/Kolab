package com.kolab.perfil;

import com.kolab.calificacion.ResenaResumen;
import com.kolab.categoria.CategoriaResumen;
import java.util.List;

/**
 * El perfil de una persona tal como lo ven los demás.
 */
public record PerfilPublico(Persona persona,
                            String distrito,
                            String miembroDesde,
                            String descripcion,
                            String experiencia,
                            Reputacion reputacion,
                            List<CategoriaResumen> categorias,
                            List<ResenaResumen> resenas) {

    public boolean ofrece() {
        return !categorias.isEmpty();
    }
}
