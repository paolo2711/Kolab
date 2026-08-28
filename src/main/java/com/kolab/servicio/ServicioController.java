package com.kolab.servicio;

import com.kolab.calificacion.CalificacionForm;
import com.kolab.demo.ServiciosDeEjemplo;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// el servicio nace cuando el cliente acepta una oferta. de aquí en adelante el camino es
// el mismo para los dos roles: coordinan, se cierra y se califican.
@Controller
@RequestMapping("/servicios")
public class ServicioController {

    private final ServiciosDeEjemplo servicios;

    public ServicioController(ServiciosDeEjemplo servicios) {
        this.servicios = servicios;
    }

    // no hay pantalla propia de servicios: los que pedí y los que doy son cosas distintas y
    // cada uno vive en su pestaña de Mi actividad. una tercera lista mezclada los repetía.
    @GetMapping
    public String lista() {
        return "redirect:/mis-solicitudes";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("seccion", "actividad");
        model.addAttribute("servicio", servicios.servicio(id));
        return "servicio/detalle";
    }

    @GetMapping("/{id}/cerrar")
    public String cerrar(@PathVariable Long id, Model model) {
        model.addAttribute("seccion", "actividad");
        model.addAttribute("servicio", servicios.servicio(id));
        model.addAttribute("calificacionForm", new CalificacionForm());
        return "servicio/cerrar";
    }

    @PostMapping("/{id}/calificar")
    public String calificar(@PathVariable Long id,
                            @Valid @ModelAttribute CalificacionForm calificacionForm,
                            BindingResult errores,
                            Model model,
                            RedirectAttributes flash) {
        if (errores.hasErrors()) {
            model.addAttribute("seccion", "actividad");
            model.addAttribute("servicio", servicios.servicio(id));
            return "servicio/cerrar";
        }
        flash.addFlashAttribute("aviso", "Servicio cerrado y calificación enviada.");
        return "redirect:/mis-solicitudes";
    }
}
