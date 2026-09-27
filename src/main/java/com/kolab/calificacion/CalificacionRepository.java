package com.kolab.calificacion;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a {@link Calificacion}. Es la capa DAO de la reputación.
 */
public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {

    Page<Calificacion> findByEvaluadoIdOrderByFechaDesc(Long idEvaluado, Pageable pagina);

    Optional<Calificacion> findByServicioIdAndEvaluadorId(Long idServicio, Long idEvaluador);

    boolean existsByServicioIdAndEvaluadorId(Long idServicio, Long idEvaluador);
}
