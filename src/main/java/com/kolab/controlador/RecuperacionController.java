package com.kolab.controlador;

import com.kolab.modelo.dto.NuevaClaveForm;
import com.kolab.modelo.dto.RecuperarForm;
import com.kolab.seguridad.BitacoraDeSeguridad;
import com.kolab.servicio.CorreoService;
import com.kolab.servicio.RecuperacionService;
import jakarta.servlet.http.HttpServletRequest;
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
    private final CorreoService correoService;
    private final BitacoraDeSeguridad bitacora;
    private final boolean desarrollo;
    private final String urlPublica;

    public RecuperacionController(RecuperacionService recuperacionService, CorreoService correoService,
                                  BitacoraDeSeguridad bitacora,
                                  @Value("${kolab.desarrollo:false}") boolean desarrollo,
                                  @Value("${kolab.url-publica:http://localhost:8080}") String urlPublica) {
        this.recuperacionService = recuperacionService;
        this.correoService = correoService;
        this.bitacora = bitacora;
        this.desarrollo = desarrollo;
        this.urlPublica = urlPublica;
    }

    @GetMapping
    public String formulario(Model model) {
        model.addAttribute("recuperarForm", new RecuperarForm());
        return "cuenta/recuperar";
    }

    // no se dice si el correo existe: eso deja averiguar quién tiene cuenta
    @PostMapping
    public String pedirEnlace(@Valid @ModelAttribute RecuperarForm recuperarForm, BindingResult errores,
                              Model model, HttpServletRequest pedido) {
        if (errores.hasErrors()) {
            return "cuenta/recuperar";
        }
        String email = recuperarForm.getEmail().trim();
        Optional<String> codigo = recuperacionService.generarCodigo(email);
        bitacora.pidioRecuperar(email, pedido.getRemoteAddr(), codigo.isPresent());
        // el enlace se arma con la direccion configurada y no con el Host del pedido, que lo cambia cualquiera
        codigo.ifPresent(c -> correoService.enviarEnlaceDeRecuperacion(email, urlPublica + "/recuperar/" + c));
        model.addAttribute("enviado", true);
        model.addAttribute("correo", email);
        // en una maquina de desarrollo el enlace sale tambien en la pantalla
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
                               BindingResult errores, Model model, RedirectAttributes flash,
                               HttpServletRequest pedido) {
        if (!nuevaClaveForm.coinciden()) {
            errores.rejectValue("confirmacion", "confirmacion.distinta", "Las dos contraseñas tienen que ser iguales");
        }
        if (errores.hasErrors()) {
            model.addAttribute("codigo", codigo);
            model.addAttribute("valido", true);
            return "cuenta/nueva-clave";
        }
        bitacora.cambioDeClave(recuperacionService.cambiarClave(codigo, nuevaClaveForm.getPassword()),
                pedido.getRemoteAddr());
        flash.addFlashAttribute("aviso", "Listo, ya tienes contraseña nueva. Ingresa con ella.");
        return "redirect:/login";
    }
}
