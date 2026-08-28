package com.kolab.servicio;

import com.kolab.perfil.Persona;
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
