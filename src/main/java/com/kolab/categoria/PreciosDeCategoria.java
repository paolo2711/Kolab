package com.kolab.categoria;

import java.math.BigDecimal;

/**
 * Lo que se está pagando de verdad en una categoría: el monto más bajo, el típico y el más alto.
 *
 * <p>Sale de la vista {@code v_precios_categoria}, que lo calcula al consultar. Si la categoría
 * todavía no tiene solicitudes, trae el rango de referencia.
 */
public record PreciosDeCategoria(BigDecimal bajo, BigDecimal tipico, BigDecimal alto, long solicitudes) {

    /**
     * Dónde cae el típico dentro del rango, de 0 a 100, para pintar la marca de la barra.
     */
    public int posicionDelTipico() {
        if (alto.compareTo(bajo) == 0) {
            return 50;
        }
        double proporcion = tipico.subtract(bajo).doubleValue() / alto.subtract(bajo).doubleValue();
        return (int) Math.round(proporcion * 100);
    }

    public boolean esDeReferencia() {
        return solicitudes == 0;
    }
}
