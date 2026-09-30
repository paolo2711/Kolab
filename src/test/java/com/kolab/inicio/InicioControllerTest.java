package com.kolab.inicio;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.kolab.categoria.CategoriaService;
import com.kolab.common.Fotos;
import com.kolab.config.SecurityConfig;
import com.kolab.mensaje.MensajeService;
import com.kolab.oferta.OfertaService;
import com.kolab.perfil.DirectorioService;
import com.kolab.solicitud.CatalogoService;
import com.kolab.solicitud.SolicitudService;
import com.kolab.usuario.EstadoUsuario;
import com.kolab.usuario.Usuario;
import com.kolab.usuario.UsuarioAutenticado;
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
