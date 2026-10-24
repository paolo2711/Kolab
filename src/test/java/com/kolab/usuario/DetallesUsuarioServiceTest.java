package com.kolab.usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kolab.common.Fotos;
import com.kolab.perfil.PerfilCategoriaRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

class DetallesUsuarioServiceTest {

    private final UsuarioService usuarioService = Mockito.mock(UsuarioService.class);
    private final ControlDeIntentos intentos = new ControlDeIntentos(5, 15);
    private final DetallesUsuarioService detalles = new DetallesUsuarioService(usuarioService,
            Mockito.mock(PerfilCategoriaRepository.class), Mockito.mock(Fotos.class), intentos);

    @Test
    void cadaIngresoSoloReconoceCuentasDeSuRol() {
        cuenta("admin@kolab.pe", Rol.ADMINISTRADOR);
        cuenta("experto@kolab.pe", Rol.USUARIO);

        assertThat(detalles.cargar("admin@kolab.pe", Rol.ADMINISTRADOR).esAdministrador()).isTrue();
        assertThat(detalles.cargar("experto@kolab.pe", Rol.USUARIO).esAdministrador()).isFalse();
    }

    @Test
    void laCuentaDeOtroRolSeRechazaComoSiNoExistiera() {
        cuenta("admin@kolab.pe", Rol.ADMINISTRADOR);
        cuenta("experto@kolab.pe", Rol.USUARIO);

        assertThatThrownBy(() -> detalles.cargar("admin@kolab.pe", Rol.USUARIO))
                .isInstanceOf(UsernameNotFoundException.class);
        assertThatThrownBy(() -> detalles.cargar("experto@kolab.pe", Rol.ADMINISTRADOR))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void elBloqueoPorIntentosValeTambienParaElPanel() {
        cuenta("admin@kolab.pe", Rol.ADMINISTRADOR);
        for (int i = 0; i < 5; i++) {
            intentos.fallo("admin@kolab.pe");
        }

        assertThatThrownBy(() -> detalles.cargar("admin@kolab.pe", Rol.ADMINISTRADOR))
                .isInstanceOf(LockedException.class);
    }

    @Test
    void quienAdministraSoloTieneElPermisoDelPanel() {
        cuenta("admin@kolab.pe", Rol.ADMINISTRADOR);

        assertThat(detalles.cargar("admin@kolab.pe", Rol.ADMINISTRADOR).getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_ADMINISTRADOR");
    }

    private void cuenta(String email, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setId((long) email.hashCode());
        usuario.setNombre("Nombre");
        usuario.setApellidos("Apellido");
        usuario.setEmail(email);
        usuario.setPasswordHash("$2a$10$hashfalso");
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuario.setRol(rol);
        Mockito.when(usuarioService.buscarPorEmail(email)).thenReturn(Optional.of(usuario));
    }
}
