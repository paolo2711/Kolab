package com.kolab.categoria;

import java.math.BigDecimal;

/**
 * Una categoría tal como se muestra: con su foto, su rango de referencia y cuántas solicitudes
 * abiertas tiene ahora. {@code foto} es {@code null} cuando no hay archivo y se pinta el ícono.
 */
public record CategoriaResumen(Long id, String nombre, String icono, String foto,
                               BigDecimal precioMinimo, BigDecimal precioMaximo,
                               long abiertas) {

    public boolean tieneFoto() {
        return foto != null;
    }

    public String getRango() {
        return "S/ " + precioMinimo.stripTrailingZeros().toPlainString()
                + " a S/ " + precioMaximo.stripTrailingZeros().toPlainString();
    }

    public String getDesde() {
        return precioMinimo.stripTrailingZeros().toPlainString();
    }

    public boolean tieneDemanda() {
        return abiertas > 0;
    }
}
