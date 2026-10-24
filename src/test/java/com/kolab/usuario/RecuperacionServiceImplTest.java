package com.kolab.usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.kolab.common.OperacionNoPermitidaException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class RecuperacionServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private RecuperacionServiceImpl servicio;
    private Usuario paolo;

    @BeforeEach
    void preparar() {
        servicio = new RecuperacionServiceImpl(usuarioRepository, new BCryptPasswordEncoder(), "clave-de-prueba");
        paolo = new Usuario();
        paolo.setId(7L);
        paolo.setEmail("paolo@kolab.pe");
        paolo.setPasswordHash("hash-anterior");
        paolo.setEstado(EstadoUsuario.ACTIVO);
    }

    @Test
    void unCorreoSinCuentaNoGeneraEnlace() {
        when(usuarioRepository.findByEmail("nadie@kolab.pe")).thenReturn(Optional.empty());

        assertThat(servicio.generarCodigo("nadie@kolab.pe")).isEmpty();
    }

    @Test
    void elEnlaceRecienGeneradoSirve() {
        String codigo = codigoDePaolo();

        assertThat(servicio.esValido(codigo)).isTrue();
    }

    @Test
    void unEnlaceAlteradoNoSirve() {
        String codigo = codigoDePaolo();
        String alterado = codigo.substring(0, codigo.length() - 2) + "xx";

        assertThat(servicio.esValido(alterado)).isFalse();
        assertThat(servicio.esValido("cualquier-cosa")).isFalse();
    }

    @Test
    void despuesDeUsarloElMismoEnlaceYaNoSirve() {
        String codigo = codigoDePaolo();

        servicio.cambiarClave(codigo, "nuevaclave1");

        assertThat(paolo.getPasswordHash()).isNotEqualTo("hash-anterior");
        assertThat(servicio.esValido(codigo)).isFalse();
        assertThatThrownBy(() -> servicio.cambiarClave(codigo, "otraclave2"))
                .isInstanceOf(OperacionNoPermitidaException.class);
    }

    private String codigoDePaolo() {
        when(usuarioRepository.findByEmail("paolo@kolab.pe")).thenReturn(Optional.of(paolo));
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(paolo));
        return servicio.generarCodigo("paolo@kolab.pe").orElseThrow();
    }
}
