package com.kolab.perfil;

import com.kolab.categoria.CategoriaService;
import com.kolab.usuario.SesionActual;
import com.kolab.usuario.UsuarioAutenticado;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PerfilController {

    private final PerfilService perfilService;
    private final CategoriaService categoriaService;
    private final SesionActual sesion;

    public PerfilController(PerfilService perfilService, CategoriaService categoriaService, SesionActual sesion) {
        this.perfilService = perfilService;
        this.categoriaService = categoriaService;
        this.sesion = sesion;
    }

    @GetMapping("/perfil")
    public String miPerfil(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        List<Long> ids = perfilService.categoriasDe(usuario.getIdUsuario());
        model.addAttribute("seccion", "perfil");
        model.addAttribute("usuario", usuario);
        model.addAttribute("misCategorias", categoriaService.activas().stream()
                .filter(c -> ids.contains(c.id())).toList());
        model.addAttribute("perfil", perfilService.miPerfil(usuario.getIdUsuario()));
        return "perfil/mi-perfil";
    }

    @GetMapping("/perfil/editar")
    public String formularioEditar(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        model.addAttribute("seccion", "perfil");
        model.addAttribute("usuario", usuario);
        model.addAttribute("perfilForm", perfilService.formularioDe(usuario.getIdUsuario()));
        return "perfil/editar";
    }

    @PostMapping("/perfil/editar")
    public String guardar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                          @Valid @ModelAttribute PerfilForm perfilForm,
                          BindingResult errores,
                          Model model,
                          RedirectAttributes flash,
                          HttpServletRequest pedido,
                          HttpServletResponse respuesta) {
        if (errores.hasErrors()) {
            model.addAttribute("seccion", "perfil");
            model.addAttribute("usuario", usuario);
            return "perfil/editar";
        }
        perfilService.actualizar(usuario.getIdUsuario(), perfilForm);
        sesion.refrescar(pedido, respuesta);
        flash.addFlashAttribute("aviso", "Perfil actualizado.");
        return "redirect:/perfil";
    }
}
