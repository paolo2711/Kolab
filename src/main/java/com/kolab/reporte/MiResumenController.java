package com.kolab.reporte;

import com.kolab.usuario.UsuarioAutenticado;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// el resumen es de quien entró y de nadie más: no recibe id, lo toma de la sesión
@Controller
public class MiResumenController {

    private final ReporteService reporteService;
    private final ResumenEnExcel enExcel;

    public MiResumenController(ReporteService reporteService, ResumenEnExcel enExcel) {
        this.reporteService = reporteService;
        this.enExcel = enExcel;
    }

    @GetMapping("/mi-resumen")
    public String resumen(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        model.addAttribute("seccion", "actividad");
        model.addAttribute("usuario", usuario);
        model.addAttribute("resumen", reporteService.resumenDe(usuario.getIdUsuario()));
        return "reporte/mi-resumen";
    }

    @GetMapping("/mi-resumen/excel")
    public ResponseEntity<byte[]> excel(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return DescargaExcel.de("mi-resumen-kolab.xlsx",
                enExcel.de(reporteService.resumenDe(usuario.getIdUsuario()), usuario.getNombre()));
    }
}
