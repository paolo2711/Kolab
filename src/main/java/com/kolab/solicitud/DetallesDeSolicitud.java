package com.kolab.solicitud;

import com.kolab.common.Fotos;
import com.kolab.common.RecursoNoEncontradoException;
import com.kolab.common.Tiempo;
import com.kolab.oferta.EstadoOferta;
import com.kolab.oferta.Oferta;
import com.kolab.oferta.OfertaRepository;
import com.kolab.oferta.ResumenesDeOferta;
import com.kolab.perfil.Personas;
import com.kolab.servicio.Servicio;
import com.kolab.servicio.ServicioRepository;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Arma la vista completa de una solicitud, desde el lado de quien la publicó o de quien oferta.
 */
@Component
class DetallesDeSolicitud {

    private final SolicitudRepository solicitudRepository;
    private final OfertaRepository ofertaRepository;
    private final ServicioRepository servicioRepository;
    private final ResumenesDeOferta resumenesDeOferta;
    private final Personas personas;
    private final Fotos fotos;

    DetallesDeSolicitud(SolicitudRepository solicitudRepository, OfertaRepository ofertaRepository,
                        ServicioRepository servicioRepository, ResumenesDeOferta resumenesDeOferta,
                        Personas personas, Fotos fotos) {
        this.solicitudRepository = solicitudRepository;
        this.ofertaRepository = ofertaRepository;
        this.servicioRepository = servicioRepository;
        this.resumenesDeOferta = resumenesDeOferta;
        this.personas = personas;
        this.fotos = fotos;
    }

    // quien ya ofertó la sigue viendo aunque se haya cerrado, para saber en qué terminó
    SolicitudDetalle paraOfertar(Long idSolicitud, Long idUsuario) {
        Solicitud s = cargar(idSolicitud);
        boolean yaOferto = ofertaRepository.findBySolicitudIdAndUsuarioId(idSolicitud, idUsuario).isPresent();
        if (!s.laPuedeOfertar(idUsuario) && !yaOferto) {
            throw new RecursoNoEncontradoException("solicitud", idSolicitud);
        }
        return armar(s, idUsuario);
    }

    SolicitudDetalle propia(Long idSolicitud, Long idAutor) {
        Solicitud s = cargar(idSolicitud);
        if (!s.esDe(idAutor)) {
            throw new RecursoNoEncontradoException("solicitud", idSolicitud);
        }
        return armar(s, idAutor);
    }

    Solicitud cargar(Long idSolicitud) {
        return solicitudRepository.findConPartesById(idSolicitud)
                .orElseThrow(() -> new RecursoNoEncontradoException("solicitud", idSolicitud));
    }

    private SolicitudDetalle armar(Solicitud s, Long quienMira) {
        List<Oferta> ofertas = ofertaRepository.findBySolicitudIdOrderByMontoPropuesto(s.getId());
        Long idServicio = ofertas.stream()
                .filter(o -> o.getEstado() == EstadoOferta.ACEPTADA)
                .findFirst()
                .flatMap(o -> servicioRepository.findByOfertaId(o.getId()))
                .map(Servicio::getId)
                .orElse(null);
        return new SolicitudDetalle(s.getId(), s.getTitulo(), s.getDescripcion(), s.getCategoria().getId(),
                s.getCategoria().getNombre(), fotos.deCategoria(s.getCategoria().getNombre()), s.getModalidad(), s.getDistrito(), s.getFechaDeseada(),
                s.getPrecioPropuesto(), Tiempo.hace(s.getFechaPublicacion()), s.getEstado(),
                personas.de(s.getAutor()),
                s.getDestinatario() == null ? null : personas.de(s.getDestinatario()),
                resumenesDeOferta.de(ofertas, quienMira), idServicio);
    }
}
