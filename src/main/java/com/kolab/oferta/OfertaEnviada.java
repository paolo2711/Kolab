package com.kolab.oferta;

import java.math.BigDecimal;

/**
 * Una oferta propia en la lista de Mi actividad: a qué solicitud fue, cuánto pedía el cliente,
 * cuánto propuse y cómo va.
 */
public record OfertaEnviada(Long idOferta,
                            Long idSolicitud,
                            String solicitud,
                            String categoria,
                            String icono,
                            String foto,
                            BigDecimal precioDelCliente,
                            BigDecimal miMonto,
                            EstadoOferta estado,
                            long mensajesSinLeer,
                            long cuantasOfertas,
                            String enviada) {

    public boolean tieneFoto() {
        return foto != null;
    }

    public boolean aceptaste() {
        return miMonto.compareTo(precioDelCliente) == 0;
    }

    public boolean tieneMensajes() {
        return mensajesSinLeer > 0;
    }

    public long getCompetidores() {
        return Math.max(0, cuantasOfertas - 1);
    }
}
