package com.kolab.perfil;

import com.kolab.usuario.Usuario;
import com.kolab.usuario.UsuarioRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PerfilServiceImpl implements PerfilService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilExpertoRepository perfilExpertoRepository;
    private final PerfilCategoriaRepository perfilCategoriaRepository;

    public PerfilServiceImpl(UsuarioRepository usuarioRepository,
                             PerfilExpertoRepository perfilExpertoRepository,
                             PerfilCategoriaRepository perfilCategoriaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.perfilExpertoRepository = perfilExpertoRepository;
        this.perfilCategoriaRepository = perfilCategoriaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> categoriasDe(Long idUsuario) {
        return perfilExpertoRepository.findByUsuarioId(idUsuario)
                .map(perfil -> perfilCategoriaRepository.findByPerfilId(perfil.getId()).stream()
                        .map(PerfilCategoria::getIdCategoria)
                        .toList())
                .orElse(List.of());
    }

    @Override
    @Transactional
    public void guardarCategorias(Long idUsuario, List<Long> idsCategoria) {
        PerfilExperto perfil = perfilDe(idUsuario);
        perfilCategoriaRepository.deleteByPerfilId(perfil.getId());
        // sin el flush, JPA inserta antes de borrar y revienta el unique de (perfil, categoría)
        perfilCategoriaRepository.flush();
        idsCategoria.stream()
                .distinct()
                .map(id -> new PerfilCategoria(perfil, id))
                .forEach(perfilCategoriaRepository::save);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean ofreceServicios(Long idUsuario) {
        return perfilCategoriaRepository.countByPerfilUsuarioId(idUsuario) > 0;
    }

    // el perfil de experto nace cuando alguien dice por primera vez que sabe hacer algo
    private PerfilExperto perfilDe(Long idUsuario) {
        return perfilExpertoRepository.findByUsuarioId(idUsuario)
                .orElseGet(() -> {
                    Usuario usuario = usuarioRepository.findById(idUsuario)
                            .orElseThrow(() -> new IllegalArgumentException("No hay usuario " + idUsuario));
                    return perfilExpertoRepository.save(new PerfilExperto(usuario));
                });
    }
}
