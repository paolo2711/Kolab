package com.kolab.reporte;

import java.math.BigDecimal;

/**
 * Las cifras de la portada: cuánto se usa la plataforma, tal como está ahora en la base.
 */
public record IndicadoresDePlataforma(long serviciosConcretados,
                                      BigDecimal calificacionPromedio,
                                      long personasQueOfrecen,
                                      long solicitudesAbiertas) {

    public boolean hayActividad() {
        return serviciosConcretados > 0 || solicitudesAbiertas > 0;
    }
}
