package com.kolab.usuario;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.kolab.common.EmailYaRegistradoException;
import com.kolab.config.SecurityConfig;
import com.kolab.demo.ArchivosDeDatos;
import com.kolab.demo.CatalogoDeCategorias;
import com.kolab.demo.DirectorioDePersonas;
import com.kolab.demo.OfertasDeEjemplo;
import com.kolab.demo.SolicitudesDeEjemplo;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CuentaController.class)
@Import({SecurityConfig.class, ArchivosDeDatos.class, CatalogoDeCategorias.class,
        DirectorioDePersonas.class, OfertasDeEjemplo.class, SolicitudesDeEjemplo.class})
class CuentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private DetallesUsuarioService detallesUsuarioService;

    @Test
    void laPantallaDeLoginEsPublica() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("cuenta/login"));
    }

    @Test
    void elBotonDeExpertoLlegaAlFormularioConEsePerfilMarcado() throws Exception {
        mockMvc.perform(get("/registro").param("perfil", "EXPERTO"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("registroForm",
                        org.hamcrest.Matchers.hasProperty("tipoPerfil", org.hamcrest.Matchers.is(TipoPerfil.EXPERTO))));
    }

    @Test
    void unRegistroValidoRedirigeAlLogin() throws Exception {
        mockMvc.perform(post("/registro").with(csrf())
                        .param("nombre", "Marco")
                        .param("apellidos", "Campos Llanos")
                        .param("email", "marco@kolab.pe")
                        .param("telefono", "987654321")
                        .param("password", "secreto123")
                        .param("confirmacion", "secreto123")
                        .param("tipoPerfil", "CLIENTE"))
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void siLasContrasenasNoCoincidenVuelveAlFormulario() throws Exception {
        mockMvc.perform(post("/registro").with(csrf())
                        .param("nombre", "Marco")
                        .param("apellidos", "Campos Llanos")
                        .param("email", "marco@kolab.pe")
                        .param("password", "secreto123")
                        .param("confirmacion", "otracosa456")
                        .param("tipoPerfil", "CLIENTE"))
                .andExpect(status().isOk())
                .andExpect(view().name("cuenta/registro"))
                .andExpect(model().attributeHasFieldErrors("registroForm", "confirmacion"));

        verify(usuarioService, never()).registrar(any());
    }

    @Test
    void elCorreoRepetidoSeMuestraComoErrorDelCampo() throws Exception {
        Mockito.when(usuarioService.registrar(any()))
                .thenThrow(new EmailYaRegistradoException("marco@kolab.pe"));

        mockMvc.perform(post("/registro").with(csrf())
                        .param("nombre", "Marco")
                        .param("apellidos", "Campos Llanos")
                        .param("email", "marco@kolab.pe")
                        .param("password", "secreto123")
                        .param("confirmacion", "secreto123")
                        .param("tipoPerfil", "CLIENTE"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("registroForm", "email"));
    }

    @Test
    void unaRutaPrivadaMandaAlLogin() throws Exception {
        mockMvc.perform(get("/servicios"))
                .andExpect(status().is3xxRedirection());
    }
}
