package com.kolab.controlador;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.kolab.modelo.entidad.EstadoUsuario;
import com.kolab.modelo.entidad.Usuario;
import com.kolab.seguridad.BitacoraDeSeguridad;
import com.kolab.seguridad.DetallesUsuarioService;
import com.kolab.seguridad.SecurityConfig;
import com.kolab.seguridad.UsuarioAutenticado;
import com.kolab.servicio.CatalogoService;
import com.kolab.servicio.CategoriaService;
import com.kolab.servicio.DirectorioService;
import com.kolab.servicio.MensajeService;
import com.kolab.servicio.OfertaService;
import com.kolab.servicio.ServicioService;
import com.kolab.servicio.SolicitudService;
import com.kolab.util.Fotos;
import com.kolab.util.FotosSubidas;
import java.time.LocalDate;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MisSolicitudesController.class)
@Import(SecurityConfig.class)
@Tag("controlador")
class MisSolicitudesControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private SolicitudService solicitudService;
    @MockitoBean private OfertaService ofertaService;
    @MockitoBean private ServicioService servicioService;
    @MockitoBean private MensajeService mensajeService;
    @MockitoBean private CategoriaService categoriaService;
    @MockitoBean private DirectorioService directorioService;
    @MockitoBean private FotosSubidas fotosSubidas;
    @MockitoBean private CatalogoService catalogoService;
    @MockitoBean private BitacoraDeSeguridad bitacora;
    @MockitoBean private DetallesUsuarioService detallesUsuarioService;
    @MockitoBean private Fotos fotos;

    @Test
    void unaFechaDeseadaEnElPasadoVuelveAlFormularioConError() throws Exception {
        LocalDate ayer = LocalDate.now().minusDays(1);

        mockMvc.perform(post("/mis-solicitudes").with(csrf()).with(user(sesion()))
                        .param("titulo", "Clases de guitarra para principiante")
                        .param("idCategoria", "1")
                        .param("modalidad", "PRESENCIAL")
                        .param("descripcion", "Quiero aprender desde cero, los sábados en la mañana.")
                        .param("fechaDeseada", ayer.toString())
                        .param("precioPropuesto", "80"))
                .andExpect(status().isOk())
                .andExpect(view().name("solicitud/nueva"))
                .andExpect(model().attributeHasFieldErrors("solicitudForm", "fechaDeseada"));

        verify(solicitudService, never()).publicar(any(), any());
    }

    private UsuarioAutenticado sesion() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Paolo");
        usuario.setApellidos("Rodriguez");
        usuario.setEmail("cliente@kolab.pe");
        usuario.setPasswordHash("$2a$10$hashfalso");
        usuario.setEstado(EstadoUsuario.ACTIVO);
        return new UsuarioAutenticado(usuario, false, null);
    }
}
