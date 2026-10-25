package com.kolab;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kolab.mensaje.MensajeService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

// desde cada chat chico se puede pasar a la misma conversacion en Mensajes, y se ve como ella
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AbrirEnMensajesTest {

    private static final long MUSICA = 1L;
    private static final String PIDE = "pide.abrir@kolab.pe";

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioService usuarioService;
    @Autowired private DetallesUsuarioService detalles;
    @Autowired private PerfilService perfilService;
    @Autowired private SolicitudService solicitudService;
    @Autowired private OfertaService ofertaService;
    @Autowired private MensajeService mensajeService;

    private Long ofrece;
    private Long pide;

    @BeforeEach
    void dosPartes() {
        pide = registrar(PIDE);
        ofrece = registrar("ofrece.abrir@kolab.pe");
        perfilService.guardarCategorias(ofrece, List.of(MUSICA));
    }

    @Test
    void desdeElChatDeUnaOferta() throws Exception {
        Long solicitud = solicitudService.publicar(solicitud(), pide);
        Long oferta = ofertaService.ofertar(solicitud, ofertaDe("90"), ofrece);

        mockMvc.perform(get("/mis-solicitudes/{id}", solicitud).with(user(detalles.cargar(PIDE, Rol.USUARIO))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(abrir(oferta))));
    }

    @Test
    void desdeElChatDelServicio() throws Exception {
        Long solicitud = solicitudService.publicar(solicitud(), pide);
        Long oferta = ofertaService.ofertar(solicitud, ofertaDe("90"), ofrece);
        Long servicio = ofertaService.aceptar(oferta, pide);
        UsuarioAutenticado quienPide = detalles.cargar(PIDE, Rol.USUARIO);

        mockMvc.perform(get("/servicios/{id}", servicio).with(user(quienPide)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(abrir(oferta))));
    }

    @Test
    void elChatChicoUsaLasBurbujasYElBotonDeMensajes() throws Exception {
        Long solicitud = solicitudService.publicar(solicitud(), pide);
        Long oferta = ofertaService.ofertar(solicitud, ofertaDe("90"), ofrece);
        mensajeService.enviar(oferta, "Hola, ¿el sábado a las diez te va?", ofrece);
        Long servicio = ofertaService.aceptar(oferta, pide);
        UsuarioAutenticado quienPide = detalles.cargar(PIDE, Rol.USUARIO);

        for (String ruta : new String[] {"/servicios/" + servicio, "/mis-solicitudes/" + solicitud}) {
            mockMvc.perform(get(ruta).with(user(quienPide)))
                    .andExpect(status().isOk())
                    .andExpect(content().string(allOf(
                            containsString("class=\"k-msj-dia\""),
                            containsString("class=\"k-msj-burbuja k-msj-suyo\""),
                            containsString("class=\"k-msj-hora\""),
                            containsString("class=\"k-btn k-btn-principal\">Enviar<"),
                            not(containsString("k-btn-oscuro")))));
        }
    }

    private static String abrir(Long idOferta) {
        return "href=\"/mensajes/" + idOferta + "\">Abrir en Mensajes</a>";
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
