package com.kolab.servicio;

import com.kolab.calificacion.CalificacionForm;
import com.kolab.usuario.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// el servicio nace cuando se acepta una oferta. de aquí en adelante el camino es el mismo para
// las dos partes: coordinan, lo cierra quien pidió y se califican.
@Controller
@RequestMapping("/servicios")
public class ServicioController {

    private final ServicioService servicioService;

    public ServicioController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    // no hay lista propia: los que pedí y los que doy viven cada uno en su pestaña de Mi actividad
    @GetMapping
    public String lista() {
        return "redirect:/mis-solicitudes";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario,
                          Model model) {
        model.addAttribute("seccion", "actividad");
        model.addAttribute("servicio", servicioService.detalle(id, usuario.getIdUsuario()));
        return "servicio/detalle";
    }

    @GetMapping("/{id}/cerrar")
    public String cerrar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario,
                         Model model) {
        ServicioDetalle servicio = servicioService.detalle(id, usuario.getIdUsuario());
        if (!servicio.puedoCerrar() && !servicio.puedoCalificar()) {
            return "redirect:/servicios/" + id;
        }
        model.addAttribute("calificacionForm", new CalificacionForm());
        return mostrarCierre(servicio, model);
    }

    @PostMapping("/{id}/calificar")
    public String calificar(@PathVariable Long id,
                            @Valid @ModelAttribute CalificacionForm calificacionForm,
                            BindingResult errores,
                            @AuthenticationPrincipal UsuarioAutenticado usuario,
                            Model model,
                            RedirectAttributes flash) {
        if (errores.hasErrors()) {
            return mostrarCierre(servicioService.detalle(id, usuario.getIdUsuario()), model);
        }
        servicioService.cerrarYCalificar(id, calificacionForm, usuario.getIdUsuario());
        flash.addFlashAttribute("aviso", "Listo. Tu calificación ya cuenta en su promedio.");
        return "redirect:/servicios/" + id;
    }

    private String mostrarCierre(ServicioDetalle servicio, Model model) {
        model.addAttribute("seccion", "actividad");
        model.addAttribute("servicio", servicio);
        return "servicio/cerrar";
    }
}
