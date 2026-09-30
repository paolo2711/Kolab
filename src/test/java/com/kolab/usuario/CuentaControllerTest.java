package com.kolab.usuario;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.kolab.common.EmailYaRegistradoException;
import com.kolab.common.Fotos;
import com.kolab.config.SecurityConfig;
import com.kolab.mensaje.MensajeService;
import com.kolab.oferta.OfertaService;
import com.kolab.solicitud.SolicitudService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CuentaController.class)
@Import(SecurityConfig.class)
class CuentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private DetallesUsuarioService detallesUsuarioService;

    @MockitoBean
    private Fotos fotos;

    @MockitoBean
    private MensajeService mensajeService;

    @MockitoBean
    private SolicitudService solicitudService;

    @MockitoBean
    private OfertaService ofertaService;

    @Test
    void laPantallaDeLoginEsPublica() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("cuenta/login"));
    }

    @Test
    void fueraDeDesarrolloElLoginNoMuestraLasHerramientas() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("reiniciar datos"))));
    }

    @Test
    void elRegistroSeAbreSinPedirTipoDeCuenta() throws Exception {
        mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(view().name("cuenta/registro"))
                .andExpect(model().attributeExists("registroForm"));
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
                        )
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
                        )
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
                        )
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("registroForm", "email"));
    }

    @Test
    void unaRutaPrivadaMandaAlLogin() throws Exception {
        mockMvc.perform(get("/servicios"))
                .andExpect(status().is3xxRedirection());
    }
}
