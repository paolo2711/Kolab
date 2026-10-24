package com.kolab.mensaje;

import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Acceso a {@link Mensaje}. Es la capa DAO de las conversaciones.
 */
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    // una conversación larga se carga por tramos, igual que cualquier chat
    Page<Mensaje> findByOfertaIdOrderByFechaEnvioDesc(Long idOferta, Pageable pagina);

    // los hilos de las ofertas de una misma solicitud, que son pocas
    List<Mensaje> findByOfertaIdInOrderByFechaEnvio(Collection<Long> idsOferta);

    // el último mensaje de cada conversación en la que participa la persona, la más reciente primero
    @EntityGraph(attributePaths = {"oferta", "oferta.solicitud", "oferta.solicitud.autor", "oferta.usuario"})
    @Query(value = """
            select m from Mensaje m
            where m.id in (select max(m2.id) from Mensaje m2 group by m2.oferta.id)
              and (m.oferta.usuario.id = :idUsuario or m.oferta.solicitud.autor.id = :idUsuario)
            order by m.fechaEnvio desc""",
            countQuery = """
            select count(m) from Mensaje m
            where m.id in (select max(m2.id) from Mensaje m2 group by m2.oferta.id)
              and (m.oferta.usuario.id = :idUsuario or m.oferta.solicitud.autor.id = :idUsuario)""")
    Page<Mensaje> ultimosDeCadaConversacion(@Param("idUsuario") Long idUsuario, Pageable pagina);

    @Query("""
            select count(m) from Mensaje m
            where m.leido = false and m.emisor.id <> :idUsuario
              and (m.oferta.usuario.id = :idUsuario or m.oferta.solicitud.autor.id = :idUsuario)""")
    long contarSinLeer(@Param("idUsuario") Long idUsuario);

    @Query("""
            select m.oferta.id, count(m) from Mensaje m
            where m.oferta.id in :ids and m.leido = false and m.emisor.id <> :idUsuario
            group by m.oferta.id""")
    List<Object[]> contarSinLeerPorOferta(@Param("ids") Collection<Long> idsOferta,
                                         @Param("idUsuario") Long idUsuario);

    @Modifying
    @Query("""
            update Mensaje m set m.leido = true
            where m.oferta.id = :idOferta and m.emisor.id <> :idUsuario and m.leido = false""")
    int marcarLeidos(@Param("idOferta") Long idOferta, @Param("idUsuario") Long idUsuario);
}
