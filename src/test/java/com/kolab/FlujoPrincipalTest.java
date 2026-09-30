package com.kolab;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kolab.calificacion.CalificacionForm;
import com.kolab.common.OperacionNoPermitidaException;
import com.kolab.common.RecursoNoEncontradoException;
import com.kolab.mensaje.MensajeService;
import com.kolab.oferta.EstadoOferta;
import com.kolab.oferta.OfertaForm;
import com.kolab.oferta.OfertaService;
import com.kolab.perfil.PerfilService;
import com.kolab.perfil.ReputacionService;
import com.kolab.servicio.ServicioDetalle;
import com.kolab.servicio.ServicioService;
import com.kolab.solicitud.CatalogoService;
import com.kolab.solicitud.EstadoSolicitud;
import com.kolab.solicitud.FiltroSolicitudes;
import com.kolab.solicitud.Modalidad;
import com.kolab.solicitud.SolicitudForm;
import com.kolab.solicitud.SolicitudResumen;
import com.kolab.solicitud.SolicitudService;
import com.kolab.usuario.RegistroForm;
import com.kolab.usuario.UsuarioService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

// el flujo de los cinco procesos contra la base de verdad (H2 con las mismas migraciones): publicar,
// ofertar, aceptar, conversar, cerrar y calificar. cada prueba se deshace al terminar.
@SpringBootTest
@Transactional
class FlujoPrincipalTest {

    private static final long MUSICA = 1L;

    @Autowired private UsuarioService usuarioService;
    @Autowired private PerfilService perfilService;
    @Autowired private SolicitudService solicitudService;
    @Autowired private CatalogoService catalogoService;
    @Autowired private OfertaService ofertaService;
    @Autowired private ServicioService servicioService;
    @Autowired private MensajeService mensajeService;
    @Autowired private ReputacionService reputacionService;

    private Long pide;
    private Long ofrece;
    private Long otro;

    @BeforeEach
    void tresPersonas() {
        pide = registrar("paolo@kolab.pe");
        ofrece = registrar("luis@kolab.pe");
        otro = registrar("carlos@kolab.pe");
        perfilService.guardarCategorias(ofrece, List.of(MUSICA));
        perfilService.guardarCategorias(otro, List.of(MUSICA));
    }

    @Test
    void publicarDejaLaSolicitudAbiertaYVisibleParaLosDemas() {
        Long id = publicar(null, "80");

        assertThat(solicitudService.propia(id, pide).estado()).isEqualTo(EstadoSolicitud.ABIERTA);
        assertThat(explorar(ofrece)).extracting(SolicitudResumen::id).contains(id);
        assertThat(explorar(pide)).extracting(SolicitudResumen::id).doesNotContain(id);
    }

    @Test
    void unaPropuestaDirectaSoloLaVeYLaOfertaSuDestinatario() {
        Long id = publicar(ofrece, "70");

        assertThat(explorar(ofrece)).extracting(SolicitudResumen::id).contains(id);
        assertThat(explorar(otro)).extracting(SolicitudResumen::id).doesNotContain(id);
        assertThatThrownBy(() -> solicitudService.paraOfertar(id, otro))
                .isInstanceOf(RecursoNoEncontradoException.class);
        assertThatThrownBy(() -> ofertaService.ofertar(id, oferta("70"), otro))
                .isInstanceOf(OperacionNoPermitidaException.class);
    }

    @Test
    void nadieOfertaDosVecesNiEnLoSuyo() {
        Long id = publicar(null, "80");
        ofertaService.ofertar(id, oferta("80"), ofrece);

        assertThatThrownBy(() -> ofertaService.ofertar(id, oferta("75"), ofrece))
                .isInstanceOf(OperacionNoPermitidaException.class);
        assertThatThrownBy(() -> ofertaService.ofertar(id, oferta("75"), pide))
                .isInstanceOf(OperacionNoPermitidaException.class);
    }

    @Test
    void aceptarCreaElServicioConSuComisionYDescartaLasDemas() {
        Long id = publicar(null, "80");
        Long elegida = ofertaService.ofertar(id, oferta("60"), ofrece);
        ofertaService.ofertar(id, oferta("75"), otro);

        Long idServicio = ofertaService.aceptar(elegida, pide);

        ServicioDetalle servicio = servicioService.detalle(idServicio, pide);
        assertThat(servicio.montoFinal()).isEqualByComparingTo("60");
        assertThat(servicio.comision()).isEqualByComparingTo("3.00");
        assertThat(solicitudService.propia(id, pide).estado()).isEqualTo(EstadoSolicitud.EN_CURSO);
        assertThat(solicitudService.propia(id, pide).ofertas())
                .extracting(o -> o.estado())
                .containsExactlyInAnyOrder(EstadoOferta.ACEPTADA, EstadoOferta.RECHAZADA);
        assertThat(explorar(otro)).extracting(SolicitudResumen::id).doesNotContain(id);
    }

