package com.kolab.solicitud;

import com.kolab.common.Fotos;
import com.kolab.common.Tiempo;
import com.kolab.mensaje.MensajeRepository;
import com.kolab.oferta.Oferta;
import com.kolab.oferta.OfertaRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * La lista de solicitudes propias de Mi actividad. Las ofertas y los mensajes sin leer de toda la
 * página se cuentan en una consulta cada uno.
 */
@Component
class MisSolicitudes {

    private final SolicitudRepository solicitudRepository;
    private final OfertaRepository ofertaRepository;
    private final MensajeRepository mensajeRepository;
    private final Fotos fotos;

    MisSolicitudes(SolicitudRepository solicitudRepository, OfertaRepository ofertaRepository,
                   MensajeRepository mensajeRepository, Fotos fotos) {
        this.solicitudRepository = solicitudRepository;
        this.ofertaRepository = ofertaRepository;
        this.mensajeRepository = mensajeRepository;
        this.fotos = fotos;
    }

    Page<MiSolicitud> de(Long idAutor, Pageable pagina) {
        Page<Solicitud> propias = solicitudRepository.findByAutorIdOrderByFechaPublicacionDesc(idAutor, pagina);
        List<Long> ids = propias.getContent().stream().map(Solicitud::getId).toList();
        Map<Long, List<Oferta>> ofertas = ids.isEmpty() ? Map.of() : ofertaRepository.vigentesEn(ids).stream()
                .collect(Collectors.groupingBy(o -> o.getSolicitud().getId()));
        Map<Long, Long> sinLeer = sinLeerPorSolicitud(ofertas, idAutor);
        return propias.map(s -> aMiSolicitud(s, ofertas.getOrDefault(s.getId(), List.of()),
                sinLeer.getOrDefault(s.getId(), 0L)));
    }

    private Map<Long, Long> sinLeerPorSolicitud(Map<Long, List<Oferta>> ofertas, Long idAutor) {
        Map<Long, Long> porOferta = new HashMap<>();
        List<Long> idsOferta = ofertas.values().stream().flatMap(List::stream).map(Oferta::getId).toList();
        if (!idsOferta.isEmpty()) {
            for (Object[] fila : mensajeRepository.contarSinLeerPorOferta(idsOferta, idAutor)) {
                porOferta.put((Long) fila[0], (Long) fila[1]);
            }
        }
        Map<Long, Long> porSolicitud = new HashMap<>();
        ofertas.forEach((idSolicitud, lista) -> porSolicitud.put(idSolicitud,
                lista.stream().mapToLong(o -> porOferta.getOrDefault(o.getId(), 0L)).sum()));
        return porSolicitud;
    }

    private MiSolicitud aMiSolicitud(Solicitud s, List<Oferta> ofertas, long sinLeer) {
        String categoria = s.getCategoria().getNombre();
        return new MiSolicitud(s.getId(), s.getTitulo(), categoria, s.getCategoria().getIcono(),
                fotos.deCategoria(categoria), s.getModalidad(), s.getPrecioPropuesto(), ofertas.size(),
                ofertas.isEmpty() ? null : ofertas.get(0).getMontoPropuesto(), sinLeer, s.getEstado(),
                Tiempo.hace(s.getFechaPublicacion()));
    }
}
