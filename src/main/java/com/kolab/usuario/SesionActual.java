package com.kolab.usuario;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

/**
 * Vuelve a leer de la base a quien está en sesión. Se usa cuando cambia algo que el menú muestra,
 * como el nombre o si ofrece servicios, para que se vea sin tener que salir y volver a entrar.
 */
@Component
public class SesionActual {

    private final DetallesUsuarioService detalles;
    private final SecurityContextRepository contextos = new HttpSessionSecurityContextRepository();

    public SesionActual(DetallesUsuarioService detalles) {
        this.detalles = detalles;
    }

    public void refrescar(HttpServletRequest pedido, HttpServletResponse respuesta) {
        Authentication actual = SecurityContextHolder.getContext().getAuthentication();
        UserDetails alDia = detalles.loadUserByUsername(actual.getName());
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                alDia, actual.getCredentials(), alDia.getAuthorities()));
        SecurityContextHolder.setContext(contexto);
        contextos.saveContext(contexto, pedido, respuesta);
    }
}
