package com.kolab.usuario;

import com.kolab.common.EmailYaRegistradoException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CuentaController {

    private final UsuarioService usuarioService;

    public CuentaController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String login() {
        return "cuenta/login";
    }

    @GetMapping("/registro")
    public String formularioRegistro(Model model) {
        model.addAttribute("registroForm", new RegistroForm());
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
}
