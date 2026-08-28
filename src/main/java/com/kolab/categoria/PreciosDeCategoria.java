package com.kolab.categoria;

import java.math.BigDecimal;
import java.util.List;

// lo que se está pagando de verdad en la categoría: el mas bajo, el tipico y el mas alto
public record PreciosDeCategoria(BigDecimal bajo, BigDecimal tipico, BigDecimal alto) {

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

    // dónde cae el típico dentro del rango, para pintar la barra
    public int posicionDelTipico() {
        if (alto.compareTo(bajo) == 0) {
            return 50;
        }
        double proporcion = tipico.subtract(bajo).doubleValue() / alto.subtract(bajo).doubleValue();
        return (int) Math.round(proporcion * 100);
    }
}
