package com.kolab.config;

import com.kolab.usuario.UsuarioAutenticado;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Quien administra no ve el mercado: si abre una pantalla pública, como la portada o el ingreso
 * normal, vuelve a su panel. Las privadas ya las rechaza la seguridad.
 */
class PanelAparte implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest pedido, HttpServletResponse respuesta, Object controlador)
            throws IOException {
        if (pedido.getUserPrincipal() instanceof Authentication autenticado
                && autenticado.getPrincipal() instanceof UsuarioAutenticado usuario
                && usuario.esAdministrador()) {
            respuesta.sendRedirect(pedido.getContextPath() + "/admin");
            return false;
        }
        return true;
    }
}
