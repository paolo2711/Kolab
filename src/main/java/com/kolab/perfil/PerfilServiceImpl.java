package com.kolab.perfil;

import com.kolab.calificacion.CalificacionService;
import com.kolab.categoria.CategoriaRepository;
import com.kolab.common.RecursoNoEncontradoException;
import com.kolab.common.Tiempo;
import com.kolab.solicitud.SolicitudRepository;
import com.kolab.usuario.Usuario;
import com.kolab.usuario.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de {@link PerfilService}.
 */
@Service
public class PerfilServiceImpl implements PerfilService {

    private static final int RESENAS = 5;

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PerfilCategoriaRepository perfilCategoriaRepository;
    private final CategoriaRepository categoriaRepository;
    private final SolicitudRepository solicitudRepository;
    private final ReputacionService reputacionService;
    private final CalificacionService calificacionService;

    public PerfilServiceImpl(UsuarioRepository usuarioRepository, PerfilRepository perfilRepository,
                             PerfilCategoriaRepository perfilCategoriaRepository,
                             CategoriaRepository categoriaRepository, SolicitudRepository solicitudRepository,
                             ReputacionService reputacionService, CalificacionService calificacionService) {
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.perfilCategoriaRepository = perfilCategoriaRepository;
        this.categoriaRepository = categoriaRepository;
        this.solicitudRepository = solicitudRepository;
        this.reputacionService = reputacionService;
        this.calificacionService = calificacionService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> categoriasDe(Long idUsuario) {
        return perfilRepository.findByUsuarioId(idUsuario)
                .map(perfil -> perfilCategoriaRepository.findByPerfilId(perfil.getId()).stream()
                        .map(PerfilCategoria::getIdCategoria)
                        .toList())
                .orElse(List.of());
    }

    @Override
    @Transactional
    public void guardarCategorias(Long idUsuario, List<Long> idsCategoria) {
        Perfil perfil = perfilDe(idUsuario);
        perfilCategoriaRepository.deleteByPerfilId(perfil.getId());
        // sin el flush, JPA inserta antes de borrar y revienta el unique de (perfil, categoría)
        perfilCategoriaRepository.flush();
        idsCategoria.stream()
                .distinct()
                .map(categoriaRepository::getReferenceById)
                .map(perfil::declararCategoria)
                .forEach(perfilCategoriaRepository::save);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean ofreceServicios(Long idUsuario) {
        return perfilCategoriaRepository.countByPerfilUsuarioId(idUsuario) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public PerfilResumen miPerfil(Long idUsuario) {
        Usuario u = usuario(idUsuario);
        Optional<Perfil> perfil = perfilRepository.findByUsuarioId(idUsuario);
        return new PerfilResumen(perfil.map(Perfil::getDescripcion).orElse(null),
                perfil.map(Perfil::getExperiencia).orElse(null), u.getDistrito(), u.getTelefono(),
                Tiempo.mesYAnio(u.getFechaRegistro()), reputacionService.de(idUsuario),
                solicitudRepository.countByAutorId(idUsuario),
                calificacionService.recibidas(idUsuario, PageRequest.of(0, RESENAS)).getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public PerfilForm formularioDe(Long idUsuario) {
        Usuario u = usuario(idUsuario);
        Optional<Perfil> perfil = perfilRepository.findByUsuarioId(idUsuario);
        PerfilForm form = new PerfilForm();
        form.setNombre(u.getNombre());
        form.setApellidos(u.getApellidos());
        form.setTelefono(u.getTelefono());
        form.setDistrito(u.getDistrito());
        form.setDescripcion(perfil.map(Perfil::getDescripcion).orElse(null));
        form.setExperiencia(perfil.map(Perfil::getExperiencia).orElse(null));
        return form;
    }

    @Override
    @Transactional
    public void actualizar(Long idUsuario, PerfilForm form) {
        Usuario u = usuario(idUsuario);
        u.setNombre(form.getNombre().trim());
        u.setApellidos(form.getApellidos().trim());
        u.setTelefono(limpio(form.getTelefono()));
        String distrito = limpio(form.getDistrito());
        // las coordenadas eran del distrito anterior: si cambia, ya no sirven
        if (distrito == null || !distrito.equals(u.getDistrito())) {
            u.setLatitud(null);
            u.setLongitud(null);
        }
        u.setDistrito(distrito);

        String descripcion = limpio(form.getDescripcion());
        String experiencia = limpio(form.getExperiencia());
        Optional<Perfil> perfil = perfilRepository.findByUsuarioId(idUsuario);
        if (perfil.isPresent() || descripcion != null || experiencia != null) {
            Perfil p = perfil.orElseGet(() -> perfilRepository.save(new Perfil(u)));
            p.setDescripcion(descripcion);
            p.setExperiencia(experiencia);
        }
    }

    // el perfil nace cuando alguien dice por primera vez que sabe hacer algo
    private Perfil perfilDe(Long idUsuario) {
        return perfilRepository.findByUsuarioId(idUsuario)
                .orElseGet(() -> perfilRepository.save(new Perfil(usuario(idUsuario))));
    }

    private Usuario usuario(Long idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("persona", idUsuario));
    }

    private static String limpio(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
