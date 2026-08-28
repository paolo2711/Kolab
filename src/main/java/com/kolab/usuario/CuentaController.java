package com.kolab.usuario;

import com.kolab.common.EmailYaRegistradoException;
import com.kolab.demo.SolicitudesDeEjemplo;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CuentaController {

    private final UsuarioService usuarioService;
    private final SolicitudesDeEjemplo solicitudes;

    public CuentaController(UsuarioService usuarioService, SolicitudesDeEjemplo solicitudes) {
        this.usuarioService = usuarioService;
        this.solicitudes = solicitudes;
    }

    @GetMapping("/login")
    public String login(Model model) {
        solicitudes.catalogo().stream().findFirst()
                .ifPresent(muestra -> model.addAttribute("muestra", muestra));
        return "cuenta/login";
    }

    @GetMapping("/registro")
    public String formularioRegistro(@RequestParam(required = false) String perfil, Model model) {
        RegistroForm form = new RegistroForm();
        form.setTipoPerfil(perfilElegido(perfil));
        model.addAttribute("registroForm", form);
        return "cuenta/registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute RegistroForm registroForm,
                            BindingResult errores,
                            RedirectAttributes flash) {

        if (!registroForm.contrasenasCoinciden()) {
            errores.rejectValue("confirmacion", "confirmacion.distinta", "Las dos contraseñas tienen que ser iguales");
        }
        if (errores.hasErrors()) {
            return "cuenta/registro";
        }

        try {
            usuarioService.registrar(registroForm);
        } catch (EmailYaRegistradoException e) {
            errores.rejectValue("email", "email.duplicado", "Ese correo ya tiene una cuenta en KOLAB");
            return "cuenta/registro";
        }

        flash.addFlashAttribute("aviso", "Tu cuenta está lista. Ingresa con tu correo y contraseña.");
        return "redirect:/login";
    }

    // el tipo llega en la url desde los dos botones de la portada, cualquier otra cosa es cliente
    private TipoPerfil perfilElegido(String perfil) {
        if (perfil == null) {
            return TipoPerfil.CLIENTE;
        }
        return "EXPERTO".equalsIgnoreCase(perfil) ? TipoPerfil.EXPERTO : TipoPerfil.CLIENTE;
    }
}
