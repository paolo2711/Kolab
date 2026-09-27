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

    @GetMapping("/recuperar")
    public String formularioRecuperar(Model model) {
        model.addAttribute("recuperarForm", new RecuperarForm());
        return "cuenta/recuperar";
    }

    // no se dice si el correo existe: eso deja averiguar quien tiene cuenta
    @PostMapping("/recuperar")
    public String recuperar(@Valid @ModelAttribute RecuperarForm recuperarForm, BindingResult errores,
                            Model model) {
        if (errores.hasErrors()) {
            return "cuenta/recuperar";
        }
        model.addAttribute("enviado", true);
        model.addAttribute("correo", recuperarForm.getEmail().trim());
        return "cuenta/recuperar";
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
