package com.kolab.perfil;

import com.kolab.common.Fotos;
import com.kolab.usuario.Usuario;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.StringUtils;

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
        String delNombre = StringUtils.left(StringUtils.trimToEmpty(usuario.getNombre()), 1);
        String delApellido = StringUtils.left(StringUtils.trimToEmpty(usuario.getApellidos()), 1);
        return StringUtils.upperCase(delNombre + delApellido);
    }
}
