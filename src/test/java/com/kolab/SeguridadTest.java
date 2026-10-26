package com.kolab;

import static com.kolab.CuentasDePrueba.CLAVE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kolab.oferta.OfertaForm;
import com.kolab.oferta.OfertaService;
import com.kolab.perfil.PerfilService;
import com.kolab.solicitud.Modalidad;
import com.kolab.solicitud.SolicitudForm;
import com.kolab.solicitud.SolicitudService;
import com.kolab.usuario.DetallesUsuarioService;
import com.kolab.usuario.RegistroForm;
import com.kolab.usuario.Rol;
import com.kolab.usuario.UsuarioAutenticado;
import com.kolab.usuario.UsuarioRepository;
import com.kolab.usuario.UsuarioService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

// la seguridad de punta a punta: las dos cadenas de verdad, los controladores de verdad y la base en
// H2. cada prueba arma sus datos con los servicios y se deshace al terminar
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SeguridadTest {

    private static final long MUSICA = 1L;

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioService usuarioService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private DetallesUsuarioService detalles;
    @Autowired private PerfilService perfilService;
    @Autowired private SolicitudService solicitudService;
    @Autowired private OfertaService ofertaService;

    private Long pide;
    private Long ofrece;
    private UsuarioAutenticado otro;

    // una oferta todavía sin responder y otra ya aceptada, con su servicio; ninguna es del tercero
    private Long solicitud;
    private Long oferta;
    private Long servicio;
    private Long ofertaAceptada;

    @BeforeEach
    void dosPartesYUnTercero() {
        pide = registrar("paolo@kolab.pe");
        ofrece = registrar("luis@kolab.pe");
        registrar("carlos@kolab.pe");
        perfilService.guardarCategorias(ofrece, List.of(MUSICA));
        otro = detalles.cargar("carlos@kolab.pe", Rol.USUARIO);

        solicitud = solicitudService.publicar(solicitudDe("80"), pide);
        oferta = ofertaService.ofertar(solicitud, ofertaDe("80"), ofrece);

        Long otraSolicitud = solicitudService.publicar(solicitudDe("120"), pide);
        ofertaAceptada = ofertaService.ofertar(otraSolicitud, ofertaDe("120"), ofrece);
        servicio = ofertaService.aceptar(ofertaAceptada, pide);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/solicitudes", "/mis-solicitudes", "/mis-solicitudes/nueva", "/mis-ofertas",
                            "/servicios", "/mensajes", "/mi-resumen", "/mi-resumen/excel", "/perfil",
                            "/perfil/editar", "/bienvenida", "/personas/1", "/categorias/1"})
    void lasRutasPrivadasSinSesionMandanAlLogin(String ruta) throws Exception {
        mockMvc.perform(get(ruta))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/admin", "/admin/cuentas", "/admin/excel"})
    void elPanelSinSesionMandaASuIngreso(String ruta) throws Exception {
        mockMvc.perform(get(ruta))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/admin/login"));
    }

    @Test
    void laSolicitudDeOtroNoExisteParaElTercero() throws Exception {
        mockMvc.perform(get("/mis-solicitudes/{id}", solicitud).with(user(otro)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/mis-solicitudes/{id}/editar", solicitud).with(user(otro)))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/mis-solicitudes/{id}/cancelar", solicitud).with(csrf()).with(user(otro)))
                .andExpect(status().isNotFound());
    }

    @Test
    void laOfertaDeOtroNoLaAceptaNiLaRechazaElTercero() throws Exception {
        mockMvc.perform(post("/mis-solicitudes/{id}/ofertas/{idOferta}", solicitud, oferta)
                        .with(csrf()).with(user(otro)))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/mis-solicitudes/{id}/ofertas/{idOferta}/rechazar", solicitud, oferta)
                        .with(csrf()).with(user(otro)))
                .andExpect(status().isNotFound());
    }

    @Test
    void elServicioDeOtroNoExisteParaElTercero() throws Exception {
        mockMvc.perform(get("/servicios/{id}", servicio).with(user(otro)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/servicios/{id}/cerrar", servicio).with(user(otro)))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/servicios/{id}/calificar", servicio).param("puntaje", "5")
                        .with(csrf()).with(user(otro)))
                .andExpect(status().isNotFound());
    }

    @Test
    void laConversacionDeOtroNiSeLeeNiSeEscribe() throws Exception {
        mockMvc.perform(get("/mensajes/{id}", ofertaAceptada).with(user(otro)))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/mensajes/{id}", ofertaAceptada).param("contenido", "hola")
                        .with(csrf()).with(user(otro)))
                .andExpect(status().isNotFound());
    }

    @Test
    void unFormularioSinTokenSeRechazaConSesion() throws Exception {
        UsuarioAutenticado quienPide = detalles.cargar("paolo@kolab.pe", Rol.USUARIO);

        mockMvc.perform(post("/mis-solicitudes/{id}/cancelar", solicitud).with(user(quienPide)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/mensajes/{id}", ofertaAceptada).param("contenido", "hola").with(user(quienPide)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/perfil/editar").with(user(quienPide)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/salir").with(user(quienPide)))
                .andExpect(status().isForbidden());
    }

    @Test
    void enElPanelUnFormularioSinTokenTambienSeRechaza() throws Exception {
        Long admin = registrar("admin.token@kolab.pe");
        usuarioRepository.getReferenceById(admin).setRol(Rol.ADMINISTRADOR);
        UsuarioAutenticado quienAdministra = detalles.cargar("admin.token@kolab.pe", Rol.ADMINISTRADOR);

        mockMvc.perform(post("/admin/cuentas/{id}/suspender", ofrece).with(user(quienAdministra)))
                .andExpect(status().isForbidden());
    }

    @Test
    void unFormularioSinTokenSeRechazaSinSesion() throws Exception {
        mockMvc.perform(post("/login").param("email", "paolo@kolab.pe").param("password", CLAVE))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/registro").param("email", "nuevo@kolab.pe"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/recuperar").param("email", "paolo@kolab.pe"))
                .andExpect(status().isForbidden());
    }

    @Test
    void cincoFallosBloqueanElIngresoDelMercadoAunqueDespuesLaClaveSeaLaBuena() throws Exception {
        registrar("bloqueo.mercado@kolab.pe");

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(formLogin("/login").userParameter("email").user("bloqueo.mercado@kolab.pe").password("mala"))
                    .andExpect(redirectedUrl("/login?error"));
        }
        mockMvc.perform(formLogin("/login").userParameter("email").user("bloqueo.mercado@kolab.pe").password(CLAVE))
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void cincoFallosBloqueanTambienElIngresoDelPanel() throws Exception {
        Long admin = registrar("bloqueo.panel@kolab.pe");
        usuarioRepository.getReferenceById(admin).setRol(Rol.ADMINISTRADOR);

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(formLogin("/admin/login").userParameter("email").user("bloqueo.panel@kolab.pe").password("mala"))
                    .andExpect(redirectedUrl("/admin/login?error"));
        }
        mockMvc.perform(formLogin("/admin/login").userParameter("email").user("bloqueo.panel@kolab.pe").password(CLAVE))
                .andExpect(redirectedUrl("/admin/login?error"));
    }

    @Test
    void sinBloqueoLaClaveBuenaEntraEnLosDosIngresos() throws Exception {
        registrar("entra.mercado@kolab.pe");
        Long admin = registrar("entra.panel@kolab.pe");
        usuarioRepository.getReferenceById(admin).setRol(Rol.ADMINISTRADOR);

        mockMvc.perform(formLogin("/login").userParameter("email").user("entra.mercado@kolab.pe").password(CLAVE))
                .andExpect(redirectedUrl("/"));
        mockMvc.perform(formLogin("/admin/login").userParameter("email").user("entra.panel@kolab.pe").password(CLAVE))
                .andExpect(redirectedUrl("/admin"));
    }

    private SolicitudForm solicitudDe(String precio) {
        SolicitudForm form = new SolicitudForm();
        form.setTitulo("Clases de guitarra para principiante");
        form.setIdCategoria(MUSICA);
        form.setModalidad(Modalidad.PRESENCIAL);
        form.setDistrito("Jesús María");
        form.setDescripcion("Quiero aprender desde cero, los sábados en la mañana.");
        form.setFechaDeseada(LocalDate.now().plusDays(7));
        form.setPrecioPropuesto(new BigDecimal(precio));
        return form;
    }

    private OfertaForm ofertaDe(String monto) {
        OfertaForm form = new OfertaForm();
        form.setMonto(new BigDecimal(monto));
        return form;
    }

    private Long registrar(String email) {
        RegistroForm form = new RegistroForm();
        form.setNombre("Persona");
        form.setApellidos("Prueba");
        form.setEmail(email);
        form.setPassword(CLAVE);
        form.setConfirmacion(CLAVE);
        return usuarioService.registrar(form).getId();
    }
}
