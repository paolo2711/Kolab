package com.kolab.solicitud;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * Acceso a {@link Solicitud}. Es la capa DAO de lo que la gente publica.
 */
public interface SolicitudRepository extends JpaRepository<Solicitud, Long>,
        JpaSpecificationExecutor<Solicitud> {

    // los filtros de Explorar se arman con Specification y llegan aquí ya paginados
    @Override
    @EntityGraph(attributePaths = "categoria")
    Page<Solicitud> findAll(Specification<Solicitud> filtro, Pageable pagina);

    @EntityGraph(attributePaths = {"categoria", "autor", "destinatario"})
    Optional<Solicitud> findConPartesById(Long id);

    @EntityGraph(attributePaths = "categoria")
    Page<Solicitud> findByAutorIdOrderByFechaPublicacionDesc(Long idAutor, Pageable pagina);

    long countByAutorId(Long idAutor);

    long countByAutorIdAndEstado(Long idAutor, EstadoSolicitud estado);

    // cuenta solo las que ve todo el mundo: una propuesta directa no es demanda de la categoría
    @Query("""
            select s.categoria.id, count(s) from Solicitud s
            where s.estado = com.kolab.solicitud.EstadoSolicitud.ABIERTA and s.destinatario is null
            group by s.categoria.id""")
    List<Object[]> contarAbiertasPorCategoria();

    @Query("""
            select distinct s.distrito from Solicitud s
            where s.estado = com.kolab.solicitud.EstadoSolicitud.ABIERTA
              and s.destinatario is null and s.distrito is not null
            order by s.distrito""")
    List<String> distritosConAbiertas();
}
