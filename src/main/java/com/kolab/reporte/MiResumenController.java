package com.kolab.reporte;

import com.kolab.usuario.UsuarioAutenticado;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// el resumen es de quien entró y de nadie más: no recibe id, lo toma de la sesión
@Controller
public class MiResumenController {

    private final ReporteService reporteService;

    public MiResumenController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/mi-resumen")
    public String resumen(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        model.addAttribute("seccion", "actividad");
        model.addAttribute("usuario", usuario);
        model.addAttribute("resumen", reporteService.resumenDe(usuario.getIdUsuario()));
        return "reporte/mi-resumen";
    }
}
