package com.kolab.inicio;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.kolab.config.SecurityConfig;
import com.kolab.demo.ArchivosDeDatos;
import com.kolab.demo.CatalogoDeCategorias;
import com.kolab.demo.DirectorioDePersonas;
import com.kolab.demo.OfertasDeEjemplo;
import com.kolab.demo.SolicitudesDeEjemplo;
import com.kolab.perfil.PerfilService;
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
@Import({SecurityConfig.class, ArchivosDeDatos.class, CatalogoDeCategorias.class,
        DirectorioDePersonas.class, OfertasDeEjemplo.class, SolicitudesDeEjemplo.class})
class InicioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PerfilService perfilService;

    @Test
    void alVisitanteSinCuentaLeMuestraLaPortada() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("portada"));
    }

    @Test
    void aQuienYaDijoQueSabeHacerAlgoLeAbreSuInicio() throws Exception {
        Mockito.when(perfilService.categoriasDe(1L)).thenReturn(List.of(1L));

        mockMvc.perform(inicioComo(true))
                .andExpect(status().isOk())
                .andExpect(view().name("inicio/inicio-usuario"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Abiertas en lo que sabes hacer")));
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
        return get("/").with(user(new UsuarioAutenticado(usuario, ofreceServicios)));
    }
}
