package com.kolab.solicitud;

import com.kolab.categoria.CategoriaService;
import com.kolab.mensaje.MensajeService;
import com.kolab.oferta.OfertaResumen;
import com.kolab.oferta.OfertaService;
import com.kolab.perfil.DirectorioService;
import com.kolab.servicio.ServicioService;
import com.kolab.usuario.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// lo que cada quien hace con sus propias solicitudes: publicarlas, seguirlas y elegir oferta
@Controller
@RequestMapping("/mis-solicitudes")
public class MisSolicitudesController {

    private final SolicitudService solicitudService;
    private final OfertaService ofertaService;
    private final ServicioService servicioService;
    private final MensajeService mensajeService;
    private final CategoriaService categoriaService;
    private final DirectorioService directorioService;

    public MisSolicitudesController(SolicitudService solicitudService, OfertaService ofertaService,
                                    ServicioService servicioService, MensajeService mensajeService,
                                    CategoriaService categoriaService, DirectorioService directorioService) {
        this.solicitudService = solicitudService;
        this.ofertaService = ofertaService;
        this.servicioService = servicioService;
        this.mensajeService = mensajeService;
        this.categoriaService = categoriaService;
        this.directorioService = directorioService;
    }

    @GetMapping
    public String mias(@RequestParam(defaultValue = "0") int pagina,
                       @AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        PageRequest pedida = PageRequest.of(Math.max(pagina, 0), SolicitudController.POR_PAGINA);
        model.addAttribute("seccion", "actividad");
        model.addAttribute("solicitudes", solicitudService.mias(usuario.getIdUsuario(), pedida));
        model.addAttribute("servicios", servicioService.queContrate(usuario.getIdUsuario(), PageRequest.of(0, 10)));
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
        form.setIdDestinatario(idPersona);
        model.addAttribute("solicitudForm", form);
        return formulario("solicitud/nueva", form, model);
    }

    @PostMapping
    public String publicar(@Valid @ModelAttribute SolicitudForm solicitudForm, BindingResult errores,
                           @AuthenticationPrincipal UsuarioAutenticado usuario,
                           Model model, RedirectAttributes flash) {
        if (errores.hasErrors()) {
            return formulario("solicitud/nueva", solicitudForm, model);
        }
        Long id = solicitudService.publicar(solicitudForm, usuario.getIdUsuario());
        flash.addFlashAttribute("aviso", solicitudForm.getIdDestinatario() == null
                ? "Publicada. Quienes saben de esa categoría ya la ven."
                : "Enviada. Solo le llega a la persona que elegiste.");
        return "redirect:/mis-solicitudes/" + id;
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario,
                          Model model) {
        SolicitudDetalle solicitud = solicitudService.propia(id, usuario.getIdUsuario());
        model.addAttribute("seccion", "actividad");
        model.addAttribute("solicitud", solicitud);
        model.addAttribute("hilos", mensajeService.hilos(
                solicitud.ofertas().stream().map(OfertaResumen::id).toList(), usuario.getIdUsuario()));
        return "solicitud/mi-solicitud-y-ofertas";
    }

    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario,
                                   Model model) {
        SolicitudForm form = solicitudService.formularioDe(id, usuario.getIdUsuario());
        model.addAttribute("solicitudForm", form);
        model.addAttribute("idSolicitud", id);
        return formulario("solicitud/editar", form, model);
    }

    @PostMapping("/{id}/editar")
    public String guardarEdicion(@PathVariable Long id, @Valid @ModelAttribute SolicitudForm solicitudForm,
                                 BindingResult errores, @AuthenticationPrincipal UsuarioAutenticado usuario,
                                 Model model, RedirectAttributes flash) {
        if (errores.hasErrors()) {
            model.addAttribute("idSolicitud", id);
            return formulario("solicitud/editar", solicitudForm, model);
        }
        solicitudService.editar(id, solicitudForm, usuario.getIdUsuario());
        flash.addFlashAttribute("aviso", "Solicitud actualizada. Quien ya ofertó ve la versión nueva.");
        return "redirect:/mis-solicitudes/" + id;
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario,
                           RedirectAttributes flash) {
        solicitudService.cancelar(id, usuario.getIdUsuario());
        flash.addFlashAttribute("aviso", "Solicitud cancelada. Ya no aparece en Explorar.");
        return "redirect:/mis-solicitudes";
    }

    @PostMapping("/{id}/ofertas/{idOferta}")
    public String aceptarOferta(@PathVariable Long id, @PathVariable Long idOferta,
                                @AuthenticationPrincipal UsuarioAutenticado usuario, RedirectAttributes flash) {
        Long idServicio = ofertaService.aceptar(idOferta, usuario.getIdUsuario());
        flash.addFlashAttribute("aviso", "Aceptaste la oferta. El servicio quedó registrado con su comisión.");
        return "redirect:/servicios/" + idServicio;
    }

    @PostMapping("/{id}/ofertas/{idOferta}/rechazar")
    public String rechazarOferta(@PathVariable Long id, @PathVariable Long idOferta,
                                 @AuthenticationPrincipal UsuarioAutenticado usuario, RedirectAttributes flash) {
        ofertaService.rechazar(idOferta, usuario.getIdUsuario());
        flash.addFlashAttribute("aviso", "Oferta descartada. Las demás siguen en juego.");
        return "redirect:/mis-solicitudes/" + id;
    }

    private String formulario(String vista, SolicitudForm form, Model model) {
        model.addAttribute("seccion", "actividad");
        model.addAttribute("categorias", categoriaService.activas());
        model.addAttribute("modalidades", Modalidad.values());
        if (form.getIdDestinatario() != null) {
            model.addAttribute("destinatario", directorioService.publico(form.getIdDestinatario()).persona());
        }
        return vista;
    }
}
