package com.kolab.modelo.dto;

import com.kolab.modelo.entidad.Perfil;
import com.kolab.util.ColorPrincipal;
import com.kolab.util.FotosSubidas;

/**
 * lo de arriba del perfil: la portada si la subio, si no la franja con el color de su foto. si no
 * hay ninguna de las dos los dos van null y la franja sale con el color por nombre
 */
public record CabeceraDePerfil(String portada, String color) {

    public static CabeceraDePerfil de(Perfil perfil) {
        String color = perfil.getUsuario().getFoto() == null || perfil.getColorFoto() == null
                ? null : ColorPrincipal.paraFranja(perfil.getColorFoto());
        return new CabeceraDePerfil(FotosSubidas.url(perfil.getPortada()), color);
    }
}
