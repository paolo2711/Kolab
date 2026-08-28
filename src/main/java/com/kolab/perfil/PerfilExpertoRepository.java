package com.kolab.perfil;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfilExpertoRepository extends JpaRepository<PerfilExperto, Long> {

    Optional<PerfilExperto> findByUsuarioId(Long idUsuario);
}
