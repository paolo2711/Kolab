package com.kolab.mensaje;

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
import org.springframework.transaction.annotation.Transactional;

// el formulario dice a donde volver despues de escribir; un destino de afuera no se sigue
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

    private Long oferta;
    private Long solicitud;
    private UsuarioAutenticado quienPide;

    @BeforeEach
    void unaOfertaConSuConversacion() {
        Long pide = registrar("pide.volver@kolab.pe");
        Long ofrece = registrar("ofrece.volver@kolab.pe");
        perfilService.guardarCategorias(ofrece, List.of(MUSICA));
        solicitud = solicitudService.publicar(solicitud(), pide);
        oferta = ofertaService.ofertar(solicitud, ofertaDe("90"), ofrece);
        quienPide = detalles.cargar("pide.volver@kolab.pe", Rol.USUARIO);
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://otro-sitio.com", "//otro-sitio.com", "/\\otro-sitio.com",
                            "/\t/otro-sitio.com", "javascript:alert(1)", "otro-sitio.com", "http:/otro-sitio.com"})
    void unDestinoDeAfueraVuelveALaBandeja(String volver) throws Exception {
        escribir(volver).andExpect(redirectedUrl("/mensajes"));
    }

    @Test
    void unaRutaDeLaAppSeRespeta() throws Exception {
        escribir("/mis-solicitudes/" + solicitud).andExpect(redirectedUrl("/mis-solicitudes/" + solicitud));
    }

    @Test
    void sinDestinoSeQuedaEnLaConversacion() throws Exception {
        escribir("").andExpect(redirectedUrl("/mensajes/" + oferta));
    }

    private ResultActions escribir(String volver) throws Exception {
        return mockMvc.perform(post("/mensajes/{id}", oferta)
                .param("contenido", "¿El sábado a las diez?")
                .param("volver", volver)
                .with(csrf()).with(user(quienPide)));
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
        form.setPassword("secreto123");
        form.setConfirmacion("secreto123");
        return usuarioService.registrar(form).getId();
    }
}
