package com.kolab.oferta;

import com.kolab.common.Fotos;
import com.kolab.common.Tiempo;
import com.kolab.mensaje.MensajeRepository;
import com.kolab.solicitud.Solicitud;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * La lista de ofertas propias de Mi actividad. Los competidores y los mensajes sin leer de toda la
 * página salen en una consulta cada uno.
 */
@Component
class MisOfertas {

    private final OfertaRepository ofertaRepository;
    private final MensajeRepository mensajeRepository;
    private final Fotos fotos;

    MisOfertas(OfertaRepository ofertaRepository, MensajeRepository mensajeRepository, Fotos fotos) {
        this.ofertaRepository = ofertaRepository;
        this.mensajeRepository = mensajeRepository;
        this.fotos = fotos;
    }

    Page<OfertaEnviada> de(Long idUsuario, Pageable pagina) {
        Page<Oferta> propias = ofertaRepository.findByUsuarioIdOrderByFechaOfertaDesc(idUsuario, pagina);
        if (propias.isEmpty()) {
            return Page.empty(pagina);
        }
        Map<Long, Long> competencia = contar(ofertaRepository.contarVigentesPorSolicitud(
                propias.getContent().stream().map(o -> o.getSolicitud().getId()).toList()));
        Map<Long, Long> sinLeer = contar(mensajeRepository.contarSinLeerPorOferta(
                propias.getContent().stream().map(Oferta::getId).toList(), idUsuario));
        return propias.map(o -> aEnviada(o, competencia.getOrDefault(o.getSolicitud().getId(), 1L),
                sinLeer.getOrDefault(o.getId(), 0L)));
    }

    private OfertaEnviada aEnviada(Oferta o, long cuantas, long sinLeer) {
        Solicitud s = o.getSolicitud();
        String categoria = s.getCategoria().getNombre();
        return new OfertaEnviada(o.getId(), s.getId(), s.getTitulo(), categoria, s.getCategoria().getIcono(),
                fotos.deCategoria(categoria), s.getPrecioPropuesto(), o.getMontoPropuesto(), o.getEstado(),
                sinLeer, cuantas, Tiempo.hace(o.getFechaOferta()));
    }

    private static Map<Long, Long> contar(List<Object[]> filas) {
        Map<Long, Long> cuenta = new HashMap<>();
        for (Object[] fila : filas) {
            cuenta.put((Long) fila[0], (Long) fila[1]);
        }
        return cuenta;
    }
}
