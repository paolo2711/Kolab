package com.kolab.mensaje;

import static com.kolab.CuentasDePrueba.CLAVE;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
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
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.transaction.annotation.Transactional;

// el «Visto» sale del mismo dato que los no leidos: se marca cuando la otra parte abre la conversacion
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VistoTest {

    private static final long MUSICA = 1L;
    private static final String VISTO = "class=\"k-msj-visto\">Visto<";

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioService usuarioService;
    @Autowired private DetallesUsuarioService detalles;
    @Autowired private PerfilService perfilService;
    @Autowired private SolicitudService solicitudService;
    @Autowired private OfertaService ofertaService;
    @Autowired private MensajeService mensajeService;
    @Autowired private EntityManager entityManager;

    private Long pide;
    private Long ofrece;
    private Long oferta;
    private Long servicio;
    private UsuarioAutenticado quienOfrece;

    @BeforeEach
    void unServicioConUnMensaje() {
        pide = registrar("pide.visto@kolab.pe");
        ofrece = registrar("ofrece.visto@kolab.pe");
        perfilService.guardarCategorias(ofrece, List.of(MUSICA));
        Long solicitud = solicitudService.publicar(solicitud(), pide);
        oferta = ofertaService.ofertar(solicitud, ofertaDe("90"), ofrece);
        servicio = ofertaService.aceptar(oferta, pide);
        mensajeService.enviar(oferta, "¿El sábado a las diez te va?", ofrece);
        quienOfrece = detalles.cargar("ofrece.visto@kolab.pe", Rol.USUARIO);
    }

    @Test
    void mientrasLaOtraParteNoAbraNoSaleVisto() throws Exception {
        verEnLosDosChats(not(containsString(VISTO)));
    }

    @Test
    void cuandoLaOtraParteAbreLaConversacionSaleVisto() throws Exception {
        mensajeService.abrir(oferta, pide);
        otroPedido();

        verEnLosDosChats(containsString(VISTO));
    }

    @Test
    void siLaOtraParteYaContestoNoHaceFalta() throws Exception {
        mensajeService.abrir(oferta, pide);
        mensajeService.enviar(oferta, "Sí, perfecto.", pide);
        otroPedido();

        verEnLosDosChats(not(containsString(VISTO)));
    }

    // marcar como leido es un update directo: en la app el que mira llega en otro pedido, aqui se limpia a mano
    private void otroPedido() {
        entityManager.flush();
        entityManager.clear();
    }

    private void verEnLosDosChats(Matcher<String> esperado) throws Exception {
        ResultMatcher contenido = content().string(esperado);
        mockMvc.perform(get("/servicios/{id}", servicio).with(user(quienOfrece)))
                .andExpect(status().isOk())
                .andExpect(contenido);
        mockMvc.perform(get("/mensajes/{id}", oferta).with(user(quienOfrece)))
                .andExpect(status().isOk())
                .andExpect(contenido);
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
