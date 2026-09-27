package com.kolab.categoria;

import java.math.BigDecimal;
import java.util.List;

/**
 * Lo que se está pagando de verdad en una categoría: el monto más bajo, el típico y el más alto.
 *
 * <p>No se guarda en ninguna tabla, se calcula al consultar. En la base equivale a la vista
 * {@code v_precios_categoria}, que hace lo mismo en SQL; aquí existe porque las pantallas todavía
 * leen los datos de ejemplo en JSON.
 */
public record PreciosDeCategoria(BigDecimal bajo, BigDecimal tipico, BigDecimal alto) {

    /**
     * Calcula los tres montos a partir de lo que la gente publicó.
     *
     * @param montos los precios propuestos de las solicitudes de esa categoría
     * @param minimo extremo bajo del rango de referencia de la categoría
     * @param maximo extremo alto del rango de referencia
     * @return los precios reales, o el rango de referencia si todavía no hay ninguna solicitud
     */
    public static PreciosDeCategoria de(List<BigDecimal> montos, BigDecimal minimo, BigDecimal maximo) {
        if (montos.isEmpty()) {
            BigDecimal medio = minimo.add(maximo).divide(BigDecimal.valueOf(2));
            return new PreciosDeCategoria(minimo, medio, maximo);
        }
        List<BigDecimal> ordenados = montos.stream().sorted().toList();
        return new PreciosDeCategoria(ordenados.get(0),
                ordenados.get(ordenados.size() / 2),
                ordenados.get(ordenados.size() - 1));
    }

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
}
