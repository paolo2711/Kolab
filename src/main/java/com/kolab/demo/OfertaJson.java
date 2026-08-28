package com.kolab.demo;

import com.kolab.oferta.EstadoOferta;
import java.math.BigDecimal;

record OfertaJson(Long id, Long solicitud, Long persona, BigDecimal monto, String mensaje,
                  int sinLeer, EstadoOferta estado, boolean mia) {
}
