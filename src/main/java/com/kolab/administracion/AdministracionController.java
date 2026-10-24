package com.kolab.administracion;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

// salvo el ingreso, solo llega quien tiene el rol: la cadena del panel en SecurityConfig cierra lo demás
@Controller
@RequestMapping("/admin")
public class AdministracionController {

    private static final int POR_PAGINA = 25;

    private final AdministracionService administracionService;

    public AdministracionController(AdministracionService administracionService) {
        this.administracionService = administracionService;
    }

    // el ingreso del panel usa la misma pantalla que el del mercado; la plantilla cambia lo que hace falta
    @GetMapping("/login")
    public String ingreso() {
        return "cuenta/login";
    }

    @GetMapping
    public String panel(Model model) {
        model.addAttribute("seccion", "panel");
        model.addAttribute("cuentasRegistradas", administracionService.cuentasRegistradas());
        return "administracion/panel";
    }

    @GetMapping("/cuentas")
    public String cuentas(@RequestParam(defaultValue = "0") int pagina, Model model) {
        model.addAttribute("seccion", "cuentas");
        model.addAttribute("cuentas", administracionService.cuentas(PageRequest.of(Math.max(pagina, 0), POR_PAGINA)));
        return "administracion/cuentas";
    }
}
