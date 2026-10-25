package com.kolab.perfil;

import com.kolab.calificacion.CalificacionService;
import com.kolab.categoria.CategoriaService;
import com.kolab.common.RecursoNoEncontradoException;
import com.kolab.common.Tiempo;
import com.kolab.usuario.Usuario;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * las tarjetas se arman con una consulta por dato, no una por persona
 */
@Service
@Transactional(readOnly = true)
public class DirectorioServiceImpl implements DirectorioService {

    private static final int RESENAS = 5;

    private final PerfilRepository perfilRepository;
    private final PerfilCategoriaRepository perfilCategoriaRepository;
    private final ReputacionService reputacionService;
    private final CalificacionService calificacionService;
    private final CategoriaService categoriaService;
    private final Personas personas;

    public DirectorioServiceImpl(PerfilRepository perfilRepository,
                                 PerfilCategoriaRepository perfilCategoriaRepository,
                                 ReputacionService reputacionService,
                                 CalificacionService calificacionService, CategoriaService categoriaService,
                                 Personas personas) {
        this.perfilRepository = perfilRepository;
        this.perfilCategoriaRepository = perfilCategoriaRepository;
        this.reputacionService = reputacionService;
        this.calificacionService = calificacionService;
        this.categoriaService = categoriaService;
        this.personas = personas;
    }

    @Override
    public List<ExpertoResumen> mejorCalificados(int cuantos) {
        return tarjetas(perfilRepository.mejorCalificados(PageRequest.of(0, cuantos)).getContent());
    }

    @Override
    public List<ExpertoResumen> queOfrecen(Long idCategoria, int cuantos) {
        return tarjetas(perfilRepository.queOfrecen(idCategoria, PageRequest.of(0, cuantos)).getContent());
    }

    @Override
    public PerfilPublico publico(String codigo) {
        Perfil perfil = perfilRepository.findByUsuarioCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("persona " + codigo));
        Usuario u = perfil.getUsuario();
        Long idUsuario = u.getId();
        List<Long> suyas = perfilCategoriaRepository.findByPerfilId(perfil.getId()).stream()
                .map(PerfilCategoria::getIdCategoria).toList();
        return new PerfilPublico(personas.de(u), u.getDistrito(), Tiempo.mesYAnio(u.getFechaRegistro()),
                perfil.getDescripcion(), perfil.getExperiencia(),
                reputacionService.de(idUsuario),
                categoriaService.activas().stream().filter(c -> suyas.contains(c.id())).toList(),
                calificacionService.recibidas(idUsuario, PageRequest.of(0, RESENAS)).getContent());
    }

    private List<ExpertoResumen> tarjetas(List<Perfil> perfiles) {
        if (perfiles.isEmpty()) {
            return List.of();
        }
        List<Long> idsUsuario = perfiles.stream().map(p -> p.getUsuario().getId()).toList();
        Map<Long, String> enQue = perfilCategoriaRepository
                .findByPerfilIdIn(perfiles.stream().map(Perfil::getId).toList()).stream()
                .collect(Collectors.groupingBy(pc -> pc.getPerfil().getId(),
                        Collectors.mapping(pc -> pc.getCategoria().getNombre(), Collectors.joining(" · "))));
        Map<Long, Reputacion> reputaciones = reputacionService.de(idsUsuario);
        Map<Long, String> comentarios = calificacionService.ultimoComentarioDe(idsUsuario);
        return perfiles.stream()
                .map(p -> {
                    Usuario u = p.getUsuario();
                    return new ExpertoResumen(personas.de(u), u.getDistrito(), enQue.get(p.getId()),
                            reputaciones.get(u.getId()), comentarios.get(u.getId()));
                })
                .toList();
    }
}
