package com.kolab.oferta;

import com.kolab.common.Foto;
import java.math.BigDecimal;

public record OfertaEnviada(Long idSolicitud,
                            String solicitud,
                            String categoria,
                            String icono,
                            String tema,
                            BigDecimal precioDelCliente,
                            BigDecimal miMonto,
                            EstadoOferta estado,
                            int mensajesSinLeer,
                            int cuantasOfertas,
                            String enviada) {

    public String foto(int ancho, int alto) {
        return Foto.de(tema, ancho, alto);
    }

    public boolean aceptaste() {
        return miMonto.compareTo(precioDelCliente) == 0;
    }

    public boolean tieneMensajes() {
        return mensajesSinLeer > 0;
    }
}
