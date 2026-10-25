package com.kolab.perfil;

import com.kolab.common.Fotos;
import com.kolab.usuario.Usuario;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.StringUtils;

/**
 * arma la Persona que va a las vistas, el unico lugar que decide nombre, iniciales y foto
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
                fotos.dePersona(usuario.getFoto(), usuario.getEmail()));
    }

    /**
     * color de la franja del perfil sacado del nombre, asi la misma persona sale siempre igual
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
