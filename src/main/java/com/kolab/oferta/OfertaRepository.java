package com.kolab.oferta;

import com.kolab.solicitud.EstadoSolicitud;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Acceso a {@link Oferta}. Es la capa DAO de las respuestas a una solicitud.
 */
public interface OfertaRepository extends JpaRepository<Oferta, Long> {

    @EntityGraph(attributePaths = {"solicitud", "solicitud.categoria"})
    Page<Oferta> findByUsuarioIdOrderByFechaOfertaDesc(Long idUsuario, Pageable pagina);

    long countByUsuarioId(Long idUsuario);

    long countByUsuarioIdAndEstado(Long idUsuario, EstadoOferta estado);

    // las ofertas de una solicitud se muestran todas juntas para poder compararlas
    @EntityGraph(attributePaths = "usuario")
    List<Oferta> findBySolicitudIdOrderByMontoPropuesto(Long idSolicitud);

    // las que siguen en juego en varias solicitudes a la vez, para armar una lista sin N+1
    @EntityGraph(attributePaths = "usuario")
    @Query("""
            select o from Oferta o
            where o.solicitud.id in :ids and o.estado <> com.kolab.oferta.EstadoOferta.RECHAZADA
            order by o.montoPropuesto""")
    List<Oferta> vigentesEn(@Param("ids") Collection<Long> idsSolicitud);

    Optional<Oferta> findBySolicitudIdAndUsuarioId(Long idSolicitud, Long idUsuario);

    @EntityGraph(attributePaths = {"solicitud", "solicitud.autor", "solicitud.categoria", "usuario"})
    Optional<Oferta> findConPartesById(Long id);

    @Query("""
            select o.solicitud.id, count(o) from Oferta o
            where o.solicitud.id in :ids and o.estado <> com.kolab.oferta.EstadoOferta.RECHAZADA
            group by o.solicitud.id""")
    List<Object[]> contarVigentesPorSolicitud(@Param("ids") Collection<Long> idsSolicitud);

    long countBySolicitudAutorIdAndEstadoAndSolicitudEstado(Long idAutor, EstadoOferta estado,
                                                              EstadoSolicitud estadoSolicitud);

    // cuánto piden de más o de menos, en promedio, frente al precio que propuso quien publicó
    @Query("""
            select avg(o.montoPropuesto - o.solicitud.precioPropuesto) from Oferta o
            where o.usuario.id = :idUsuario""")
    Double diferenciaPromedioConLoPedido(@Param("idUsuario") Long idUsuario);

    // cuándo se publicó cada solicitud de la persona y cuándo le llegó la primera oferta
    @Query("""
            select o.solicitud.fechaPublicacion, min(o.fechaOferta) from Oferta o
            where o.solicitud.autor.id = :idAutor
            group by o.solicitud.id, o.solicitud.fechaPublicacion""")
    List<Object[]> primerasOfertas(@Param("idAutor") Long idAutor);
}
