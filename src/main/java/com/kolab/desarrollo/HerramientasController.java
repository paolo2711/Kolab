package com.kolab.desarrollo;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// solo existe con kolab.desarrollo=true: en el servidor estas rutas no están y responden 404
@Controller
@RequestMapping("/desarrollo")
@ConditionalOnProperty(name = "kolab.desarrollo", havingValue = "true")
class HerramientasController {

    private final ReinicioDeBase reinicio;
    private final CargaDeEjemplos carga;

    HerramientasController(ReinicioDeBase reinicio, CargaDeEjemplos carga) {
        this.reinicio = reinicio;
        this.carga = carga;
    }

    @PostMapping("/reiniciar")
    public String reiniciar(HttpServletRequest pedido, RedirectAttributes flash) throws ServletException {
        reinicio.reiniciar();
        // la cuenta con la que se estaba dentro puede no existir ya
        pedido.logout();
        flash.addFlashAttribute("aviso", "Base reiniciada con los ejemplos.");
        return "redirect:/login";
    }

    @PostMapping("/ejemplos")
    public String cargarEjemplos(RedirectAttributes flash) {
        flash.addFlashAttribute("aviso", carga.cargarSiVacia()
                ? "Ejemplos cargados."
                : "La base ya tiene datos. Para empezar de cero, reinicia los datos.");
        return "redirect:/login";
    }
}
