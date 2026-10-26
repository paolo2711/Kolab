package com.kolab.mensaje;

import static com.kolab.CuentasDePrueba.CLAVE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

// el formulario solo dice a que pantalla volver y su id, la ruta la arma el controlador
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VolverDeMensajesTest {

    private static final long MUSICA = 1L;

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioService usuarioService;
    @Autowired private DetallesUsuarioService detalles;
    @Autowired private PerfilService perfilService;
    @Autowired private SolicitudService solicitudService;
    @Autowired private OfertaService ofertaService;

    private Long idPide;
    private Long oferta;
    private Long solicitud;
    private UsuarioAutenticado quienPide;

    @BeforeEach
    void unaOfertaConSuConversacion() {
        idPide = registrar("pide.volver@kolab.pe");
        Long ofrece = registrar("ofrece.volver@kolab.pe");
        perfilService.guardarCategorias(ofrece, List.of(MUSICA));
        solicitud = solicitudService.publicar(solicitud(), idPide);
        oferta = ofertaService.ofertar(solicitud, ofertaDe("90"), ofrece);
        quienPide = detalles.cargar("pide.volver@kolab.pe", Rol.USUARIO);
    }

    @Test
    void desdeElDetalleDeLaSolicitudVuelveAEl() throws Exception {
        escribir("solicitud", String.valueOf(solicitud)).andExpect(redirectedUrl("/mis-solicitudes/" + solicitud));
    }

    @Test
    void desdeElDetalleDelServicioVuelveAEl() throws Exception {
        Long servicio = ofertaService.aceptar(oferta, idPide);

        escribir("servicio", String.valueOf(servicio)).andExpect(redirectedUrl("/servicios/" + servicio));
    }

    // la ruta nunca sale del formulario: cualquier otra cosa se queda en la conversacion
    @ParameterizedTest
    @ValueSource(strings = {"https://otro-sitio.com", "//otro-sitio.com", "/mis-solicitudes/1", "SERVICIO",
                            "javascript:alert(1)"})
    void unValorFueraDeLaListaSeQuedaEnLaConversacion(String volver) throws Exception {
        escribir(volver, "1").andExpect(redirectedUrl("/mensajes/" + oferta));
    }

    @Test
    void sinDestinoOSinIdSeQuedaEnLaConversacion() throws Exception {
        escribir("", null).andExpect(redirectedUrl("/mensajes/" + oferta));
        escribir("solicitud", null).andExpect(redirectedUrl("/mensajes/" + oferta));
    }

    private ResultActions escribir(String volver, String id) throws Exception {
        MockHttpServletRequestBuilder pedido = post("/mensajes/{id}", oferta)
                .param("contenido", "¿El sábado a las diez?")
                .param("volver", volver)
                .with(csrf()).with(user(quienPide));
        if (id != null) {
            pedido.param("id", id);
        }
        return mockMvc.perform(pedido);
    }

    private SolicitudForm solicitud() {
        SolicitudForm form = new SolicitudForm();
        form.setTitulo("Clases de guitarra para principiante");
        form.setIdCategoria(MUSICA);
        form.setModalidad(Modalidad.PRESENCIAL);
        form.setDistrito("Jesús María");
        form.setDescripcion("Quiero aprender desde cero, los sábados en la mañana.");
        form.setFechaDeseada(LocalDate.now().plusDays(7));
        form.setPrecioPropuesto(new BigDecimal("90"));
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
