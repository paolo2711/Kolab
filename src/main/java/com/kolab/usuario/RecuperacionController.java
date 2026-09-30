package com.kolab.usuario;

import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/recuperar")
public class RecuperacionController {

    private final RecuperacionService recuperacionService;
    private final boolean desarrollo;

    public RecuperacionController(RecuperacionService recuperacionService,
                                  @Value("${kolab.desarrollo:false}") boolean desarrollo) {
        this.recuperacionService = recuperacionService;
        this.desarrollo = desarrollo;
    }

    @GetMapping
    public String formulario(Model model) {
        model.addAttribute("recuperarForm", new RecuperarForm());
        return "cuenta/recuperar";
    }

    // no se dice si el correo existe: eso deja averiguar quién tiene cuenta
    @PostMapping
    public String pedirEnlace(@Valid @ModelAttribute RecuperarForm recuperarForm, BindingResult errores,
                              Model model) {
        if (errores.hasErrors()) {
            return "cuenta/recuperar";
        }
        Optional<String> codigo = recuperacionService.generarCodigo(recuperarForm.getEmail());
        model.addAttribute("enviado", true);
        model.addAttribute("correo", recuperarForm.getEmail().trim());
        // sin servidor de correo, en una máquina de desarrollo el enlace se muestra en la pantalla
        if (desarrollo) {
            codigo.ifPresent(c -> model.addAttribute("enlaceDePrueba", "/recuperar/" + c));
        }
        return "cuenta/recuperar";
    }

    @GetMapping("/{codigo}")
    public String nuevaClave(@PathVariable String codigo, Model model) {
        model.addAttribute("codigo", codigo);
        model.addAttribute("valido", recuperacionService.esValido(codigo));
        model.addAttribute("nuevaClaveForm", new NuevaClaveForm());
        return "cuenta/nueva-clave";
    }

    @PostMapping("/{codigo}")
    public String guardarClave(@PathVariable String codigo, @Valid @ModelAttribute NuevaClaveForm nuevaClaveForm,
                               BindingResult errores, Model model, RedirectAttributes flash) {
        if (!nuevaClaveForm.coinciden()) {
            errores.rejectValue("confirmacion", "confirmacion.distinta", "Las dos contraseñas tienen que ser iguales");
        }
        if (errores.hasErrors()) {
            model.addAttribute("codigo", codigo);
            model.addAttribute("valido", true);
            return "cuenta/nueva-clave";
        }
        recuperacionService.cambiarClave(codigo, nuevaClaveForm.getPassword());
        flash.addFlashAttribute("aviso", "Listo, ya tienes contraseña nueva. Ingresa con ella.");
        return "redirect:/login";
    }
}
