package com.kolab.oferta;

import java.math.BigDecimal;

/**
 * Una oferta propia en la lista de Mi actividad: a qué solicitud fue, cuánto pedía el cliente,
 * cuánto propuse y qué está pasando con ella.
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
                            SituacionDeOferta situacion,
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

    // la competencia solo importa mientras quien pidió todavía no elige
    public long getCompetidores() {
        return situacion == SituacionDeOferta.ESPERANDO ? Math.max(0, cuantasOfertas - 1) : 0;
    }
}
