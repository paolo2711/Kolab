package com.kolab.modelo.dto;

import com.kolab.modelo.entidad.EstadoServicio;
import java.math.BigDecimal;

public record ServicioResumen(Long id,
                              String titulo,
                              Persona contraparte,
                              BigDecimal montoFinal,
                              BigDecimal comision,
                              EstadoServicio estado,
                              String fecha,
                              boolean loDoy) {

    public BigDecimal getNeto() {
        return montoFinal.subtract(comision);
    }
}
