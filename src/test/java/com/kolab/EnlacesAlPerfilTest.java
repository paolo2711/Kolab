package com.kolab;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

// con un servicio en curso, cada parte llega al perfil de la otra desde donde la ve nombrada
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EnlacesAlPerfilTest {

    private static final long MUSICA = 1L;

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioService usuarioService;
    @Autowired private DetallesUsuarioService detalles;
    @Autowired private PerfilService perfilService;
    @Autowired private SolicitudService solicitudService;
    @Autowired private OfertaService ofertaService;

    private Long pide;
    private Long ofrece;
    private Long oferta;
    private Long servicio;

    @BeforeEach
    void unServicioEnCurso() {
        pide = registrar("pide.enlace@kolab.pe");
        ofrece = registrar("ofrece.enlace@kolab.pe");
        perfilService.guardarCategorias(ofrece, List.of(MUSICA));
        Long solicitud = solicitudService.publicar(solicitud(), pide);
        oferta = ofertaService.ofertar(solicitud, ofertaDe("90"), ofrece);
        servicio = ofertaService.aceptar(oferta, pide);
    }

    @Test
    void desdeElDetalleDelServicio() throws Exception {
        mockMvc.perform(get("/servicios/{id}", servicio).with(user(cuenta("pide.enlace@kolab.pe"))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(enlaceA(ofrece))));
    }

    @Test
    void desdeLosServiciosDeMiActividad() throws Exception {
        mockMvc.perform(get("/mis-solicitudes").with(user(cuenta("pide.enlace@kolab.pe"))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(enlaceA(ofrece))));
        mockMvc.perform(get("/mis-ofertas").with(user(cuenta("ofrece.enlace@kolab.pe"))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(enlaceA(pide))));
    }

    @Test
    void desdeLaCabeceraDeLaConversacion() throws Exception {
        mockMvc.perform(get("/mensajes/{id}", oferta).with(user(cuenta("ofrece.enlace@kolab.pe"))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(enlaceA(pide))));
    }

    private static String enlaceA(Long idUsuario) {
        return "href=\"/personas/" + idUsuario + "\"";
    }

    private UsuarioAutenticado cuenta(String email) {
        return detalles.cargar(email, Rol.USUARIO);
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
