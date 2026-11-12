package com.kolab.modelo.dto;

import com.kolab.modelo.entidad.EstadoOferta;
import java.math.BigDecimal;

public record OfertaResumen(Long id,
                            Persona persona,
                            Reputacion reputacion,
                            BigDecimal monto,
                            String mensaje,
                            EstadoOferta estado,
                            String enviada,
                            long sinLeer) {

    public BigDecimal getCalificacion() {
        return reputacion.promedio();
    }

    public long getServicios() {
        return reputacion.serviciosDados();
    }

    public boolean sinCalificar() {
        return reputacion.sinCalificar();
    }

    public boolean tieneSinLeer() {
        return sinLeer > 0;
    }

    public boolean enJuego() {
        return estado == EstadoOferta.ENVIADA;
    }
}
