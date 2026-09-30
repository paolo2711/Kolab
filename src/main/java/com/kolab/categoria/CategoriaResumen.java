package com.kolab.categoria;

import com.kolab.common.Foto;
import java.math.BigDecimal;

public record CategoriaResumen(Long id, String nombre, String icono, String tema,
                               BigDecimal precioMinimo, BigDecimal precioMaximo,
                               long abiertas) {

    public String foto() {
        return Foto.de(tema);
    }

    public String getRango() {
        return "S/ " + precioMinimo.toPlainString() + " a S/ " + precioMaximo.toPlainString();
    }

    public boolean tieneDemanda() {
        return abiertas > 0;
    }
}
