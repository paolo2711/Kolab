package com.kolab.solicitud;

import com.kolab.demo.CatalogoDeCategorias;
import com.kolab.demo.DirectorioDePersonas;
import com.kolab.demo.MensajesDeEjemplo;
import com.kolab.demo.ServiciosDeEjemplo;
import com.kolab.demo.SolicitudesDeEjemplo;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// lo que el cliente hace con sus propias solicitudes: publicarlas, seguirlas y elegir oferta.
@Controller
@RequestMapping("/mis-solicitudes")
public class MisSolicitudesController {

    private final SolicitudesDeEjemplo solicitudes;
    private final ServiciosDeEjemplo servicios;
    private final CatalogoDeCategorias catalogo;
    private final MensajesDeEjemplo mensajes;
    private final DirectorioDePersonas directorio;

    public MisSolicitudesController(SolicitudesDeEjemplo solicitudes, ServiciosDeEjemplo servicios,
                                    CatalogoDeCategorias catalogo, MensajesDeEjemplo mensajes,
                                    DirectorioDePersonas directorio) {
        this.solicitudes = solicitudes;
        this.servicios = servicios;
        this.catalogo = catalogo;
        this.mensajes = mensajes;
        this.directorio = directorio;
    }

    @GetMapping
    public String mias(Model model) {
        model.addAttribute("seccion", "actividad");
        model.addAttribute("solicitudes", solicitudes.misSolicitudes());
        model.addAttribute("servicios", servicios.serviciosQuePedi());
        return "solicitud/mis-solicitudes";
    }

    @GetMapping("/nueva")
    public String formularioNueva(@RequestParam(required = false) String titulo,
                                  @RequestParam(required = false) Long idCategoria,
                                  @RequestParam(required = false) Long idPersona,
                                  Model model) {
        SolicitudForm form = new SolicitudForm();
        form.setTitulo(titulo);
        form.setIdCategoria(idCategoria);

        model.addAttribute("seccion", "actividad");
        model.addAttribute("categorias", catalogo.categorias());
        model.addAttribute("modalidades", Modalidad.values());
        model.addAttribute("solicitudForm", form);
        if (idPersona != null) {
            model.addAttribute("destinatario", directorio.persona(idPersona));
        }
        return "solicitud/nueva";
    }

    @PostMapping
    public String publicar(@Valid @ModelAttribute SolicitudForm solicitudForm,
                           BindingResult errores,
                           Model model,
                           RedirectAttributes flash) {
        if (errores.hasErrors()) {
            model.addAttribute("seccion", "actividad");
            model.addAttribute("categorias", catalogo.categorias());
            model.addAttribute("modalidades", Modalidad.values());
            return "solicitud/nueva";
        }
        flash.addFlashAttribute("aviso", "Publicada. Los expertos de la categoría ya la ven.");
        return "redirect:/mis-solicitudes";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        SolicitudDetalle solicitud = solicitudes.solicitud(id);
        model.addAttribute("seccion", "actividad");
        model.addAttribute("solicitud", solicitud);
        model.addAttribute("hilos", solicitud.ofertas().stream()
                .collect(java.util.stream.Collectors.toMap(o -> o.id(), o -> mensajes.mensajesCon(o.id()))));
        return "solicitud/mi-solicitud-y-ofertas";
    }

    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable Long id, Model model) {
        SolicitudDetalle solicitud = solicitudes.solicitud(id);

        SolicitudForm form = new SolicitudForm();
        form.setTitulo(solicitud.titulo());
        form.setIdCategoria(solicitud.idCategoria());
        form.setModalidad(solicitud.modalidad());
        form.setDistrito(solicitud.distrito());
        form.setDescripcion(solicitud.descripcion());
        form.setFechaDeseada(solicitud.fechaDeseada());
        form.setPrecioPropuesto(solicitud.precioPropuesto());

        model.addAttribute("seccion", "actividad");
        model.addAttribute("idSolicitud", id);
        model.addAttribute("categorias", catalogo.categorias());
        model.addAttribute("modalidades", Modalidad.values());
        model.addAttribute("solicitudForm", form);
        return "solicitud/editar";
    }

    @PostMapping("/{id}/editar")
    public String guardarEdicion(@PathVariable Long id,
                                 @Valid @ModelAttribute SolicitudForm solicitudForm,
                                 BindingResult errores,
                                 Model model,
                                 RedirectAttributes flash) {
        if (errores.hasErrors()) {
            model.addAttribute("seccion", "actividad");
            model.addAttribute("idSolicitud", id);
            model.addAttribute("categorias", catalogo.categorias());
            model.addAttribute("modalidades", Modalidad.values());
            return "solicitud/editar";
        }
        flash.addFlashAttribute("aviso", "Solicitud actualizada. Quien ya ofertó ve la versión nueva.");
        return "redirect:/mis-solicitudes/" + id;
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes flash) {
        flash.addFlashAttribute("aviso", "Solicitud cancelada. Ya no aparece en Explorar.");
        return "redirect:/mis-solicitudes";
    }

    @PostMapping("/{id}/ofertas/{idOferta}")
    public String aceptarOferta(@PathVariable Long id, @PathVariable Long idOferta, RedirectAttributes flash) {
        flash.addFlashAttribute("aviso", "Aceptaste la oferta. El servicio quedó registrado con su comisión.");
        return "redirect:/mis-solicitudes/" + id;
    }
}
