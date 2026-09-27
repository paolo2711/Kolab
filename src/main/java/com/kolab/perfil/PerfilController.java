package com.kolab.perfil;

import com.kolab.categoria.CategoriaResumen;
import com.kolab.demo.ArchivosDeDatos;
import com.kolab.demo.CatalogoDeCategorias;
import com.kolab.usuario.UsuarioAutenticado;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping("/perfil/editar")
    public String formularioEditar(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        PerfilResumen perfil = archivos.perfilDe(usuario.esExperto());

        PerfilForm form = new PerfilForm();
        form.setNombre(usuario.getNombre());
        form.setApellidos(apellidosDe(usuario));
        form.setUbicacion(perfil.ubicacion());
        form.setDescripcion(perfil.descripcion());

        model.addAttribute("seccion", "perfil");
        model.addAttribute("usuario", usuario);
        model.addAttribute("perfilForm", form);
        return "perfil/editar";
    }

    @PostMapping("/perfil/editar")
    public String guardar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                          @Valid @ModelAttribute PerfilForm perfilForm,
                          BindingResult errores,
                          Model model,
                          RedirectAttributes flash) {
        if (errores.hasErrors()) {
            model.addAttribute("seccion", "perfil");
            model.addAttribute("usuario", usuario);
            return "perfil/editar";
        }
        flash.addFlashAttribute("aviso", "Perfil actualizado.");
        return "redirect:/perfil";
    }

    private String apellidosDe(UsuarioAutenticado usuario) {
        String completo = usuario.getNombreCompleto();
        int corte = completo.indexOf(' ');
        return corte < 0 ? "" : completo.substring(corte + 1);
    }
}
