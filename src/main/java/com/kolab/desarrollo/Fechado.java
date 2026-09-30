package com.kolab.desarrollo;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Lleva al pasado las fechas de los ejemplos. Los servicios fechan todo con la hora actual, que es
 * lo correcto en uso real; aquí se corrige al final para que los ejemplos parezcan de semanas atrás.
 *
 * <p>Los cambios se juntan y se aplican de una vez al terminar la carga: si se aplicaran en el
 * momento, el siguiente guardado de la misma fila los pisaría con la fecha de la entidad.
 */
@Component
class Fechado {

    private final EntityManager em;

    Fechado(EntityManager em) {
        this.em = em;
    }

    record Cambio(String sql, LocalDateTime fecha, Object id) {
    }

    static Cambio solicitud(Long id, int haceHoras) {
        return new Cambio("update solicitud set fecha_publicacion = ?1 where id_solicitud = ?2", horas(haceHoras), id);
    }

    static Cambio oferta(Long id, int haceHoras) {
        return new Cambio("update oferta set fecha_oferta = ?1 where id_oferta = ?2", horas(haceHoras), id);
    }

    static Cambio inicio(Long idServicio, int haceHoras) {
        return new Cambio("update servicio set fecha_inicio = ?1 where id_servicio = ?2", horas(haceHoras), idServicio);
    }

    static Cambio cierre(Long idServicio, int haceHoras) {
        return new Cambio("update servicio set fecha_cierre = ?1 where id_servicio = ?2", horas(haceHoras), idServicio);
    }

    static Cambio calificaciones(Long idServicio, int haceHoras) {
        return new Cambio("update calificacion set fecha = ?1 where id_servicio = ?2", horas(haceHoras), idServicio);
    }

    static Cambio mensaje(Long id, int haceMinutos) {
        return new Cambio("update mensaje set fecha_envio = ?1 where id_mensaje = ?2",
                LocalDateTime.now().minusMinutes(haceMinutos), id);
    }

    static Cambio registro(Long idUsuario, int meses) {
        return new Cambio("update usuario set fecha_registro = ?1 where id_usuario = ?2",
                LocalDateTime.now().minusMonths(meses), idUsuario);
    }

    void aplicar(List<Cambio> cambios, List<Long> mensajesLeidos) {
        em.flush();
        for (Cambio c : cambios) {
            em.createNativeQuery(c.sql()).setParameter(1, c.fecha()).setParameter(2, c.id()).executeUpdate();
        }
        if (!mensajesLeidos.isEmpty()) {
            em.createNativeQuery("update mensaje set leido = true where id_mensaje in (?1)")
                    .setParameter(1, mensajesLeidos)
                    .executeUpdate();
        }
        em.clear();
    }

    private static LocalDateTime horas(int haceHoras) {
        return LocalDateTime.now().minusHours(haceHoras);
    }
}