    @Test
    void soloQuienPublicoPuedeAceptar() {
        Long id = publicar(null, "80");
        Long idOferta = ofertaService.ofertar(id, oferta("80"), ofrece);

        assertThatThrownBy(() -> ofertaService.aceptar(idOferta, otro))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void losMensajesLeLleganSoloALaOtraParteYSeLeenAlAbrir() {
        Long id = publicar(null, "80");
        Long idOferta = ofertaService.ofertar(id, oferta("80"), ofrece);

        mensajeService.enviar(idOferta, "¿Puedes el sábado?", pide);

        assertThat(mensajeService.sinLeer(ofrece)).isEqualTo(1);
        assertThat(mensajeService.sinLeer(pide)).isZero();
        mensajeService.abrir(idOferta, ofrece);
        assertThat(mensajeService.sinLeer(ofrece)).isZero();
        assertThatThrownBy(() -> mensajeService.abrir(idOferta, otro))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void cerrarYCalificarActualizaElPromedioDeLosDos() {
        Long id = publicar(null, "80");
        Long idServicio = ofertaService.aceptar(ofertaService.ofertar(id, oferta("80"), ofrece), pide);

        assertThatThrownBy(() -> servicioService.cerrarYCalificar(idServicio, nota(5), ofrece))
                .isInstanceOf(OperacionNoPermitidaException.class);

        servicioService.cerrarYCalificar(idServicio, nota(5), pide);
        servicioService.cerrarYCalificar(idServicio, nota(4), ofrece);

        assertThat(reputacionService.de(ofrece).promedio()).isEqualByComparingTo("5.00");
        assertThat(reputacionService.de(ofrece).serviciosDados()).isEqualTo(1);
        assertThat(reputacionService.de(pide).promedio()).isEqualByComparingTo("4.00");
        assertThat(solicitudService.propia(id, pide).estado()).isEqualTo(EstadoSolicitud.CERRADA);
        assertThatThrownBy(() -> servicioService.cerrarYCalificar(idServicio, nota(5), pide))
                .isInstanceOf(OperacionNoPermitidaException.class);
    }

    @Test
    void cancelarLaSacaDelCatalogoYYaNoSeEdita() {
        Long id = publicar(null, "80");

        solicitudService.cancelar(id, pide);

        assertThat(explorar(ofrece)).extracting(SolicitudResumen::id).doesNotContain(id);
        assertThatThrownBy(() -> solicitudService.editar(id, solicitud(null, "90"), pide))
                .isInstanceOf(OperacionNoPermitidaException.class);
    }

    private List<SolicitudResumen> explorar(Long quien) {
        return catalogoService.explorar(new FiltroSolicitudes(), quien, PageRequest.of(0, 50)).getContent();
    }

    private Long publicar(Long para, String precio) {
        return solicitudService.publicar(solicitud(para, precio), pide);
    }

    private SolicitudForm solicitud(Long para, String precio) {
        SolicitudForm form = new SolicitudForm();
        form.setTitulo("Clases de guitarra para principiante");
        form.setIdCategoria(MUSICA);
        form.setModalidad(Modalidad.PRESENCIAL);
        form.setDistrito("Jesús María");
        form.setDescripcion("Quiero aprender desde cero, los sábados en la mañana.");
        form.setFechaDeseada(LocalDate.now().plusDays(7));
        form.setPrecioPropuesto(new BigDecimal(precio));
        form.setIdDestinatario(para);
        return form;
    }

    private OfertaForm oferta(String monto) {
        OfertaForm form = new OfertaForm();
        form.setMonto(new BigDecimal(monto));
        return form;
    }

    private CalificacionForm nota(int puntaje) {
        CalificacionForm form = new CalificacionForm();
        form.setPuntaje(puntaje);
        return form;
    }

    private Long registrar(String email) {
        RegistroForm form = new RegistroForm();
        form.setNombre(email.substring(0, 1).toUpperCase() + email.substring(1, email.indexOf('@')));
        form.setApellidos("Prueba");
        form.setEmail(email);
        form.setPassword("secreto123");
        form.setConfirmacion("secreto123");
        return usuarioService.registrar(form).getId();
    }
}
