package com.kolab.usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kolab.common.EmailYaRegistradoException;
import com.kolab.perfil.Perfil;
import com.kolab.perfil.PerfilRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PerfilRepository perfilRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UsuarioServiceImpl usuarioService;

    @BeforeEach
    void prepararServicio() {
        usuarioService = new UsuarioServiceImpl(usuarioRepository, perfilRepository, passwordEncoder);
    }

    @Test
    void guardaElCorreoEnMinusculasYSinEspacios() {
        RegistroForm form = formularioValido("  Paolo.Rodriguez@Gmail.com  ");
        when(usuarioRepository.existsByEmail("paolo.rodriguez@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");

        usuarioService.registrar(form);

        assertThat(usuarioGuardado().getEmail()).isEqualTo("paolo.rodriguez@gmail.com");
    }

    @Test
    void nuncaGuardaLaContrasenaEnClaro() {
        RegistroForm form = formularioValido("cliente@kolab.pe");
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("secreto123")).thenReturn("$2a$10$hashfalso");

        usuarioService.registrar(form);

        assertThat(usuarioGuardado().getPasswordHash()).isEqualTo("$2a$10$hashfalso");
        verify(passwordEncoder, times(1)).encode("secreto123");
    }

    @Test
    void rechazaUnCorreoQueYaTieneCuenta() {
        RegistroForm form = formularioValido("repetido@kolab.pe");
        when(usuarioRepository.existsByEmail("repetido@kolab.pe")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.registrar(form))
                .isInstanceOf(EmailYaRegistradoException.class);

        verify(usuarioRepository, never()).save(any());
        verify(perfilRepository, never()).save(any());
    }

    @Test
    void alRegistrarseLaCuentaYaTieneSuPerfil() {
        RegistroForm form = formularioValido("cliente@kolab.pe");
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");

        Usuario usuario = usuarioService.registrar(form);

        ArgumentCaptor<Perfil> captor = ArgumentCaptor.forClass(Perfil.class);
        verify(perfilRepository).save(captor.capture());
        assertThat(captor.getValue().getUsuario()).isSameAs(usuario);
        assertThat(captor.getValue().sinCalificar()).isTrue();
    }

    @Test
    void elTelefonoVacioSeGuardaComoNulo() {
        RegistroForm form = formularioValido("cliente@kolab.pe");
        form.setTelefono("   ");
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");

        usuarioService.registrar(form);

        assertThat(usuarioGuardado().getTelefono()).isNull();
    }

    private Usuario usuarioGuardado() {
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        return captor.getValue();
    }

    private RegistroForm formularioValido(String email) {
        RegistroForm form = new RegistroForm();
        form.setNombre("Paolo");
        form.setApellidos("Rodriguez Paredes");
        form.setEmail(email);
        form.setTelefono("987654321");
        form.setPassword("secreto123");
        form.setConfirmacion("secreto123");
        return form;
    }
}
