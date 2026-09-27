package com.kolab.perfil;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a {@link Perfil}. Es la capa DAO del perfil y de la reputación.
 */
public interface PerfilRepository extends JpaRepository<Perfil, Long> {

    Optional<Perfil> findByUsuarioId(Long idUsuario);
}
