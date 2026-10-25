package com.kolab.inicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.kolab.categoria.CategoriaService;
import com.kolab.common.BitacoraDeSeguridad;
import com.kolab.common.Fotos;
import com.kolab.config.SecurityConfig;
import com.kolab.mensaje.MensajeService;
import com.kolab.oferta.OfertaService;
import com.kolab.perfil.DirectorioService;
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
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(InicioController.class)
@Import(SecurityConfig.class)
class InicioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DetallesUsuarioService detallesUsuarioService;

    @MockitoBean
    private BitacoraDeSeguridad bitacora;

    @MockitoBean
    private CatalogoService catalogoService;

    @MockitoBean
    private CategoriaService categoriaService;

    @MockitoBean
    private DirectorioService directorioService;

    @MockitoBean
    private Fotos fotos;

    @MockitoBean
    private MensajeService mensajeService;

    @MockitoBean
    private SolicitudService solicitudService;

    @MockitoBean
    private OfertaService ofertaService;

    @Test
    void alVisitanteSinCuentaLeMuestraLaPortada() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("portada"));
    }

    @Test
    void aQuienYaDijoQueSabeHacerAlgoLeAbreSuInicio() throws Exception {
        Mockito.when(catalogoService.enMisCategorias(Mockito.eq(1L), Mockito.anyInt())).thenReturn(List.of());

        mockMvc.perform(inicioComo(true))
                .andExpect(status().isOk())
                .andExpect(view().name("inicio/inicio-usuario"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Lo que se está pidiendo ahora")));
    }

    @Test
    void aQuienTodaviaNoDiceQueSabeHacerLePreguntaUnaVez() throws Exception {
        mockMvc.perform(inicioComo(false))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bienvenida"));
    }

    @Test
    void quienRecibioUnaPropuestaLaVeArribaMarcadaParaEl() throws Exception {
        SolicitudResumen propuesta = solicitud(7L, "Clases de guitarra para mi hijo", true);
        Mockito.when(catalogoService.propuestasSinResponder(Mockito.eq(1L), Mockito.anyInt())).thenReturn(List.of(propuesta));
        Mockito.when(catalogoService.enMisCategorias(Mockito.eq(1L), Mockito.anyInt()))
                .thenReturn(List.of(propuesta, solicitud(8L, "Pintar la fachada", false)));

        String pagina = mockMvc.perform(inicioComo(true))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(pagina).contains("Te propusieron", "Para ti");
        // arriba en su seccion y no repetida en la lista de mas abajo
        assertThat(pagina.split("Clases de guitarra para mi hijo", -1)).hasSize(2);
        assertThat(pagina.indexOf("Te propusieron")).isLessThan(pagina.indexOf("Pintar la fachada"));
    }

    @Test
    void sinPropuestasNoApareceLaSeccion() throws Exception {
        Mockito.when(catalogoService.enMisCategorias(Mockito.eq(1L), Mockito.anyInt())).thenReturn(List.of());

        mockMvc.perform(inicioComo(true))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("Te propusieron"))));
    }

    private SolicitudResumen solicitud(Long id, String titulo, boolean directa) {
        return new SolicitudResumen(id, titulo, 1L, "Música", "bi-music-note", null, Modalidad.PRESENCIAL,
                "Jesús María", new BigDecimal("80"), null, List.of(), "hace 2 horas", directa, null);
    }

    private MockHttpServletRequestBuilder inicioComo(boolean ofreceServicios) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Paolo");
        usuario.setApellidos("Rodriguez Paredes");
        usuario.setEmail("paolo@kolab.pe");
        usuario.setPasswordHash("$2a$10$hashfalso");
        usuario.setEstado(EstadoUsuario.ACTIVO);
        return get("/").with(user(new UsuarioAutenticado(usuario, ofreceServicios, null)));
    }
}
