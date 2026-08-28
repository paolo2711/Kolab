package com.kolab.solicitud;

import com.kolab.demo.ArchivosDeDatos;
import com.kolab.demo.CatalogoDeCategorias;
import com.kolab.demo.MensajesDeEjemplo;
import com.kolab.demo.OfertasDeEjemplo;
import com.kolab.demo.SolicitudesDeEjemplo;
import com.kolab.categoria.CategoriaResumen;
import com.kolab.oferta.OfertaForm;
import com.kolab.perfil.PerfilService;
import com.kolab.usuario.UsuarioAutenticado;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

// catálogo público de solicitudes abiertas. es la entrada del experto: aquí busca y desde
// aquí oferta. lo que el cliente hace con sus propias solicitudes vive en MisSolicitudesController.
@Controller
@RequestMapping("/solicitudes")
public class SolicitudController {

    private final SolicitudesDeEjemplo solicitudes;
    private final OfertasDeEjemplo ofertas;
    private final MensajesDeEjemplo mensajes;
    private final CatalogoDeCategorias catalogo;
    private final ArchivosDeDatos archivos;
    private final PerfilService perfilService;

    public SolicitudController(SolicitudesDeEjemplo solicitudes, OfertasDeEjemplo ofertas,
                               MensajesDeEjemplo mensajes, CatalogoDeCategorias catalogo,
                               ArchivosDeDatos archivos, PerfilService perfilService) {
        this.solicitudes = solicitudes;
        this.ofertas = ofertas;
        this.mensajes = mensajes;
        this.catalogo = catalogo;
        this.archivos = archivos;
        this.perfilService = perfilService;
    }

    @GetMapping
    public String catalogo(@ModelAttribute("filtro") FiltroSolicitudes filtro,
                           @AuthenticationPrincipal UsuarioAutenticado usuario,
                           Model model) {
        List<Long> misCategorias = perfilService.categoriasDe(usuario.getIdUsuario());

        model.addAttribute("seccion", "explorar");
        model.addAttribute("categorias", catalogo.categorias());
        model.addAttribute("modalidades", Modalidad.values());
        model.addAttribute("distritos", solicitudes.distritos());
        model.addAttribute("puedeFiltrarPorLoQueSe", !misCategorias.isEmpty());

        var perfil = archivos.perfilDe(usuario.esExperto());
        model.addAttribute("solicitudes",
                solicitudes.conDistancia(aplicar(filtro, misCategorias), perfil.latitud(), perfil.longitud()));
        return "solicitud/explorar";
    }

    // el filtro se aplica de verdad; antes la pantalla ignoraba lo que eligieras
    private List<SolicitudResumen> aplicar(FiltroSolicitudes filtro, List<Long> misCategorias) {
        return solicitudes.catalogo().stream()
                .filter(s -> filtro.getIdCategoria() == null || filtro.getIdCategoria().equals(s.idCategoria()))
                .filter(s -> filtro.getModalidad() == null || filtro.getModalidad() == s.modalidad())
                .filter(s -> filtro.getPrecioMinimo() == null
                        || s.precioPropuesto().compareTo(filtro.getPrecioMinimo()) >= 0)
                .filter(s -> filtro.getPrecioMaximo() == null
                        || s.precioPropuesto().compareTo(filtro.getPrecioMaximo()) <= 0)
                .filter(s -> filtro.getDistrito() == null || filtro.getDistrito().isBlank()
                        || filtro.getDistrito().equals(s.distrito()))
                .filter(s -> !filtro.isSoloLoQueSe() || misCategorias.contains(s.idCategoria()))
                .toList();
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("seccion", "explorar");
        model.addAttribute("solicitud", solicitudes.solicitud(id));
        ofertas.miOfertaEn(id).ifPresent(mia -> model.addAttribute("miOferta", mia));
        if (!model.containsAttribute("ofertaForm")) {
            model.addAttribute("ofertaForm", new OfertaForm());
        }
        return "solicitud/detalle";
    }

    @PostMapping("/{id}/ofertas")
    public String ofertar(@PathVariable Long id,
                          @Valid @ModelAttribute OfertaForm ofertaForm,
                          BindingResult errores,
                          Model model,
                          RedirectAttributes flash) {
        if (errores.hasErrors()) {
            model.addAttribute("seccion", "explorar");
            model.addAttribute("solicitud", solicitudes.solicitud(id));
            ofertas.miOfertaEn(id).ifPresent(mia -> {
            model.addAttribute("miOferta", mia);
            model.addAttribute("mensajes", mensajes.mensajesCon(mia.id()));
        });
            return "solicitud/detalle";
        }
        flash.addFlashAttribute("aviso", "Tu oferta salió. Ahora le toca al cliente decidir.");
        return "redirect:/mis-ofertas";
    }
}
