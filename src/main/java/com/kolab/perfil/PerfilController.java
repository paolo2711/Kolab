package com.kolab.perfil;

import com.kolab.categoria.CategoriaResumen;
import com.kolab.demo.ArchivosDeDatos;
import com.kolab.demo.CatalogoDeCategorias;
import com.kolab.usuario.UsuarioAutenticado;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PerfilController {

    private final ArchivosDeDatos archivos;
    private final CatalogoDeCategorias catalogo;
    private final PerfilService perfilService;

    public PerfilController(ArchivosDeDatos archivos, CatalogoDeCategorias catalogo,
                            PerfilService perfilService) {
        this.archivos = archivos;
        this.catalogo = catalogo;
        this.perfilService = perfilService;
    }

    @GetMapping("/perfil")
    public String miPerfil(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        List<Long> ids = perfilService.categoriasDe(usuario.getIdUsuario());
        List<CategoriaResumen> misCategorias = catalogo.categorias().stream()
                .filter(c -> ids.contains(c.id()))
                .toList();

        model.addAttribute("seccion", "perfil");
        model.addAttribute("usuario", usuario);
        model.addAttribute("misCategorias", misCategorias);
        model.addAttribute("perfil", archivos.perfilDe(usuario.esExperto()));
        return "perfil/mi-perfil";
    }
}
