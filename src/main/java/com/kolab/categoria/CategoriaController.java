package com.kolab.categoria;

import com.kolab.perfil.DirectorioService;
import com.kolab.solicitud.CatalogoService;
import com.kolab.usuario.UsuarioAutenticado;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class CategoriaController {

    private static final int EXPERTOS = 6;
    private static final int SOLICITUDES = 10;

    private final CategoriaService categoriaService;
    private final DirectorioService directorioService;
    private final CatalogoService catalogoService;

    public CategoriaController(CategoriaService categoriaService, DirectorioService directorioService,
                               CatalogoService catalogoService) {
        this.categoriaService = categoriaService;
        this.directorioService = directorioService;
        this.catalogoService = catalogoService;
    }

    @GetMapping("/categorias/{id}")
    public String detalle(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario,
                          Model model) {
        model.addAttribute("seccion", "explorar");
        model.addAttribute("categoria", categoriaService.resumen(id));
        model.addAttribute("precios", categoriaService.precios(id));
        model.addAttribute("expertos", directorioService.queOfrecen(id, EXPERTOS));
        model.addAttribute("solicitudes", catalogoService.abiertasEn(id, usuario.getIdUsuario(), SOLICITUDES));
        return "categoria/detalle";
    }
}
