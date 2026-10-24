package com.kolab.perfil;

import com.kolab.servicio.ServicioRepository;
import com.kolab.usuario.UsuarioRepository;
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
    private final UsuarioRepository usuarioRepository;

    public ReputacionServiceImpl(PerfilRepository perfilRepository,
                                 ServicioRepository servicioRepository,
                                 UsuarioRepository usuarioRepository) {
        this.perfilRepository = perfilRepository;
        this.servicioRepository = servicioRepository;
        this.usuarioRepository = usuarioRepository;
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
        for (Long id : idsUsuario) {
            reputaciones.putIfAbsent(id, new Reputacion(Reputacion.NUEVA.promedio(), 0,
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
        Perfil perfil = perfilRepository.findByUsuarioId(idUsuario)
                .orElseGet(() -> perfilRepository.save(new Perfil(usuarioRepository.getReferenceById(idUsuario))));
        perfil.registrarCalificacion(puntaje);
    }
}
