package com.kolab.oferta;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kolab.common.BitacoraDeSeguridad;
import com.kolab.common.Fotos;
import com.kolab.config.SecurityConfig;
import com.kolab.mensaje.MensajeService;
import com.kolab.servicio.ServicioService;
import com.kolab.solicitud.CatalogoService;
import com.kolab.solicitud.Modalidad;
import com.kolab.solicitud.SolicitudResumen;
import com.kolab.solicitud.SolicitudService;
import com.kolab.usuario.DetallesUsuarioService;
import com.kolab.usuario.EstadoUsuario;
import com.kolab.usuario.Usuario;
import com.kolab.usuario.UsuarioAutenticado;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MisOfertasController.class)
@Import(SecurityConfig.class)
class MisOfertasControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private OfertaService ofertaService;
    @MockitoBean private ServicioService servicioService;
    @MockitoBean private CatalogoService catalogoService;
    @MockitoBean private BitacoraDeSeguridad bitacora;
    @MockitoBean private DetallesUsuarioService detallesUsuarioService;
    @MockitoBean private Fotos fotos;
    @MockitoBean private MensajeService mensajeService;
    @MockitoBean private SolicitudService solicitudService;

    @BeforeEach
    void sinOfertasNiServicios() {
        Mockito.when(ofertaService.mias(Mockito.eq(1L), Mockito.any())).thenReturn(Page.empty());
        Mockito.when(servicioService.queDoy(Mockito.eq(1L), Mockito.any())).thenReturn(Page.empty());
    }

    @Test
    void lasPropuestasSinResponderTienenSuGrupoEnLoQueOfrezco() throws Exception {
        Mockito.when(catalogoService.propuestasSinResponder(Mockito.eq(1L), Mockito.anyInt())).thenReturn(List.of(
                new SolicitudResumen(7L, "Clases de guitarra para mi hijo", 1L, "Música", "bi-music-note", null,
                        Modalidad.PRESENCIAL, "Jesús María", new BigDecimal("80"), null, List.of(),
                        "hace 2 horas", true, true, null)));

        mockMvc.perform(get("/mis-ofertas").with(user(sesion())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Te propusieron")))
                .andExpect(content().string(containsString("Clases de guitarra para mi hijo")));
    }

    @Test
    void sinPropuestasNoApareceElGrupo() throws Exception {
        mockMvc.perform(get("/mis-ofertas").with(user(sesion())))
                .andExpect(content().string(not(containsString("Te propusieron"))));
    }

    private UsuarioAutenticado sesion() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Luis");
        usuario.setApellidos("Mendoza");
        usuario.setEmail("experto@kolab.pe");
        usuario.setPasswordHash("$2a$10$hashfalso");
        usuario.setEstado(EstadoUsuario.ACTIVO);
        return new UsuarioAutenticado(usuario, true, null);
    }
}
