package com.kolab.calificacion;

import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Acceso a {@link Calificacion}. Es la capa DAO de la reputación.
 */
public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {

    @EntityGraph(attributePaths = {"evaluador", "servicio", "servicio.oferta", "servicio.oferta.solicitud"})
    Page<Calificacion> findByEvaluadoIdOrderByFechaDesc(Long idEvaluado, Pageable pagina);

    boolean existsByServicioIdAndEvaluadorId(Long idServicio, Long idEvaluador);

    @Query("""
            select c.evaluado.id, c.comentario from Calificacion c
            where c.id in (select max(c2.id) from Calificacion c2
                           where c2.evaluado.id in :ids and c2.comentario is not null
                           group by c2.evaluado.id)""")
    List<Object[]> ultimosComentarios(@Param("ids") Collection<Long> idsUsuario);

    @Query("select coalesce(avg(c.puntaje), 0) from Calificacion c")
    Double promedioGeneral();

    @Query("select c.puntaje, count(c) from Calificacion c where c.evaluado.id = :idUsuario group by c.puntaje")
    List<Object[]> contarPorPuntaje(@Param("idUsuario") Long idUsuario);
}
