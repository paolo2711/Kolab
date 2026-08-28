package com.kolab.demo;

import java.math.BigDecimal;

record CategoriaJson(Long id, String nombre, String icono,
                     BigDecimal precioMinimo, BigDecimal precioMaximo, String tema) {
}
