package com.kolab.perfil;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a {@link PerfilCategoria}. Es la capa DAO de lo que cada persona declaró saber hacer.
 */
/**
 * Acceso a {@link PerfilCategoria}. Es la capa DAO de lo que cada persona declaró saber hacer.
 */
public interface PerfilCategoriaRepository extends JpaRepository<PerfilCategoria, Long> {

    List<PerfilCategoria> findByPerfilId(Long idPerfil);

    long countByPerfilUsuarioId(Long idUsuario);

    void deleteByPerfilId(Long idPerfil);
}
