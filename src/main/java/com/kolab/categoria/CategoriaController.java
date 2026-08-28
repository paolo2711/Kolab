package com.kolab.categoria;

import com.kolab.demo.ArchivosDeDatos;
import com.kolab.demo.CatalogoDeCategorias;
import com.kolab.demo.DirectorioDePersonas;
import com.kolab.demo.SolicitudesDeEjemplo;
import com.kolab.usuario.UsuarioAutenticado;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class CategoriaController {

    private final ArchivosDeDatos archivos;
    private final CatalogoDeCategorias categorias;
    private final DirectorioDePersonas directorio;
    private final SolicitudesDeEjemplo solicitudes;

    public CategoriaController(ArchivosDeDatos archivos, CatalogoDeCategorias categorias,
                               DirectorioDePersonas directorio, SolicitudesDeEjemplo solicitudes) {
        this.archivos = archivos;
        this.categorias = categorias;
        this.directorio = directorio;
        this.solicitudes = solicitudes;
    }

    @GetMapping("/categorias/{id}")
    public String detalle(@PathVariable Long id,
                          @AuthenticationPrincipal UsuarioAutenticado usuario,
                          Model model) {
        var perfil = archivos.perfilDe(usuario.esExperto());

        model.addAttribute("seccion", "explorar");
        model.addAttribute("categoria", categorias.categoria(id));
        model.addAttribute("precios", categorias.preciosDe(id));
        model.addAttribute("expertos", directorio.expertosDe(id));
        model.addAttribute("solicitudes",
                solicitudes.conDistancia(solicitudes.abiertasDe(id), perfil.latitud(), perfil.longitud()));
        return "categoria/detalle";
    }
}
