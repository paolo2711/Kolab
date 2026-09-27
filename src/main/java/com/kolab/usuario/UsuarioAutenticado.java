package com.kolab.usuario;

import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class UsuarioAutenticado extends User {

    private final Long idUsuario;
    private final String nombre;
    private final String nombreCompleto;
    private final String iniciales;
    private final boolean ofreceServicios;

    public UsuarioAutenticado(Usuario usuario, boolean ofreceServicios) {
        super(usuario.getEmail(),
                usuario.getPasswordHash(),
                usuario.getEstado() == EstadoUsuario.ACTIVO,
                true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_USUARIO")));
        this.ofreceServicios = ofreceServicios;
        this.idUsuario = usuario.getId();
        this.nombre = usuario.getNombre();
        this.nombreCompleto = usuario.getNombreCompleto();
        this.iniciales = inicialesDe(usuario);
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getIniciales() {
        return iniciales;
    }

    // ofrece quien haya declarado alguna categoria
    public boolean ofreceServicios() {
        return ofreceServicios;
    }

    public boolean esExperto() {
        return ofreceServicios;
    }

    private static String inicialesDe(Usuario usuario) {
        String apellidos = usuario.getApellidos();
        char primera = usuario.getNombre().charAt(0);
        if (apellidos == null || apellidos.isBlank()) {
            return String.valueOf(primera);
        }
        return "" + primera + apellidos.charAt(0);
    }
}
