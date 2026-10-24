package com.kolab.administracion;

import java.math.BigDecimal;
import java.util.List;

/**
 * Cómo va la plataforma: las cifras de hoy, los últimos meses y lo que más se pide.
 */
public record Tablero(long cuentas, long solicitudesAbiertas, long serviciosCerrados, BigDecimal comisionDelMes,
                      List<Mes> meses, List<CategoriaPedida> categorias) {

    public record Mes(String nombre, long solicitudes, long serviciosCerrados, BigDecimal comision) {
    }

    public record CategoriaPedida(String nombre, long solicitudes) {
    }
}
