package com.kolab.reporte;

/**
 * Los reportes que salen de la base: los indicadores de la plataforma y el resumen de cada persona.
 */
public interface ReporteService {

    IndicadoresDePlataforma plataforma();

    /**
     * Cómo le va a una persona de los dos lados de su cuenta: lo que pidió y lo que ofreció.
     */
    MiResumen resumenDe(Long idUsuario);
}
