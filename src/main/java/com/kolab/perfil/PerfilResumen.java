package com.kolab.perfil;

import com.kolab.calificacion.ResenaResumen;
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
                            List<ResenaResumen> resenas) {

    public boolean sinCalificar() {
        return reputacion.sinCalificar();
    }
}
