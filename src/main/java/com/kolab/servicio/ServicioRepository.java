package com.kolab.servicio;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a {@link Servicio}. Es la capa DAO de los tratos cerrados.
 */
public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    Optional<Servicio> findByOfertaId(Long idOferta);

    Page<Servicio> findByOfertaSolicitudAutorIdOrderByFechaInicioDesc(Long idAutor, Pageable pagina);

    Page<Servicio> findByOfertaUsuarioIdOrderByFechaInicioDesc(Long idUsuario, Pageable pagina);
}
