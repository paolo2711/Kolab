package com.kolab.oferta;

import com.kolab.perfil.Persona;
import java.math.BigDecimal;

public record OfertaResumen(Long id,
                            Persona persona,
                            BigDecimal calificacion,
                            int servicios,
                            BigDecimal monto,
                            String mensaje,
                            int sinLeer) {

    public boolean tieneSinLeer() {
        return sinLeer > 0;
    }
}
