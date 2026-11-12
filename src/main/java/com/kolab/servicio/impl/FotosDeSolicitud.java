package com.kolab.servicio.impl;

import com.kolab.modelo.entidad.Solicitud;
import com.kolab.util.Fotos;
import com.kolab.util.FotosSubidas;
import org.springframework.stereotype.Component;

/**
 * que foto se muestra de una solicitud: la que subio quien la publico o la de su categoria
 */
@Component
public class FotosDeSolicitud {

    private final Fotos fotos;

    public FotosDeSolicitud(Fotos fotos) {
        this.fotos = fotos;
    }

    public String de(Solicitud s) {
        String propia = propia(s);
        return propia != null ? propia : fotos.deCategoria(s.getCategoria().getNombre());
    }

    // null si usa la de su categoria
    public String propia(Solicitud s) {
        return FotosSubidas.url(s.getFoto());
    }
}
