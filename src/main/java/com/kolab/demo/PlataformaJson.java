package com.kolab.demo;

import java.math.BigDecimal;

public record PlataformaJson(int serviciosConcretados, BigDecimal calificacionPromedio,
                             int expertosActivos, int comisionPorcentaje) {
}
