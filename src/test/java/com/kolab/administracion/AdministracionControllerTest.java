package com.kolab.administracion;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.kolab.categoria.CategoriaService;
import com.kolab.common.BitacoraDeSeguridad;
import com.kolab.common.Fotos;
import com.kolab.config.SecurityConfig;
import com.kolab.inicio.InicioController;
import com.kolab.mensaje.MensajeService;
import com.kolab.oferta.OfertaService;
import com.kolab.perfil.DirectorioService;
import com.kolab.solicitud.CatalogoService;
import com.kolab.solicitud.SolicitudService;
import com.kolab.usuario.DetallesUsuarioService;
import com.kolab.usuario.EstadoUsuario;
import com.kolab.usuario.Rol;
import com.kolab.usuario.Usuario;
import com.kolab.usuario.UsuarioAutenticado;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({AdministracionController.class, InicioController.class})
@Import(SecurityConfig.class)
class AdministracionControllerTest {

    private static final String CLAVE = "kolab1234";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private AdministracionService administracionService;

    @MockitoBean
    private TableroEnExcel tableroEnExcel;

    @MockitoBean
    private DetallesUsuarioService detallesUsuarioService;

    @MockitoBean
    private BitacoraDeSeguridad bitacora;

    @MockitoBean
    private Fotos fotos;

    @MockitoBean
    private MensajeService mensajeService;

    @MockitoBean
    private SolicitudService solicitudService;

    @MockitoBean
    private OfertaService ofertaService;

    @MockitoBean
    private CatalogoService catalogoService;

    @MockitoBean
    private CategoriaService categoriaService;

    @MockitoBean
    private DirectorioService directorioService;

