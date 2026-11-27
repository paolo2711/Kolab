package com.kolab.controlador;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.kolab.modelo.dto.Historial;
import com.kolab.modelo.entidad.EstadoUsuario;
import com.kolab.modelo.entidad.Rol;
import com.kolab.modelo.entidad.Usuario;
import com.kolab.seguridad.BitacoraDeSeguridad;
import com.kolab.seguridad.DetallesUsuarioService;
import com.kolab.seguridad.SecurityConfig;
import com.kolab.seguridad.UsuarioAutenticado;
import com.kolab.servicio.CatalogoService;
import com.kolab.servicio.HistorialService;
import com.kolab.servicio.MensajeService;
import com.kolab.servicio.OfertaService;
import com.kolab.servicio.SolicitudService;
import com.kolab.util.Fotos;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// el historial del log solo lo ve el administrador
@WebMvcTest(HistorialController.class)
@Import(SecurityConfig.class)
@Tag("controlador")
class HistorialControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private HistorialService historialService;
    @MockitoBean private DetallesUsuarioService detallesUsuarioService;
    @MockitoBean private BitacoraDeSeguridad bitacora;
    @MockitoBean private Fotos fotos;
    @MockitoBean private CatalogoService catalogoService;
    @MockitoBean private MensajeService mensajeService;
    @MockitoBean private SolicitudService solicitudService;
    @MockitoBean private OfertaService ofertaService;

    @Test
    void elAdministradorVeLasLineasDelLog() throws Exception {
        Mockito.when(historialService.leer(null, null)).thenReturn(new Historial(List.of(
                new Historial.Linea("26/11 09:10:00", "ERROR", "ManejoDeErrores", "error inesperado en /servicios/5")),
                1, 0, 0));

        mockMvc.perform(get("/admin/historial").with(user(sesion(Rol.ADMINISTRADOR))))
                .andExpect(status().isOk())
                .andExpect(view().name("administracion/historial"))
                .andExpect(content().string(containsString("error inesperado en /servicios/5")))
                .andExpect(content().string(containsString("k-nivel-error")));
    }

    @Test
    void llevaElMenuDelPanelYNoElDelMercado() throws Exception {
        Mockito.when(historialService.leer(null, null)).thenReturn(new Historial(List.of(), 0, 0, 0));

        mockMvc.perform(get("/admin/historial").with(user(sesion(Rol.ADMINISTRADOR))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/admin/monitoreo\"")))
                .andExpect(content().string(not(containsString("href=\"/mis-solicitudes/nueva\""))));
    }

    @Test
    void unNivelInventadoSeIgnora() throws Exception {
        Mockito.when(historialService.leer(null, null)).thenReturn(new Historial(List.of(), 0, 0, 0));

        mockMvc.perform(get("/admin/historial").param("nivel", "<script>").with(user(sesion(Rol.ADMINISTRADOR))))
                .andExpect(status().isOk());

        Mockito.verify(historialService).leer(null, null);
    }

    @Test
    void aUnUsuarioLeRespondeComoSiNoExistiera() throws Exception {
        mockMvc.perform(get("/admin/historial").with(user(sesion(Rol.USUARIO))))
                .andExpect(status().isNotFound());

        Mockito.verifyNoInteractions(historialService);
    }

    @Test
    void sinSesionMandaAlIngresoDelPanel() throws Exception {
        mockMvc.perform(get("/admin/historial"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/admin/login"));
    }

    private static UsuarioAutenticado sesion(Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Equipo");
        usuario.setApellidos("KOLAB");
        usuario.setEmail("admin@kolab.pe");
        usuario.setPasswordHash("$2a$10$hashfalso");
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuario.setRol(rol);
        return new UsuarioAutenticado(usuario, false, null);
    }
}
