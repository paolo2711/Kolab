package com.kolab.solicitud;

import com.kolab.categoria.CategoriaService;
import com.kolab.oferta.OfertaForm;
import com.kolab.oferta.OfertaService;
import com.kolab.perfil.PerfilService;
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

// el catálogo de solicitudes abiertas: aquí se busca y desde aquí se oferta. lo que cada quien
// hace con sus propias solicitudes vive en MisSolicitudesController.
@Controller
@RequestMapping("/solicitudes")
public class SolicitudController {

    static final int POR_PAGINA = 15;

    private final CatalogoService catalogoService;
    private final SolicitudService solicitudService;
    private final OfertaService ofertaService;
    private final CategoriaService categoriaService;
    private final PerfilService perfilService;

    public SolicitudController(CatalogoService catalogoService, SolicitudService solicitudService,
                               OfertaService ofertaService, CategoriaService categoriaService,
                               PerfilService perfilService) {
        this.catalogoService = catalogoService;
        this.solicitudService = solicitudService;
        this.ofertaService = ofertaService;
        this.categoriaService = categoriaService;
        this.perfilService = perfilService;
    }

    @GetMapping
    public String catalogo(@ModelAttribute("filtro") FiltroSolicitudes filtro,
                           @RequestParam(defaultValue = "0") int pagina,
                           @AuthenticationPrincipal UsuarioAutenticado usuario,
                           Model model) {
        Long yo = usuario.getIdUsuario();
        model.addAttribute("seccion", "explorar");
        model.addAttribute("categorias", categoriaService.activas());
        model.addAttribute("modalidades", Modalidad.values());
        model.addAttribute("distritos", catalogoService.distritos());
        model.addAttribute("puedeFiltrarPorLoQueSe", perfilService.ofreceServicios(yo));
        model.addAttribute("solicitudes",
                catalogoService.explorar(filtro, yo, PageRequest.of(Math.max(pagina, 0), POR_PAGINA)));
        return "solicitud/explorar";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario,
                          Model model) {
        if (solicitudService.esSuya(id, usuario.getIdUsuario())) {
            return "redirect:/mis-solicitudes/" + id;
        }
        if (!model.containsAttribute("ofertaForm")) {
            model.addAttribute("ofertaForm", new OfertaForm());
        }
        return mostrar(id, usuario, model);
    }

    @PostMapping("/{id}/ofertas")
    public String ofertar(@PathVariable Long id,
                          @Valid @ModelAttribute OfertaForm ofertaForm,
                          BindingResult errores,
                          @AuthenticationPrincipal UsuarioAutenticado usuario,
                          Model model,
                          RedirectAttributes flash) {
        if (errores.hasErrors()) {
            return mostrar(id, usuario, model);
        }
        ofertaService.ofertar(id, ofertaForm, usuario.getIdUsuario());
        flash.addFlashAttribute("aviso", "Tu oferta salió. Ahora le toca a quien publicó decidir.");
        return "redirect:/solicitudes/" + id;
    }

    private String mostrar(Long id, UsuarioAutenticado usuario, Model model) {
        model.addAttribute("seccion", "explorar");
        model.addAttribute("solicitud", solicitudService.paraOfertar(id, usuario.getIdUsuario()));
        ofertaService.miOfertaEn(id, usuario.getIdUsuario()).ifPresent(mia -> model.addAttribute("miOferta", mia));
        return "solicitud/detalle";
    }
}
