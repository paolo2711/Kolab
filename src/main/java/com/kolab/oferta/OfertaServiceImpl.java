package com.kolab.oferta;

import com.kolab.common.OperacionNoPermitidaException;
import com.kolab.common.RecursoNoEncontradoException;
import com.kolab.servicio.Servicio;
import com.kolab.servicio.ServicioRepository;
import com.kolab.solicitud.Solicitud;
import com.kolab.solicitud.SolicitudRepository;
import com.kolab.usuario.UsuarioRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de {@link OfertaService}. El porcentaje de comisión sale de
 * {@code kolab.comision-porcentaje}, no del código.
 */
@Service
public class OfertaServiceImpl implements OfertaService {

    private static final Logger log = LoggerFactory.getLogger(OfertaServiceImpl.class);

    private final OfertaRepository ofertaRepository;
    private final SolicitudRepository solicitudRepository;
    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ResumenesDeOferta resumenes;
    private final MisOfertas misOfertas;
    private final BigDecimal comision;

    public OfertaServiceImpl(OfertaRepository ofertaRepository, SolicitudRepository solicitudRepository,
                             ServicioRepository servicioRepository, UsuarioRepository usuarioRepository,
                             ResumenesDeOferta resumenes, MisOfertas misOfertas,
                             @Value("${kolab.comision-porcentaje}") BigDecimal comision) {
        this.ofertaRepository = ofertaRepository;
        this.solicitudRepository = solicitudRepository;
        this.servicioRepository = servicioRepository;
        this.usuarioRepository = usuarioRepository;
        this.resumenes = resumenes;
        this.misOfertas = misOfertas;
        this.comision = comision;
    }

    @Override
    @Transactional
    public Long ofertar(Long idSolicitud, OfertaForm form, Long idUsuario) {
        Solicitud s = solicitudRepository.findConPartesById(idSolicitud)
                .orElseThrow(() -> new RecursoNoEncontradoException("solicitud", idSolicitud));
        if (!s.laPuedeOfertar(idUsuario)) {
            throw new OperacionNoPermitidaException("Esta solicitud ya no recibe ofertas.");
        }
        if (ofertaRepository.findBySolicitudIdAndUsuarioId(idSolicitud, idUsuario).isPresent()) {
            throw new OperacionNoPermitidaException("Ya ofertaste en esta solicitud.");
        }
        String mensaje = form.getMensaje() == null || form.getMensaje().isBlank() ? null : form.getMensaje().trim();
        Oferta nueva = new Oferta(s, usuarioRepository.getReferenceById(idUsuario), form.getMonto(), mensaje);
        return ofertaRepository.save(nueva).getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OfertaResumen> miOfertaEn(Long idSolicitud, Long idUsuario) {
        return ofertaRepository.findBySolicitudIdAndUsuarioId(idSolicitud, idUsuario)
                .map(o -> resumenes.de(List.of(o), idUsuario).get(0));
    }

    @Override
    @Transactional
    public Long aceptar(Long idOferta, Long idAutor) {
        Oferta elegida = delAutor(idOferta, idAutor);
        if (!elegida.getSolicitud().puedeEditarse() || elegida.getEstado() != EstadoOferta.ENVIADA) {
            throw new OperacionNoPermitidaException("Esa oferta ya no se puede aceptar.");
        }
        Servicio servicio = servicioRepository.save(elegida.aceptar(comision));
        ofertaRepository.findBySolicitudIdOrderByMontoPropuesto(elegida.getSolicitud().getId()).stream()
                .filter(o -> !o.getId().equals(idOferta) && o.getEstado() == EstadoOferta.ENVIADA)
                .forEach(Oferta::rechazar);
        log.info("oferta {} aceptada, servicio {} creado", idOferta, servicio.getId());
        return servicio.getId();
    }

    @Override
    @Transactional
    public void rechazar(Long idOferta, Long idAutor) {
        Oferta oferta = delAutor(idOferta, idAutor);
        if (oferta.getEstado() != EstadoOferta.ENVIADA) {
            throw new OperacionNoPermitidaException("Esa oferta ya no está en juego.");
        }
        oferta.rechazar();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OfertaEnviada> mias(Long idUsuario, Pageable pagina) {
        return misOfertas.de(idUsuario, pagina);
    }

    @Override
    @Transactional(readOnly = true)
    public long cuantasMias(Long idUsuario) {
        return ofertaRepository.countByUsuarioId(idUsuario);
    }

    // solo quien publicó la solicitud decide sobre las ofertas que le llegan
    private Oferta delAutor(Long idOferta, Long idAutor) {
        Oferta oferta = ofertaRepository.findConPartesById(idOferta)
                .orElseThrow(() -> new RecursoNoEncontradoException("oferta", idOferta));
        if (!oferta.getSolicitud().esDe(idAutor)) {
            throw new RecursoNoEncontradoException("oferta", idOferta);
        }
        return oferta;
    }
}
