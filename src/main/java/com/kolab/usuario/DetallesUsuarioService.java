package com.kolab.usuario;

import com.kolab.common.Fotos;
import com.kolab.perfil.PerfilCategoriaRepository;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DetallesUsuarioService implements UserDetailsService {

    private final UsuarioService usuarioService;
    private final PerfilCategoriaRepository perfilCategoriaRepository;
    private final Fotos fotos;
    private final ControlDeIntentos intentos;

    public DetallesUsuarioService(UsuarioService usuarioService,
                                  PerfilCategoriaRepository perfilCategoriaRepository, Fotos fotos,
                                  ControlDeIntentos intentos) {
        this.usuarioService = usuarioService;
        this.perfilCategoriaRepository = perfilCategoriaRepository;
        this.fotos = fotos;
        this.intentos = intentos;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        // se corta antes de mirar la clave; quien mira la pantalla ve el mismo mensaje de siempre
        if (intentos.estaBloqueado(email)) {
            throw new LockedException("Demasiados intentos con " + email);
        }
        return usuarioService.buscarPorEmail(email)
                .map(usuario -> new UsuarioAutenticado(usuario, ofreceServicios(usuario),
                        fotos.dePersona(usuario.getEmail())))
                .orElseThrow(() -> new UsernameNotFoundException("No hay cuenta con el correo " + email));
    }

    private boolean ofreceServicios(Usuario usuario) {
        return perfilCategoriaRepository.countByPerfilUsuarioId(usuario.getId()) > 0;
    }
}
