package com.kolab.usuario;

import com.kolab.common.Fotos;
import com.kolab.reporte.IndicadoresDePlataforma;
import com.kolab.reporte.ReporteService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// ingresar, crear cuenta y recuperar comparten el panel de la derecha: la foto y las cifras reales
@ControllerAdvice(assignableTypes = {CuentaController.class, RecuperacionController.class})
public class PantallasDeAcceso {

    private final Fotos fotos;
    private final ReporteService reporteService;

    public PantallasDeAcceso(Fotos fotos, ReporteService reporteService) {
        this.fotos = fotos;
        this.reporteService = reporteService;
    }

    @ModelAttribute("fotoAcceso")
    public String fotoAcceso() {
        return fotos.suelta("acceso");
    }

    @ModelAttribute("plataforma")
    public IndicadoresDePlataforma plataforma() {
        return reporteService.plataforma();
    }
}
