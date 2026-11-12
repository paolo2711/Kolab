package com.kolab.modelo.dto;

import java.util.List;

/**
 * Mi perfil: lo que cuento de mí, mi reputación y mi actividad en cifras.
 */
public record PerfilResumen(String descripcion,
                            String experiencia,
                            String distrito,
                            String telefono,
                            String miembroDesde,
                            Reputacion reputacion,
                            long solicitudesPublicadas,
                            List<ResenaResumen> resenas,
                            CabeceraDePerfil cabecera) {

    public boolean sinCalificar() {
        return reputacion.sinCalificar();
    }
}
