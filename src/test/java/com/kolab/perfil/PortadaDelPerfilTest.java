package com.kolab.perfil;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kolab.usuario.DetallesUsuarioService;
import com.kolab.usuario.RegistroForm;
import com.kolab.usuario.Rol;
import com.kolab.usuario.UsuarioAutenticado;
import com.kolab.usuario.UsuarioService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

// mi perfil lleva la misma portada que ven los demas: la foto de la primera categoria
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PortadaDelPerfilTest {

    private static final long MUSICA = 1L;
    private static final String PORTADA = "class=\"k-banner\"";

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioService usuarioService;
    @Autowired private PerfilService perfilService;
    @Autowired private DetallesUsuarioService detalles;

    @Test
    void conCategoriasMiPerfilYElPublicoTienenPortada() throws Exception {
        Long id = registrar("con.portada@kolab.pe");
        perfilService.guardarCategorias(id, List.of(MUSICA));
        UsuarioAutenticado yo = detalles.cargar("con.portada@kolab.pe", Rol.USUARIO);

        mockMvc.perform(get("/perfil").with(user(yo)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(PORTADA)));
        mockMvc.perform(get("/personas/{id}", id).with(user(otro())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(PORTADA)));
    }

    @Test
    void sinCategoriasNingunoTienePortada() throws Exception {
        Long id = registrar("sin.portada@kolab.pe");
        UsuarioAutenticado yo = detalles.cargar("sin.portada@kolab.pe", Rol.USUARIO);

        mockMvc.perform(get("/perfil").with(user(yo)))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString(PORTADA))));
        mockMvc.perform(get("/personas/{id}", id).with(user(otro())))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString(PORTADA))));
    }

    // la propia ruta publica manda a mi perfil, asi que la ve otra cuenta
    private UsuarioAutenticado otro() {
        registrar("mira.portada@kolab.pe");
        return detalles.cargar("mira.portada@kolab.pe", Rol.USUARIO);
    }

    private Long registrar(String email) {
        RegistroForm form = new RegistroForm();
        form.setNombre("Persona");
        form.setApellidos("Prueba");
        form.setEmail(email);
        form.setPassword("secreto123");
        form.setConfirmacion("secreto123");
        return usuarioService.registrar(form).getId();
    }
}
