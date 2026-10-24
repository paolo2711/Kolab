package com.kolab.desarrollo;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kolab.common.BitacoraDeSeguridad;
import com.kolab.common.Fotos;
import com.kolab.config.SecurityConfig;
import com.kolab.mensaje.MensajeService;
import com.kolab.oferta.OfertaService;
import com.kolab.solicitud.SolicitudService;
import com.kolab.usuario.DetallesUsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(HerramientasController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "kolab.desarrollo=true")
class HerramientasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BitacoraDeSeguridad bitacora;

    @MockitoBean
    private ReinicioDeBase reinicio;

    @MockitoBean
    private CargaDeEjemplos carga;

    @MockitoBean
    private Ejemplos ejemplos;

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
    void desdeLaPropiaMaquinaSeReinicia() throws Exception {
        mockMvc.perform(post("/desarrollo/reiniciar").with(csrf()).with(desde("127.0.0.1")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(reinicio).reiniciar();
    }

    @Test
    void desdeOtraMaquinaNoExiste() throws Exception {
        mockMvc.perform(post("/desarrollo/reiniciar").with(csrf()).with(desde("190.12.40.7")))
                .andExpect(status().isNotFound());

        verify(reinicio, never()).reiniciar();
    }

    @Test
    void cargarEjemplosDesdeOtraMaquinaTampoco() throws Exception {
        mockMvc.perform(post("/desarrollo/ejemplos").with(csrf()).with(desde("190.12.40.7")))
                .andExpect(status().isNotFound());

        verify(carga, never()).cargarSiVacia();
    }

    private RequestPostProcessor desde(String direccion) {
        return pedido -> {
            pedido.setRemoteAddr(direccion);
            return pedido;
        };
    }
}
