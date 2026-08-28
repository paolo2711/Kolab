package com.kolab.mensaje;

import com.kolab.demo.MensajesDeEjemplo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class MensajeController {

    private final MensajesDeEjemplo mensajes;

    public MensajeController(MensajesDeEjemplo mensajes) {
        this.mensajes = mensajes;
    }

    @GetMapping("/mensajes")
    public String bandeja(Model model) {
        model.addAttribute("seccion", "mensajes");
        model.addAttribute("conversaciones", mensajes.conversaciones());
        return "mensaje/bandeja";
    }

    @GetMapping("/mensajes/{idOferta}")
    public String conversacion(@PathVariable Long idOferta, Model model) {
        model.addAttribute("seccion", "mensajes");
        model.addAttribute("conversacion", mensajes.conversacion(idOferta));
        return "mensaje/conversacion";
    }
}
