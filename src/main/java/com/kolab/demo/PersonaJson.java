package com.kolab.demo;

import java.math.BigDecimal;
import java.util.List;

record PersonaJson(Long id, String nombre, String iniciales, String distrito,
                   BigDecimal calificacion, int servicios, List<Long> categorias,
                   String titular, String ultimaResena, boolean verificado, int cara) {
}
