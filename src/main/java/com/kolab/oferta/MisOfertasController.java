package com.kolab.oferta;

import com.kolab.demo.OfertasDeEjemplo;
import com.kolab.demo.ServiciosDeEjemplo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MisOfertasController {

    private final OfertasDeEjemplo ofertas;
    private final ServiciosDeEjemplo servicios;

    public MisOfertasController(OfertasDeEjemplo ofertas, ServiciosDeEjemplo servicios) {
        this.ofertas = ofertas;
        this.servicios = servicios;
    }

    @GetMapping("/mis-ofertas")
    public String mias(Model model) {
        model.addAttribute("seccion", "actividad");
        model.addAttribute("ofertas", ofertas.misOfertas());
        model.addAttribute("servicios", servicios.serviciosQueDoy());
        return "oferta/mis-ofertas";
    }
}
