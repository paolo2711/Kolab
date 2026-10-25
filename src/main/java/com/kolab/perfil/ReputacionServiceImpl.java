package com.kolab.perfil;

import com.kolab.common.RecursoNoEncontradoException;
import com.kolab.servicio.ServicioRepository;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de {@link ReputacionService}.
 */
@Service
public class ReputacionServiceImpl implements ReputacionService {

    private final PerfilRepository perfilRepository;
    private final ServicioRepository servicioRepository;

    public ReputacionServiceImpl(PerfilRepository perfilRepository, ServicioRepository servicioRepository) {
        this.perfilRepository = perfilRepository;
        this.servicioRepository = servicioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Reputacion> de(Collection<Long> idsUsuario) {
        if (idsUsuario.isEmpty()) {
            return Map.of();
        }
        Map<Long, Long> dados = new HashMap<>();
        for (Object[] fila : servicioRepository.contarCerradosPorQuienOfrece(idsUsuario)) {
            dados.put((Long) fila[0], (Long) fila[1]);
        }
        Map<Long, Reputacion> reputaciones = new HashMap<>();
        for (Perfil p : perfilRepository.findByUsuarioIdIn(idsUsuario)) {
            Long id = p.getUsuario().getId();
            reputaciones.put(id, new Reputacion(p.getCalifPromedio(), p.getTotalCalificaciones(),
                    dados.getOrDefault(id, 0L)));
        }
        return reputaciones;
    }

    @Override
    @Transactional(readOnly = true)
    public Reputacion de(Long idUsuario) {
        return de(List.of(idUsuario)).get(idUsuario);
    }

    @Override
    @Transactional
    public void sumarCalificacion(Long idUsuario, int puntaje) {
        perfilRepository.findByUsuarioId(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("persona", idUsuario))
                .registrarCalificacion(puntaje);
    }
}
