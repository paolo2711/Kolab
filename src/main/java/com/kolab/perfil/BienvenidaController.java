package com.kolab.perfil;

import com.kolab.categoria.CategoriaService;
import com.kolab.usuario.SesionActual;
import com.kolab.usuario.UsuarioAutenticado;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BienvenidaController {

    public static final String YA_PREGUNTADO = "bienvenidaVista";

    private final PerfilService perfilService;
    private final CategoriaService categoriaService;
    private final SesionActual sesionActual;

    public BienvenidaController(PerfilService perfilService, CategoriaService categoriaService,
                                SesionActual sesionActual) {
        this.perfilService = perfilService;
        this.categoriaService = categoriaService;
        this.sesionActual = sesionActual;
    }

    @GetMapping("/bienvenida")
    public String preguntar(@AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        model.addAttribute("usuario", usuario);
        model.addAttribute("categorias", categoriaService.activas());
        model.addAttribute("elegidas", perfilService.categoriasDe(usuario.getIdUsuario()));
        return "cuenta/bienvenida";
    }

    @PostMapping("/bienvenida")
    public String guardar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                          @RequestParam(name = "categoria", required = false) List<Long> categorias,
                          HttpSession sesion,
                          RedirectAttributes flash,
                          HttpServletRequest pedido,
                          HttpServletResponse respuesta) {
        List<Long> elegidas = categorias == null ? List.of() : categorias;
        perfilService.guardarCategorias(usuario.getIdUsuario(), elegidas);
        sesionActual.refrescar(pedido, respuesta);
        sesion.setAttribute(YA_PREGUNTADO, true);

        if (elegidas.isEmpty()) {
            flash.addFlashAttribute("aviso", "Listo. Cuando quieras ofrecer algo, lo agregas desde tu cuenta.");
        } else {
            flash.addFlashAttribute("aviso", elegidas.size() == 1
                    ? "Guardado. Ya te van a aparecer solicitudes de esa categoría."
                    : "Guardado. Ya te van a aparecer solicitudes de " + elegidas.size() + " categorías.");
        }
        return "redirect:/";
    }

    @PostMapping("/bienvenida/despues")
    public String despues(HttpSession sesion) {
        sesion.setAttribute(YA_PREGUNTADO, true);
        return "redirect:/";
    }
}
