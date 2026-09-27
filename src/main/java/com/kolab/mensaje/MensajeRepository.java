package com.kolab.mensaje;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a {@link Mensaje}. Es la capa DAO de la mensajería.
 */
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    // una conversación larga se carga por tramos, igual que cualquier chat
    Page<Mensaje> findByOfertaIdOrderByFechaEnvioDesc(Long idOferta, Pageable pagina);

    long countByOfertaIdAndLeidoFalseAndEmisorIdNot(Long idOferta, Long idUsuario);
}
