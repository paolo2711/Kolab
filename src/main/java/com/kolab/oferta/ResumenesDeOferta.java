package com.kolab.oferta;

import com.kolab.common.Tiempo;
import com.kolab.mensaje.MensajeRepository;
import com.kolab.perfil.Personas;
import com.kolab.perfil.Reputacion;
import com.kolab.perfil.ReputacionService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Convierte ofertas en fichas para comparar: quién es, su reputación, cuánto pide y si hay mensajes
 * sin leer. La reputación y los mensajes de toda la lista salen en una consulta cada uno.
 */
@Component
public class ResumenesDeOferta {

    private final ReputacionService reputacionService;
    private final MensajeRepository mensajeRepository;
    private final Personas personas;

    public ResumenesDeOferta(ReputacionService reputacionService, MensajeRepository mensajeRepository,
                             Personas personas) {
        this.reputacionService = reputacionService;
        this.mensajeRepository = mensajeRepository;
        this.personas = personas;
    }

    /**
     * @param quienMira para contar solo los mensajes que le escribieron a esa persona
     */
    public List<OfertaResumen> de(List<Oferta> ofertas, Long quienMira) {
        if (ofertas.isEmpty()) {
            return List.of();
        }
        Map<Long, Reputacion> reputaciones = reputacionService.de(
                ofertas.stream().map(o -> o.getUsuario().getId()).distinct().toList());
        Map<Long, Long> sinLeer = new HashMap<>();
        for (Object[] fila : mensajeRepository.contarSinLeerPorOferta(
                ofertas.stream().map(Oferta::getId).toList(), quienMira)) {
            sinLeer.put((Long) fila[0], (Long) fila[1]);
        }
        return ofertas.stream()
                .map(o -> new OfertaResumen(o.getId(), personas.de(o.getUsuario()),
                        reputaciones.get(o.getUsuario().getId()), o.getMontoPropuesto(), o.getMensaje(),
                        o.getEstado(), Tiempo.hace(o.getFechaOferta()), sinLeer.getOrDefault(o.getId(), 0L)))
                .toList();
    }
}
