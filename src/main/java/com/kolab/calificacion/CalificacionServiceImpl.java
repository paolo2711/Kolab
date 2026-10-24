package com.kolab.calificacion;

import com.kolab.common.OperacionNoPermitidaException;
import com.kolab.common.Tiempo;
import com.kolab.perfil.Personas;
import com.kolab.perfil.ReputacionService;
import com.kolab.servicio.Servicio;
import com.kolab.usuario.Usuario;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de {@link CalificacionService}.
 */
@Service
public class CalificacionServiceImpl implements CalificacionService {

    private final CalificacionRepository calificacionRepository;
    private final ReputacionService reputacionService;
    private final Personas personas;

    public CalificacionServiceImpl(CalificacionRepository calificacionRepository,
                                   ReputacionService reputacionService, Personas personas) {
        this.calificacionRepository = calificacionRepository;
        this.reputacionService = reputacionService;
        this.personas = personas;
    }

    @Override
    @Transactional
    public void registrar(Servicio servicio, Long idEvaluador, CalificacionForm form) {
        if (!servicio.estaCerrado()) {
            throw new OperacionNoPermitidaException("Se califica cuando el servicio ya está cerrado.");
        }
        if (yaCalifico(servicio.getId(), idEvaluador)) {
            throw new OperacionNoPermitidaException("Ya calificaste este servicio.");
        }
        Usuario pide = servicio.getOferta().getSolicitud().getAutor();
        Usuario ofrece = servicio.getOferta().getUsuario();
        boolean evaluaQuienPide = pide.getId().equals(idEvaluador);
        Usuario evaluador = evaluaQuienPide ? pide : ofrece;
        Usuario evaluado = evaluaQuienPide ? ofrece : pide;

        String comentario = form.getComentario() == null || form.getComentario().isBlank()
                ? null : form.getComentario().trim();
        calificacionRepository.save(new Calificacion(servicio, evaluador, evaluado, form.getPuntaje(), comentario));
        reputacionService.sumarCalificacion(evaluado.getId(), form.getPuntaje());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean yaCalifico(Long idServicio, Long idEvaluador) {
        return calificacionRepository.existsByServicioIdAndEvaluadorId(idServicio, idEvaluador);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ResenaResumen> recibidas(Long idUsuario, Pageable pagina) {
        return calificacionRepository.findByEvaluadoIdOrderByFechaDesc(idUsuario, pagina)
                .map(c -> new ResenaResumen(personas.de(c.getEvaluador()), c.getPuntaje(), c.getComentario(),
                        c.getServicio().getOferta().getSolicitud().getTitulo(), Tiempo.hace(c.getFecha())));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, String> ultimoComentarioDe(Collection<Long> idsUsuario) {
        Map<Long, String> ultimos = new HashMap<>();
        if (!idsUsuario.isEmpty()) {
            for (Object[] fila : calificacionRepository.ultimosComentarios(idsUsuario)) {
                ultimos.put((Long) fila[0], (String) fila[1]);
            }
        }
        return ultimos;
    }
}
