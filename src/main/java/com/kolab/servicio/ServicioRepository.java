package com.kolab.servicio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
 * Acceso a {@link Servicio}. Es la capa DAO de los tratos cerrados.
 */
public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    Optional<Servicio> findByOfertaId(Long idOferta);

    @EntityGraph(attributePaths = {"oferta", "oferta.solicitud", "oferta.solicitud.categoria",
            "oferta.solicitud.autor", "oferta.usuario"})
    Optional<Servicio> findConPartesById(Long id);

    @EntityGraph(attributePaths = {"oferta", "oferta.solicitud", "oferta.usuario"})
    Page<Servicio> findByOfertaSolicitudAutorIdOrderByFechaInicioDesc(Long idAutor, Pageable pagina);

    @EntityGraph(attributePaths = {"oferta", "oferta.solicitud", "oferta.solicitud.autor"})
    Page<Servicio> findByOfertaUsuarioIdOrderByFechaInicioDesc(Long idUsuario, Pageable pagina);

    @Query("""
            select o.usuario.id, count(s) from Servicio s join s.oferta o
            where s.estado = com.kolab.servicio.EstadoServicio.CERRADO and o.usuario.id in :ids
            group by o.usuario.id""")
    List<Object[]> contarCerradosPorQuienOfrece(@Param("ids") Collection<Long> idsUsuario);

    @Query("select s.oferta.id, s.id from Servicio s where s.oferta.id in :ids")
    List<Object[]> idsPorOferta(@Param("ids") Collection<Long> idsOferta);

    long countByEstado(EstadoServicio estado);

    long countByOfertaSolicitudAutorIdAndEstado(Long idAutor, EstadoServicio estado);

    long countByOfertaUsuarioIdAndEstado(Long idUsuario, EstadoServicio estado);

    @Query("""
            select coalesce(sum(s.montoFinal), 0) from Servicio s
            where s.oferta.solicitud.autor.id = :idUsuario and s.estado = com.kolab.servicio.EstadoServicio.CERRADO""")
    BigDecimal totalPagado(@Param("idUsuario") Long idUsuario);

    @Query("""
            select coalesce(sum(s.montoFinal - s.comision), 0) from Servicio s
            where s.oferta.usuario.id = :idUsuario and s.estado = com.kolab.servicio.EstadoServicio.CERRADO""")
    BigDecimal totalGanado(@Param("idUsuario") Long idUsuario);

    // cerrados en los que la persona fue parte y todavía no calificó a la otra
    @Query("""
            select count(s) from Servicio s
            where s.estado = com.kolab.servicio.EstadoServicio.CERRADO
              and (s.oferta.usuario.id = :idUsuario or s.oferta.solicitud.autor.id = :idUsuario)
              and not exists (select 1 from Calificacion c
                              where c.servicio = s and c.evaluador.id = :idUsuario)""")
    long porCalificar(@Param("idUsuario") Long idUsuario);

    // lo cerrado desde una fecha, para armar las barras por mes; son pocos meses y pocas filas
    @Query("""
            select s from Servicio s
            where s.oferta.usuario.id = :idUsuario and s.estado = com.kolab.servicio.EstadoServicio.CERRADO
              and s.fechaCierre >= :desde""")
    List<Servicio> cerradosQueDiDesde(@Param("idUsuario") Long idUsuario, @Param("desde") LocalDateTime desde);

    @Query("""
            select s.oferta.solicitud.categoria.nombre, sum(s.montoFinal - s.comision) from Servicio s
            where s.oferta.usuario.id = :idUsuario and s.estado = com.kolab.servicio.EstadoServicio.CERRADO
            group by s.oferta.solicitud.categoria.nombre""")
    List<Object[]> ganadoPorCategoria(@Param("idUsuario") Long idUsuario);

    @Query("""
            select s.oferta.solicitud.categoria.nombre, sum(s.montoFinal) from Servicio s
            where s.oferta.solicitud.autor.id = :idUsuario and s.estado = com.kolab.servicio.EstadoServicio.CERRADO
            group by s.oferta.solicitud.categoria.nombre""")
    List<Object[]> pagadoPorCategoria(@Param("idUsuario") Long idUsuario);

    // cuántos servicios le dio la persona a cada cliente: sirve para saber quién volvió
    @Query("""
            select count(s) from Servicio s where s.oferta.usuario.id = :idUsuario
            group by s.oferta.solicitud.autor.id""")
    List<Long> serviciosPorCliente(@Param("idUsuario") Long idUsuario);

    long countByOfertaSolicitudAutorId(Long idAutor);

    // cuánto se cerró por debajo o por encima del precio que propuso quien pidió, en promedio
    @Query("""
            select avg(s.montoFinal - s.oferta.solicitud.precioPropuesto) from Servicio s
            where s.oferta.solicitud.autor.id = :idUsuario""")
    Double diferenciaPromedioConLoPropuesto(@Param("idUsuario") Long idUsuario);
}
