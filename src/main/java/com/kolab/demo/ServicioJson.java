package com.kolab.demo;

import com.kolab.servicio.EstadoServicio;
import java.math.BigDecimal;

record ServicioJson(Long id, Long solicitud, Long oferta, BigDecimal comision,
                    EstadoServicio estado, String fecha, String miembroDesde,
                    boolean loPedi, boolean loDoy) {
}
