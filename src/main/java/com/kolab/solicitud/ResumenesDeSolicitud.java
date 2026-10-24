package com.kolab.solicitud;

import com.kolab.common.Distancia;
import com.kolab.common.Fotos;
import com.kolab.common.Tiempo;
import com.kolab.oferta.Oferta;
import com.kolab.oferta.OfertaRepository;
import com.kolab.perfil.Persona;
import com.kolab.perfil.Personas;
import com.kolab.usuario.Usuario;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Convierte solicitudes en filas listas para mostrar. Las ofertas de toda la lista se traen en una
 * sola consulta, no una por fila.
 */
@Component
class ResumenesDeSolicitud {

    private final OfertaRepository ofertaRepository;
    private final Personas personas;
    private final Fotos fotos;

    ResumenesDeSolicitud(OfertaRepository ofertaRepository, Personas personas, Fotos fotos) {
        this.ofertaRepository = ofertaRepository;
        this.personas = personas;
        this.fotos = fotos;
    }

    /**
     * @param desde quien mira, para calcular a qué distancia queda cada una; puede ser {@code null}
     */
    List<SolicitudResumen> de(List<Solicitud> solicitudes, Usuario desde) {
        if (solicitudes.isEmpty()) {
            return List.of();
        }
        List<Long> ids = solicitudes.stream().map(Solicitud::getId).toList();
        Map<Long, List<Oferta>> ofertas = ofertaRepository.vigentesEn(ids).stream()
                .collect(Collectors.groupingBy(o -> o.getSolicitud().getId()));
        return solicitudes.stream()
                .map(s -> aResumen(s, ofertas.getOrDefault(s.getId(), List.of()), desde))
                .toList();
    }

    private SolicitudResumen aResumen(Solicitud s, List<Oferta> ofertas, Usuario desde) {
        List<Persona> quienes = ofertas.stream().map(o -> personas.de(o.getUsuario())).toList();
        BigDecimal mejor = ofertas.isEmpty() ? null : ofertas.get(0).getMontoPropuesto();
        String categoria = s.getCategoria().getNombre();
        return new SolicitudResumen(s.getId(), s.getTitulo(), s.getCategoria().getId(), categoria,
                s.getCategoria().getIcono(), fotos.deCategoria(categoria), s.getModalidad(),
                s.getDistrito(), s.getPrecioPropuesto(), mejor, quienes,
                Tiempo.hace(s.getFechaPublicacion()), s.esPropuestaDirecta(), kilometros(desde, s));
    }

    static Double kilometros(Usuario desde, Solicitud s) {
        if (desde == null || desde.getLatitud() == null || desde.getLongitud() == null
                || s.getLatitud() == null || s.getLongitud() == null) {
            return null;
        }
        return Distancia.entre(desde.getLatitud().doubleValue(), desde.getLongitud().doubleValue(),
                s.getLatitud().doubleValue(), s.getLongitud().doubleValue());
    }
}
