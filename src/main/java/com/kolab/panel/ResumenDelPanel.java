package com.kolab.panel;

import java.math.BigDecimal;
import java.util.List;

/**
 * Las cifras del panel, todas sacadas de la base en una sola consulta por bloque.
 */
public record ResumenDelPanel(long solicitudesAbiertas,
                              long serviciosConcretados,
                              BigDecimal comisionRegistrada,
                              BigDecimal calificacionPromedio,
                              long personasQueOfrecen,
                              List<BarraDeCategoria> porCategoria,
                              List<PrecioDeCategoria> precios,
                              List<SolicitudReciente> ultimas) {

    public boolean sinCalificaciones() {
        return calificacionPromedio == null || calificacionPromedio.signum() == 0;
    }

    /**
     * Una categoría con cuántas solicitudes abiertas tiene y qué tan larga sale su barra.
     */
    public record BarraDeCategoria(String categoria, long abiertas, int porcentaje) {
    }

    /**
     * Una fila de la vista {@code v_precios_categoria}.
     */
    public record PrecioDeCategoria(String categoria, long solicitudes, BigDecimal bajo,
                                    BigDecimal tipico, BigDecimal alto) {
    }

    /**
     * Una de las últimas solicitudes publicadas.
     */
    public record SolicitudReciente(Long id, String titulo, String categoria, String autor,
                                    BigDecimal precio, String estado) {
    }
}
