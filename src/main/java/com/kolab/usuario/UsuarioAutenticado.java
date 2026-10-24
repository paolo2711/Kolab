package com.kolab.usuario;

import com.kolab.perfil.Personas;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class UsuarioAutenticado extends User {

    private final Long idUsuario;
    private final String nombre;
    private final String nombreCompleto;
    private final String iniciales;
    private final boolean ofreceServicios;
    private final String foto;
    private final boolean administrador;

    public UsuarioAutenticado(Usuario usuario, boolean ofreceServicios, String foto) {
        super(usuario.getEmail(),
                usuario.getPasswordHash(),
                usuario.getEstado() == EstadoUsuario.ACTIVO,
                true, true, true,
                permisos(usuario));
        this.ofreceServicios = ofreceServicios;
        this.idUsuario = usuario.getId();
        this.nombre = usuario.getNombre();
        this.nombreCompleto = usuario.getNombreCompleto();
        this.iniciales = Personas.inicialesDe(usuario);
        this.foto = foto;
        this.administrador = usuario.esAdministrador();
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

    // null si no hay foto: el menú pinta las iniciales
    public String getFoto() {
        return foto;
    }

    // ofrece quien haya declarado alguna categoria
    public boolean ofreceServicios() {
        return ofreceServicios;
    }

    public boolean esAdministrador() {
        return administrador;
    }

    // quien administra no es usuario del mercado: no publica, no oferta ni escribe, solo ve el panel
    private static List<SimpleGrantedAuthority> permisos(Usuario usuario) {
        return List.of(new SimpleGrantedAuthority(usuario.esAdministrador() ? "ROLE_ADMINISTRADOR" : "ROLE_USUARIO"));
    }
}
