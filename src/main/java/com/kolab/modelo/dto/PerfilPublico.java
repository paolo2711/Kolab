package com.kolab.modelo.dto;

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
                            List<ResenaResumen> resenas,
                            CabeceraDePerfil cabecera) {

    public boolean ofrece() {
        return !categorias.isEmpty();
    }
}
