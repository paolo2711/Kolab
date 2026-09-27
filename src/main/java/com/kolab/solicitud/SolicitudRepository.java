package com.kolab.solicitud;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a {@link Solicitud}. Es la capa DAO del catálogo de solicitudes.
 */
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    Page<Solicitud> findByEstadoOrderByFechaPublicacionDesc(EstadoSolicitud estado, Pageable pagina);

    Page<Solicitud> findByAutorIdOrderByFechaPublicacionDesc(Long idAutor, Pageable pagina);

    Page<Solicitud> findByCategoriaIdAndEstadoOrderByFechaPublicacionDesc(Long idCategoria,
                                                                         EstadoSolicitud estado,
                                                                         Pageable pagina);

    // las propuestas dirigidas a una persona son pocas y se muestran juntas
    List<Solicitud> findByDestinatarioIdAndEstado(Long idDestinatario, EstadoSolicitud estado);

    long countByCategoriaIdAndEstado(Long idCategoria, EstadoSolicitud estado);
}
