package com.kolab.perfil;

import com.kolab.categoria.CategoriaResumen;
import com.kolab.demo.CatalogoDeCategorias;
import com.kolab.demo.DirectorioDePersonas;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// el perfil que ve el resto. desde aqui se le propone un servicio directo, sin pasar por el
// catalogo
@Controller
public class PersonaController {

    private final DirectorioDePersonas directorio;
    private final CatalogoDeCategorias catalogo;

    public PersonaController(DirectorioDePersonas directorio, CatalogoDeCategorias catalogo) {
        this.directorio = directorio;
        this.catalogo = catalogo;
    }

    @GetMapping("/personas/{id}")
    public String publico(@PathVariable Long id, Model model) {
        List<Long> suyas = directorio.categoriasDe(id);
        List<CategoriaResumen> categorias = catalogo.categorias().stream()
                .filter(c -> suyas.contains(c.id()))
                .toList();

        model.addAttribute("experto", directorio.experto(id));
        model.addAttribute("categorias", categorias);
        return "perfil/persona";
    }
}
