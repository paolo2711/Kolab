package com.kolab.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class ManejoDeErrores {

    // la persona vuelve a la pantalla desde la que lo intentó y ve por qué no se pudo
    @ExceptionHandler(OperacionNoPermitidaException.class)
    public String noPermitida(OperacionNoPermitidaException e, HttpServletRequest pedido,
                              RedirectAttributes flash) {
        flash.addFlashAttribute("error", e.getMessage());
        return "redirect:" + deDondeVino(pedido);
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
