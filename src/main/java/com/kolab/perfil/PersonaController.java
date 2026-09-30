package com.kolab.perfil;

import com.kolab.usuario.UsuarioAutenticado;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// el perfil que ve el resto. desde aquí se le propone un servicio directo, sin pasar por el catálogo
@Controller
public class PersonaController {

    private final DirectorioService directorioService;

    public PersonaController(DirectorioService directorioService) {
        this.directorioService = directorioService;
    }

    @GetMapping("/personas/{id}")
    public String publico(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario,
                          Model model) {
        if (id.equals(usuario.getIdUsuario())) {
            return "redirect:/perfil";
        }
        model.addAttribute("seccion", "explorar");
        model.addAttribute("perfil", directorioService.publico(id));
        return "perfil/persona";
    }
}
