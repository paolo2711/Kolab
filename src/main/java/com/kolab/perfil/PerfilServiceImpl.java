package com.kolab.perfil;

import com.kolab.categoria.CategoriaRepository;
import com.kolab.usuario.Usuario;
import com.kolab.usuario.UsuarioRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de {@link PerfilService}.
 */
@Service
public class PerfilServiceImpl implements PerfilService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PerfilCategoriaRepository perfilCategoriaRepository;
    private final CategoriaRepository categoriaRepository;

    public PerfilServiceImpl(UsuarioRepository usuarioRepository,
                             PerfilRepository perfilRepository,
                             PerfilCategoriaRepository perfilCategoriaRepository,
                             CategoriaRepository categoriaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.perfilCategoriaRepository = perfilCategoriaRepository;
        this.categoriaRepository = categoriaRepository;
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

    // el perfil nace cuando alguien dice por primera vez que sabe hacer algo
    private Perfil perfilDe(Long idUsuario) {
        return perfilRepository.findByUsuarioId(idUsuario)
                .orElseGet(() -> {
                    Usuario usuario = usuarioRepository.findById(idUsuario)
                            .orElseThrow(() -> new IllegalArgumentException("No hay usuario " + idUsuario));
                    return perfilRepository.save(new Perfil(usuario));
                });
    }
}
