package com.kolab.controlador;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kolab.seguridad.BitacoraDeSeguridad;
import com.kolab.seguridad.DetallesUsuarioService;
import com.kolab.seguridad.SecurityConfig;
import com.kolab.servicio.CatalogoService;
import com.kolab.servicio.CorreoService;
import com.kolab.servicio.MensajeService;
import com.kolab.servicio.OfertaService;
import com.kolab.servicio.RecuperacionService;
import com.kolab.servicio.SolicitudService;
import com.kolab.util.Fotos;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// el enlace del correo sale de la direccion configurada y no del Host del pedido,
// que cualquiera puede cambiar para mandar a la persona a otro sitio
@WebMvcTest(RecuperacionController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "kolab.url-publica=https://kolab.pe")
@Tag("controlador")
class RecuperacionControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private RecuperacionService recuperacionService;
    @MockitoBean private CorreoService correoService;
    @MockitoBean private BitacoraDeSeguridad bitacora;
    @MockitoBean private DetallesUsuarioService detallesUsuarioService;
    @MockitoBean private Fotos fotos;
    @MockitoBean private CatalogoService catalogoService;
    @MockitoBean private MensajeService mensajeService;
    @MockitoBean private SolicitudService solicitudService;
    @MockitoBean private OfertaService ofertaService;

    @Test
    void conCuentaMandaElCorreoConElEnlaceDeKolab() throws Exception {
        when(recuperacionService.generarCodigo("paolo@kolab.pe")).thenReturn(Optional.of("abc.def"));

        mockMvc.perform(post("/recuperar").param("email", "paolo@kolab.pe").with(csrf())
                        .header(HttpHeaders.HOST, "sitio-falso.com"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Revisa tu correo")));

        verify(correoService).enviarEnlaceDeRecuperacion("paolo@kolab.pe", "https://kolab.pe/recuperar/abc.def");
    }

    @Test
    void sinCuentaNoMandaNadaYLaPantallaEsLaMisma() throws Exception {
        when(recuperacionService.generarCodigo("nadie@kolab.pe")).thenReturn(Optional.empty());

        mockMvc.perform(post("/recuperar").param("email", "nadie@kolab.pe").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Revisa tu correo")));

        verifyNoInteractions(correoService);
    }
}
