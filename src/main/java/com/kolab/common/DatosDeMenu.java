package com.kolab.common;

import com.kolab.demo.OfertasDeEjemplo;
import com.kolab.demo.SolicitudesDeEjemplo;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// los contadores del menú y de las pestañas de Mi actividad los necesitan todas las pantallas
@ControllerAdvice
public class DatosDeMenu {

    private final OfertasDeEjemplo ofertas;
    private final SolicitudesDeEjemplo solicitudes;

    public DatosDeMenu(OfertasDeEjemplo ofertas, SolicitudesDeEjemplo solicitudes) {
        this.ofertas = ofertas;
        this.solicitudes = solicitudes;
    }

    @ModelAttribute("mensajesSinLeer")
    public int mensajesSinLeer() {
        return ofertas.mensajesSinLeer();
    }

    @ModelAttribute("cuantasPedidas")
    public int cuantasPedidas() {
        return solicitudes.misSolicitudes().size();
    }

    @ModelAttribute("cuantasOfrecidas")
    public int cuantasOfrecidas() {
        return ofertas.misOfertas().size();
    }
}
