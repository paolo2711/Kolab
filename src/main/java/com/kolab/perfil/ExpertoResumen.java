package com.kolab.perfil;

import java.math.BigDecimal;

public record ExpertoResumen(Persona persona, String distrito, BigDecimal calificacion,
                             int servicios, String titular, String ultimaResena,
                             boolean verificado) {
}
