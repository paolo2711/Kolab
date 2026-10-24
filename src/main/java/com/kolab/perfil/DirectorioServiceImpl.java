package com.kolab.perfil;

import com.kolab.calificacion.CalificacionService;
import com.kolab.categoria.CategoriaService;
import com.kolab.common.RecursoNoEncontradoException;
import com.kolab.common.Tiempo;
import com.kolab.usuario.Usuario;
import com.kolab.usuario.UsuarioRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de {@link DirectorioService}. Las tarjetas de una lista se arman con una consulta
 * por dato (categorías, reputación, comentarios), sin importar cuántas personas haya.
 */
@Service
@Transactional(readOnly = true)
public class DirectorioServiceImpl implements DirectorioService {

    private static final int RESENAS = 5;

    private final PerfilRepository perfilRepository;
    private final PerfilCategoriaRepository perfilCategoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReputacionService reputacionService;
    private final CalificacionService calificacionService;
    private final CategoriaService categoriaService;
    private final Personas personas;

    public DirectorioServiceImpl(PerfilRepository perfilRepository,
                                 PerfilCategoriaRepository perfilCategoriaRepository,
                                 UsuarioRepository usuarioRepository, ReputacionService reputacionService,
                                 CalificacionService calificacionService, CategoriaService categoriaService,
                                 Personas personas) {
        this.perfilRepository = perfilRepository;
        this.perfilCategoriaRepository = perfilCategoriaRepository;
        this.usuarioRepository = usuarioRepository;
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
    public PerfilPublico publico(Long idUsuario) {
        Usuario u = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("persona", idUsuario));
        Optional<Perfil> perfil = perfilRepository.findByUsuarioId(idUsuario);
        List<Long> suyas = perfil.map(p -> perfilCategoriaRepository.findByPerfilId(p.getId()).stream()
                .map(PerfilCategoria::getIdCategoria).toList()).orElse(List.of());
        return new PerfilPublico(personas.de(u), u.getDistrito(), Tiempo.mesYAnio(u.getFechaRegistro()),
                perfil.map(Perfil::getDescripcion).orElse(null), perfil.map(Perfil::getExperiencia).orElse(null),
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
