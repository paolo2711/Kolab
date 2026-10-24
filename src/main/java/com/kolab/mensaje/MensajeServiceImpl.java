package com.kolab.mensaje;

import com.kolab.common.OperacionNoPermitidaException;
import com.kolab.common.RecursoNoEncontradoException;
import com.kolab.common.Tiempo;
import com.kolab.oferta.Oferta;
import com.kolab.oferta.OfertaRepository;
import com.kolab.perfil.Personas;
import com.kolab.servicio.Servicio;
import com.kolab.servicio.ServicioRepository;
import com.kolab.solicitud.EstadoSolicitud;
import com.kolab.solicitud.Solicitud;
import com.kolab.usuario.Usuario;
import com.kolab.usuario.UsuarioRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de {@link MensajeService}.
 */
@Service
public class MensajeServiceImpl implements MensajeService {

    // lo que se muestra de una conversación al abrirla; lo anterior queda en la base
    private static final int ULTIMOS = 50;

    private final MensajeRepository mensajeRepository;
    private final OfertaRepository ofertaRepository;
    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;
    private final Personas personas;

    public MensajeServiceImpl(MensajeRepository mensajeRepository, OfertaRepository ofertaRepository,
                              ServicioRepository servicioRepository, UsuarioRepository usuarioRepository,
                              Personas personas) {
        this.mensajeRepository = mensajeRepository;
        this.ofertaRepository = ofertaRepository;
        this.servicioRepository = servicioRepository;
        this.usuarioRepository = usuarioRepository;
        this.personas = personas;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConversacionResumen> bandeja(Long idUsuario, Pageable pagina) {
        Page<Mensaje> ultimos = mensajeRepository.ultimosDeCadaConversacion(idUsuario, pagina);
        List<Long> idsOferta = ultimos.getContent().stream().map(m -> m.getOferta().getId()).toList();
        Map<Long, Long> sinLeer = new HashMap<>();
        if (!idsOferta.isEmpty()) {
            mensajeRepository.contarSinLeerPorOferta(idsOferta, idUsuario)
                    .forEach(f -> sinLeer.put((Long) f[0], (Long) f[1]));
        }
        return ultimos.map(m -> {
            Oferta o = m.getOferta();
            return new ConversacionResumen(o.getSolicitud().getTitulo(), personas.de(contraparte(o, idUsuario)),
                    m.getContenido(), Tiempo.hace(m.getFechaEnvio()), sinLeer.getOrDefault(o.getId(), 0L) > 0,
                    "/mensajes/" + o.getId());
        });
    }

    @Override
    @Transactional
    public ConversacionDetalle abrir(Long idOferta, Long idUsuario) {
        Oferta o = deUnaParte(idOferta, idUsuario);
        mensajeRepository.marcarLeidos(idOferta, idUsuario);
        List<Mensaje> recientes = new ArrayList<>(mensajeRepository
                .findByOfertaIdOrderByFechaEnvioDesc(idOferta, PageRequest.of(0, ULTIMOS)).getContent());
        Collections.reverse(recientes);
        Servicio servicio = servicioRepository.findByOfertaId(idOferta).orElse(null);
        return new ConversacionDetalle(idOferta, o.getSolicitud().getTitulo(),
                personas.de(contraparte(o, idUsuario)), enlaceDe(o, servicio, idUsuario),
                servicio != null ? "Ver el servicio" : "Ver la solicitud",
                recientes.stream().map(m -> aResumen(m, idUsuario)).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, List<MensajeResumen>> hilos(Collection<Long> idsOferta, Long idUsuario) {
        if (idsOferta.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<MensajeResumen>> hilos = mensajeRepository.findByOfertaIdInOrderByFechaEnvio(idsOferta)
                .stream()
                .collect(Collectors.groupingBy(m -> m.getOferta().getId(),
                        Collectors.mapping(m -> aResumen(m, idUsuario), Collectors.toList())));
        // una conversación sin mensajes también está, vacía: así la vista no pregunta por nulos
        idsOferta.forEach(id -> hilos.putIfAbsent(id, List.of()));
        return hilos;
    }

    @Override
    @Transactional
    public Long enviar(Long idOferta, String contenido, Long idUsuario) {
        Oferta o = deUnaParte(idOferta, idUsuario);
        if (o.getSolicitud().getEstado() == EstadoSolicitud.CANCELADA) {
            throw new OperacionNoPermitidaException("La solicitud se canceló: la conversación quedó cerrada.");
        }
        return mensajeRepository.save(new Mensaje(o, usuarioRepository.getReferenceById(idUsuario), contenido.trim()))
                .getId();
    }

    @Override
    @Transactional(readOnly = true)
    public long sinLeer(Long idUsuario) {
        return mensajeRepository.contarSinLeer(idUsuario);
    }

    // solo hablan quien publicó y quien ofertó; para cualquier otro la conversación no existe
    private Oferta deUnaParte(Long idOferta, Long idUsuario) {
        Oferta o = ofertaRepository.findConPartesById(idOferta)
                .orElseThrow(() -> new RecursoNoEncontradoException("conversación", idOferta));
        if (!o.getUsuario().getId().equals(idUsuario) && !o.getSolicitud().esDe(idUsuario)) {
            throw new RecursoNoEncontradoException("conversación", idOferta);
        }
        return o;
    }

    private Usuario contraparte(Oferta o, Long idUsuario) {
        Solicitud s = o.getSolicitud();
        return s.esDe(idUsuario) ? o.getUsuario() : s.getAutor();
    }

    private String enlaceDe(Oferta o, Servicio servicio, Long idUsuario) {
        if (servicio != null) {
            return "/servicios/" + servicio.getId();
        }
        Long idSolicitud = o.getSolicitud().getId();
        return o.getSolicitud().esDe(idUsuario) ? "/mis-solicitudes/" + idSolicitud : "/solicitudes/" + idSolicitud;
    }

    private MensajeResumen aResumen(Mensaje m, Long idUsuario) {
        return new MensajeResumen(m.getContenido(), Tiempo.hora(m.getFechaEnvio()),
                m.getEmisor().getId().equals(idUsuario));
    }
}
