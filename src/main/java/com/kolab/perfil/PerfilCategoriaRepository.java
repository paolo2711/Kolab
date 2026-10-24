package com.kolab.perfil;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a {@link PerfilCategoria}: lo que cada persona declaró saber hacer.
 */
public interface PerfilCategoriaRepository extends JpaRepository<PerfilCategoria, Long> {

    List<PerfilCategoria> findByPerfilId(Long idPerfil);

    // las categorías de varias personas a la vez, para armar una lista de tarjetas sin N+1
    @EntityGraph(attributePaths = {"perfil", "categoria"})
    List<PerfilCategoria> findByPerfilIdIn(Collection<Long> idsPerfil);

    long countByPerfilUsuarioId(Long idUsuario);

    void deleteByPerfilId(Long idPerfil);
}
