package com.kolab.solicitud;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesRegex;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kolab.usuario.DetallesUsuarioService;
import com.kolab.usuario.RegistroForm;
import com.kolab.usuario.Rol;
import com.kolab.usuario.UsuarioAutenticado;
import com.kolab.usuario.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

// si el formulario vuelve con un error, la vista previa no arranca vacia: muestra lo que ya se escribio
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VistaPreviaDePublicarTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioService usuarioService;
    @Autowired private DetallesUsuarioService detalles;

    private UsuarioAutenticado yo;

    @BeforeEach
    void unaCuenta() {
        RegistroForm form = new RegistroForm();
        form.setNombre("Persona");
        form.setApellidos("Prueba");
        form.setEmail("publica.previa@kolab.pe");
        form.setPassword("secreto123");
        form.setConfirmacion("secreto123");
        usuarioService.registrar(form);
        yo = detalles.cargar("publica.previa@kolab.pe", Rol.USUARIO);
    }

    @Test
    void alVolverConUnErrorLaVistaPreviaTraeLoEscrito() throws Exception {
        mockMvc.perform(post("/mis-solicitudes").with(csrf()).with(user(yo))
                        .param("titulo", "Clases de cajón peruano")
                        .param("idCategoria", "1")
                        .param("modalidad", "VIRTUAL")
                        .param("precioPropuesto", "85")
                        .param("descripcion", "corta"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        matchesRegex("(?s).*id=\"vpTitulo\">Clases de cajón peruano<.*"),
                        matchesRegex("(?s).*id=\"vpCategoria\">Música<.*"),
                        matchesRegex("(?s).*id=\"vpModalidad\">Virtual<.*"),
                        matchesRegex("(?s).*id=\"vpPrecio\">85.00<.*"))));
    }

    @Test
    void enBlancoLaVistaPreviaMuestraDondeVaCadaCosa() throws Exception {
        mockMvc.perform(get("/mis-solicitudes/nueva").with(user(yo)))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("k-vp-vacio\" id=\"vpTitulo\">Tu título va aquí<"),
                        containsString("id=\"vpCategoria\">Categoría<"),
                        containsString("id=\"vpPrecio\">0.00<"))));
    }
}
