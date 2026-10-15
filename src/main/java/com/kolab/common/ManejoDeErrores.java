package com.kolab.common;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class ManejoDeErrores {

    private final BitacoraDeSeguridad bitacora;

    public ManejoDeErrores(BitacoraDeSeguridad bitacora) {
        this.bitacora = bitacora;
    }

    // la persona vuelve a la pantalla desde la que lo intentó y ve por qué no se pudo
    @ExceptionHandler(OperacionNoPermitidaException.class)
    public String noPermitida(OperacionNoPermitidaException e, HttpServletRequest pedido,
                              RedirectAttributes flash) {
        flash.addFlashAttribute("error", e.getMessage());
        return "redirect:" + deDondeVino(pedido);
    }

    /**
     * Pedir algo ajeno y pedir algo que no existe dan la misma respuesta, así que la diferencia
     * solo se ve en la bitácora: varios de estos seguidos son alguien probando ids.
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String noEncontrado(HttpServletRequest pedido, Principal quien, Model model) {
        bitacora.noEsSuyo(pedido.getRequestURI(), quien == null ? null : quien.getName(),
                pedido.getRemoteAddr());
        model.addAttribute("status", HttpStatus.NOT_FOUND.value());
        return "error";
    }

    // solo se vuelve a una ruta de la propia aplicacion, nunca a otro sitio
    private String deDondeVino(HttpServletRequest pedido) {
        String anterior = pedido.getHeader("Referer");
        String base = pedido.getScheme() + "://" + pedido.getServerName();
        if (anterior == null || !anterior.startsWith(base)) {
            return "/";
        }
        int inicio = anterior.indexOf('/', base.length());
        return inicio < 0 ? "/" : anterior.substring(inicio);
    }
}
