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

    private static final int FRANJAS = 6;

    private final Fotos fotos;

    public Personas(Fotos fotos) {
        this.fotos = fotos;
    }

    public Persona de(Usuario usuario) {
        return new Persona(usuario.getId(), usuario.getCodigo(), usuario.getNombreCompleto(), inicialesDe(usuario),
                fotos.dePersona(usuario.getEmail()));
    }

    /**
     * El color de la franja de arriba del perfil, sacado del nombre: la misma persona sale siempre
     * con el mismo color, en su perfil y en el que ven los demás.
     */
    public static int franjaDe(String nombreCompleto) {
        return Math.floorMod(StringUtils.trimToEmpty(nombreCompleto).hashCode(), FRANJAS);
    }

    public static String inicialesDe(Usuario usuario) {
        String delNombre = StringUtils.left(StringUtils.trimToEmpty(usuario.getNombre()), 1);
        String delApellido = StringUtils.left(StringUtils.trimToEmpty(usuario.getApellidos()), 1);
        return StringUtils.upperCase(delNombre + delApellido);
    }
}
