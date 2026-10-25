package com.kolab.perfil;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kolab.calificacion.CalificacionForm;
import com.kolab.oferta.OfertaForm;
import com.kolab.oferta.OfertaService;
import com.kolab.servicio.ServicioService;
import com.kolab.solicitud.Modalidad;
import com.kolab.solicitud.SolicitudForm;
import com.kolab.solicitud.SolicitudService;
import com.kolab.usuario.DetallesUsuarioService;
import com.kolab.usuario.RegistroForm;
import com.kolab.usuario.Rol;
import com.kolab.usuario.UsuarioRepository;
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

// cada resena lleva al perfil de quien la escribio, en mi perfil y en el de los demas
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ResenasAlPerfilTest {

    private static final long MUSICA = 1L;

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioService usuarioService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private DetallesUsuarioService detalles;
    @Autowired private PerfilService perfilService;
    @Autowired private SolicitudService solicitudService;
    @Autowired private OfertaService ofertaService;
    @Autowired private ServicioService servicioService;

    private Long pide;
    private Long ofrece;

    @BeforeEach
    void unServicioCalificado() {
        pide = registrar("pide.resena@kolab.pe");
        ofrece = registrar("ofrece.resena@kolab.pe");
        registrar("mira.resena@kolab.pe");
        perfilService.guardarCategorias(ofrece, List.of(MUSICA));
        Long solicitud = solicitudService.publicar(solicitud(), pide);
        Long oferta = ofertaService.ofertar(solicitud, ofertaDe("90"), ofrece);
        Long servicio = ofertaService.aceptar(oferta, pide);
        CalificacionForm nota = new CalificacionForm();
        nota.setPuntaje(5);
        nota.setComentario("Muy paciente, llegó puntual.");
        servicioService.cerrarYCalificar(servicio, nota, pide);
    }

    @Test
    void enElPerfilQueVenLosDemas() throws Exception {
        mockMvc.perform(get("/personas/{codigo}", codigoDe(ofrece))
                        .with(user(detalles.cargar("mira.resena@kolab.pe", Rol.USUARIO))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(enlaceA(pide))));
    }

    @Test
    void enMiPerfil() throws Exception {
        mockMvc.perform(get("/perfil").with(user(detalles.cargar("ofrece.resena@kolab.pe", Rol.USUARIO))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(enlaceA(pide))));
    }

    private String codigoDe(Long idUsuario) {
        return usuarioRepository.findById(idUsuario).orElseThrow().getCodigo();
    }

    private String enlaceA(Long idUsuario) {
        return "href=\"/personas/" + codigoDe(idUsuario) + "\"";
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
