package com.kolab.mensaje;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MensajeController {

    private static final int POR_PAGINA = 20;

    private final MensajeService mensajeService;

    public MensajeController(MensajeService mensajeService) {
        this.mensajeService = mensajeService;
    }

    @GetMapping("/mensajes")
    public String bandeja(@RequestParam(defaultValue = "0") int pagina,
                          @AuthenticationPrincipal UsuarioAutenticado usuario, Model model) {
        model.addAttribute("seccion", "mensajes");
        model.addAttribute("conversaciones",
                mensajeService.bandeja(usuario.getIdUsuario(), PageRequest.of(Math.max(pagina, 0), POR_PAGINA)));
        return "mensaje/bandeja";
    }

    @GetMapping("/mensajes/{idOferta}")
    public String conversacion(@PathVariable Long idOferta, @AuthenticationPrincipal UsuarioAutenticado usuario,
                               Model model) {
        model.addAttribute("seccion", "mensajes");
        model.addAttribute("conversacion", mensajeService.abrir(idOferta, usuario.getIdUsuario()));
        return "mensaje/conversacion";
    }

    // se escribe desde tres pantallas distintas; cada una dice a dónde volver
    @PostMapping("/mensajes/{idOferta}")
    public String enviar(@PathVariable Long idOferta, @Valid @ModelAttribute MensajeForm mensajeForm,
                         BindingResult errores, @RequestParam(defaultValue = "") String volver,
                         @AuthenticationPrincipal UsuarioAutenticado usuario, RedirectAttributes flash) {
        if (errores.hasErrors()) {
            flash.addFlashAttribute("error", errores.getFieldError().getDefaultMessage());
        } else {
            mensajeService.enviar(idOferta, mensajeForm.getContenido(), usuario.getIdUsuario());
        }
        return "redirect:" + destino(volver, idOferta);
    }

    // solo rutas de la propia aplicación: nada de redirigir a otro sitio
    private String destino(String volver, Long idOferta) {
        if (volver.startsWith("/") && !volver.startsWith("//") && !volver.contains("\\")) {
            return volver;
        }
        return "/mensajes/" + idOferta;
    }
}