    @Test
    void sinSesionElPanelMandaASuPropioIngreso() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/admin/login"));
    }

    @Test
    void elIngresoDelPanelNoOfreceCrearCuentaNiRecuperarLaClave() throws Exception {
        mockMvc.perform(get("/admin/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("cuenta/login"))
                .andExpect(content().string(containsString("Panel de administración")))
                .andExpect(content().string(not(containsString("Crear cuenta"))))
                .andExpect(content().string(not(containsString("Olvidé mi contraseña"))));
    }

    @Test
    void quienAdministraEntraPorElPanelYVaASuPortada() throws Exception {
        Mockito.when(detallesUsuarioService.cargar("admin@kolab.pe", Rol.ADMINISTRADOR))
                .thenReturn(sesion(Rol.ADMINISTRADOR, "admin@kolab.pe"));

        mockMvc.perform(formLogin("/admin/login").userParameter("email").user("admin@kolab.pe").password(CLAVE))
                .andExpect(redirectedUrl("/admin"));
    }

    @Test
    void unaCuentaDelMercadoNoEntraPorElPanel() throws Exception {
        Mockito.when(detallesUsuarioService.cargar("experto@kolab.pe", Rol.ADMINISTRADOR))
                .thenThrow(new UsernameNotFoundException("no administra"));

        mockMvc.perform(formLogin("/admin/login").userParameter("email").user("experto@kolab.pe").password(CLAVE))
                .andExpect(redirectedUrl("/admin/login?error"));
    }

    @Test
    void quienAdministraNoEntraPorElIngresoDelMercado() throws Exception {
        Mockito.when(detallesUsuarioService.cargar("admin@kolab.pe", Rol.USUARIO))
                .thenThrow(new UsernameNotFoundException("administra"));

        mockMvc.perform(formLogin("/login").userParameter("email").user("admin@kolab.pe").password(CLAVE))
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void aQuienNoAdministraElPanelLeRespondeComoSiNoExistiera() throws Exception {
        mockMvc.perform(get("/admin/cuentas").with(user(sesion(Rol.USUARIO, "experto@kolab.pe"))))
                .andExpect(status().isNotFound());

        Mockito.verifyNoInteractions(administracionService);
    }

    @Test
    void elPanelTieneSuMenuYNoElDelMercado() throws Exception {
        Mockito.when(administracionService.tablero()).thenReturn(unTablero());

        mockMvc.perform(get("/admin").with(user(sesion(Rol.ADMINISTRADOR, "admin@kolab.pe"))))
                .andExpect(status().isOk())
                .andExpect(view().name("administracion/panel"))
                .andExpect(content().string(containsString("/admin/cuentas")))
                .andExpect(content().string(not(containsString("Explorar"))))
                .andExpect(content().string(not(containsString("Publicar"))))
                .andExpect(content().string(not(containsString("Mensajes"))));
    }

    @Test
    void laPortadaMuestraLasCifrasYLasDosTablas() throws Exception {
        Mockito.when(administracionService.tablero()).thenReturn(unTablero());

        mockMvc.perform(get("/admin").with(user(sesion(Rol.ADMINISTRADOR, "admin@kolab.pe"))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Comisión del mes")))
                .andExpect(content().string(containsString("octubre de 2026")))
                .andExpect(content().string(containsString("Idiomas")))
                .andExpect(content().string(containsString("/admin/excel")));
    }

    @Test
    void quienAdministraBajaElPanelEnExcel() throws Exception {
        Mockito.when(administracionService.tablero()).thenReturn(unTablero());
        Mockito.when(tableroEnExcel.de(Mockito.any())).thenReturn(new byte[] {1, 2, 3});

        mockMvc.perform(get("/admin/excel").with(user(sesion(Rol.ADMINISTRADOR, "admin@kolab.pe"))))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("panel-kolab.xlsx")))
                .andExpect(content().contentType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    void elExcelDelPanelNoLoBajaQuienNoAdministra() throws Exception {
        mockMvc.perform(get("/admin/excel").with(user(sesion(Rol.USUARIO, "experto@kolab.pe"))))
                .andExpect(status().isNotFound());

        Mockito.verifyNoInteractions(tableroEnExcel);
    }

    @Test
    void laListaDeCuentasEstaEnSuPropiaRuta() throws Exception {
        Mockito.when(administracionService.cuentas(Mockito.any())).thenReturn(Page.empty());

        mockMvc.perform(get("/admin/cuentas").with(user(sesion(Rol.ADMINISTRADOR, "admin@kolab.pe"))))
                .andExpect(status().isOk())
                .andExpect(view().name("administracion/cuentas"));
    }

    @Test
    void siQuienAdministraAbreElMercadoVuelveAlPanel() throws Exception {
        UsuarioAutenticado admin = sesion(Rol.ADMINISTRADOR, "admin@kolab.pe");

        mockMvc.perform(get("/").with(user(admin)))
                .andExpect(redirectedUrl("/admin"));
        mockMvc.perform(get("/mensajes").with(user(admin)))
                .andExpect(redirectedUrl("/admin"));
    }

    @Test
    void quienAdministraNoPublicaNiEscribe() throws Exception {
        UsuarioAutenticado admin = sesion(Rol.ADMINISTRADOR, "admin@kolab.pe");

        mockMvc.perform(post("/mis-solicitudes").with(csrf()).with(user(admin)))
                .andExpect(redirectedUrl("/admin"));
        mockMvc.perform(post("/mensajes/1").with(csrf()).with(user(admin)))
                .andExpect(redirectedUrl("/admin"));
    }

    private Tablero unTablero() {
        return new Tablero(15, 12, 9, new BigDecimal("8.50"),
                List.of(new Tablero.Mes("octubre de 2026", 5, 2, new BigDecimal("8.50"))),
                List.of(new Tablero.CategoriaPedida("Idiomas", 8)));
    }

    private UsuarioAutenticado sesion(Rol rol, String email) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Equipo");
        usuario.setApellidos("KOLAB");
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(CLAVE));
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuario.setRol(rol);
        return new UsuarioAutenticado(usuario, false, null);
    }
}
