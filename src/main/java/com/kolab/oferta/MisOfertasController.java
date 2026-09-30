package com.kolab.oferta;

import com.kolab.servicio.ServicioService;
import com.kolab.usuario.UsuarioAutenticado;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MisOfertasController {

    private static final int POR_PAGINA = 15;

    private final OfertaService ofertaService;
    private final ServicioService servicioService;

    public MisOfertasController(OfertaService ofertaService, ServicioService servicioService) {
        this.ofertaService = ofertaService;
        this.servicioService = servicioService;
    }

    @GetMapping("/mis-ofertas")
    public String mias(@RequestParam(defaultValue = "0") int pagina,
                       @AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        model.addAttribute("seccion", "actividad");
        model.addAttribute("ofertas",
                ofertaService.mias(usuario.getIdUsuario(), PageRequest.of(Math.max(pagina, 0), POR_PAGINA)));
        model.addAttribute("servicios", servicioService.queDoy(usuario.getIdUsuario(), PageRequest.of(0, 10)));
        return "oferta/mis-ofertas";
    }
}
