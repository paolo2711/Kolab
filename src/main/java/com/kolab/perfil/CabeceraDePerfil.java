package com.kolab.perfil;

import com.kolab.common.ColorPrincipal;
import com.kolab.common.FotosSubidas;

/**
 * lo de arriba del perfil: la portada si la subio, si no la franja con el color de su foto. si no
 * hay ninguna de las dos los dos van null y la franja sale con el color por nombre
 */
public record CabeceraDePerfil(String portada, String color) {

    static CabeceraDePerfil de(Perfil perfil) {
        String color = perfil.getUsuario().getFoto() == null || perfil.getColorFoto() == null
                ? null : ColorPrincipal.paraFranja(perfil.getColorFoto());
        return new CabeceraDePerfil(FotosSubidas.url(perfil.getPortada()), color);
    }
}
