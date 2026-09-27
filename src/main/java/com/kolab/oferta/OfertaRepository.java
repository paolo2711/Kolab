package com.kolab.oferta;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a {@link Oferta}. Es la capa DAO de las ofertas.
 */
public interface OfertaRepository extends JpaRepository<Oferta, Long> {

    Page<Oferta> findByUsuarioIdOrderByFechaOfertaDesc(Long idUsuario, Pageable pagina);

    // las ofertas de una solicitud se muestran todas juntas para poder compararlas
    List<Oferta> findBySolicitudIdOrderByMontoPropuesto(Long idSolicitud);

    Optional<Oferta> findBySolicitudIdAndUsuarioId(Long idSolicitud, Long idUsuario);

    long countBySolicitudId(Long idSolicitud);
}
