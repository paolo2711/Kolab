package com.kolab.perfil;

import com.kolab.common.Fotos;
import com.kolab.usuario.Usuario;
import org.springframework.stereotype.Component;

/**
 * Arma la {@link Persona} que viaja a las vistas a partir de la cuenta. Es el único lugar que decide
 * cómo se muestra una persona: nombre, iniciales y foto.
 */
@Component
public class Personas {

    private final Fotos fotos;

    public Personas(Fotos fotos) {
        this.fotos = fotos;
    }

    public Persona de(Usuario usuario) {
        return new Persona(usuario.getId(), usuario.getNombreCompleto(), inicialesDe(usuario),
                fotos.dePersona(usuario.getEmail()));
    }

    public static String inicialesDe(Usuario usuario) {
        String apellidos = usuario.getApellidos();
        char primera = usuario.getNombre().charAt(0);
        if (apellidos == null || apellidos.isBlank()) {
            return String.valueOf(primera);
        }
        return ("" + primera + apellidos.charAt(0)).toUpperCase();
    }
}
