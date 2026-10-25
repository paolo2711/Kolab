package com.kolab.config;

import com.kolab.common.BitacoraDeSeguridad;
import com.kolab.usuario.DetallesUsuarioService;
import com.kolab.usuario.UsuarioAutenticado;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Cierra la sesión de una cuenta que se suspendió mientras estaba adentro. La sesión guarda cómo
 * estaba la cuenta al ingresar, así que se vuelve a preguntar a la base en cada pedido.
 */
class SesionDeCuentaSuspendida extends OncePerRequestFilter {

    private final DetallesUsuarioService detalles;
    private final BitacoraDeSeguridad bitacora;
    private final String ingreso;

    SesionDeCuentaSuspendida(DetallesUsuarioService detalles, BitacoraDeSeguridad bitacora, String ingreso) {
        this.detalles = detalles;
        this.bitacora = bitacora;
        this.ingreso = ingreso;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest pedido, HttpServletResponse respuesta, FilterChain cadena)
            throws ServletException, IOException {
        Authentication actual = SecurityContextHolder.getContext().getAuthentication();
        if (actual != null && actual.getPrincipal() instanceof UsuarioAutenticado usuario
                && detalles.suspendida(usuario.getIdUsuario())) {
            bitacora.sesionCortada(usuario.getUsername(), pedido.getRemoteAddr());
            new SecurityContextLogoutHandler().logout(pedido, respuesta, actual);
            respuesta.sendRedirect(pedido.getContextPath() + ingreso + "?suspendida");
            return;
        }
        cadena.doFilter(pedido, respuesta);
    }

    // las fotos, el css y el js no necesitan la consulta
    @Override
    protected boolean shouldNotFilter(HttpServletRequest pedido) {
        String ruta = pedido.getRequestURI().substring(pedido.getContextPath().length());
        return ruta.startsWith("/css/") || ruta.startsWith("/js/") || ruta.startsWith("/img/")
                || ruta.startsWith("/fotos/") || ruta.equals("/error");
    }
}
