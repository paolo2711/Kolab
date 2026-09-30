package com.kolab.mensaje;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * La mensajería interna. Cada conversación cuelga de una oferta y solo hablan sus dos partes:
 * quien publicó la solicitud y quien ofertó.
 */
public interface MensajeService {

    Page<ConversacionResumen> bandeja(Long idUsuario, Pageable pagina);

    /**
     * Abre la conversación y marca como leído lo que le escribieron a esta persona.
     */
    ConversacionDetalle abrir(Long idOferta, Long idUsuario);

    /**
     * Los mensajes de varias conversaciones a la vez, sin marcarlos como leídos. Cada id pedido
     * viene en el resultado, con lista vacía si todavía no se escribieron.
     */
    Map<Long, List<MensajeResumen>> hilos(Collection<Long> idsOferta, Long idUsuario);

    /**
     * @return el id del mensaje nuevo
     */
    Long enviar(Long idOferta, String contenido, Long idUsuario);

    long sinLeer(Long idUsuario);
}
