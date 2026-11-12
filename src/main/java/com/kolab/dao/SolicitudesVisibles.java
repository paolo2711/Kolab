package com.kolab.dao;

import com.kolab.modelo.dto.FiltroSolicitudes;
import com.kolab.modelo.entidad.EstadoSolicitud;
import com.kolab.modelo.entidad.Modalidad;
import com.kolab.modelo.entidad.Oferta;
import com.kolab.modelo.entidad.Solicitud;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.Collection;
import org.springframework.data.jpa.domain.Specification;

/**
 * Las condiciones con las que se busca en el catálogo. Cada una es una pieza que se combina con
 * las demás, así un filtro nuevo es un método más y no otra consulta.
 */
public final class SolicitudesVisibles {

    private SolicitudesVisibles() {
    }

    /**
     * Las abiertas que esta persona puede ofertar: no son suyas, y si son una propuesta directa,
     * van dirigidas a ella.
     */
    public static Specification<Solicitud> paraOfertar(Long idUsuario) {
        return (s, consulta, cb) -> cb.and(
                cb.equal(s.get("estado"), EstadoSolicitud.ABIERTA),
                cb.notEqual(s.get("autor").get("id"), idUsuario),
                cb.or(cb.isNull(s.get("destinatario")),
                        cb.equal(s.join("destinatario", JoinType.LEFT).get("id"), idUsuario)));
    }

    /**
     * Las propuestas directas abiertas que le hicieron a esta persona y que todavía no respondió con
     * una oferta. Las que ya ofertó siguen en Lo que ofrezco.
     */
    public static Specification<Solicitud> propuestasSinResponderA(Long idUsuario) {
        return (s, consulta, cb) -> {
            Subquery<Long> suyas = consulta.subquery(Long.class);
            Root<Oferta> oferta = suyas.from(Oferta.class);
            suyas.select(oferta.get("id")).where(
                    cb.equal(oferta.get("solicitud"), s),
                    cb.equal(oferta.get("usuario").get("id"), idUsuario));
            return cb.and(
                    cb.equal(s.get("estado"), EstadoSolicitud.ABIERTA),
                    cb.equal(s.get("destinatario").get("id"), idUsuario),
                    cb.not(cb.exists(suyas)));
        };
    }

    // lo que se muestra a quien todavía no entró
    public static Specification<Solicitud> publicas() {
        return (s, consulta, cb) -> cb.and(
                cb.equal(s.get("estado"), EstadoSolicitud.ABIERTA),
                cb.isNull(s.get("destinatario")));
    }

    public static Specification<Solicitud> segun(FiltroSolicitudes filtro, Collection<Long> misCategorias) {
        Specification<Solicitud> spec = (s, q, cb) -> cb.conjunction();
        if (filtro.getIdCategoria() != null) {
            spec = spec.and(deCategoria(filtro.getIdCategoria()));
        }
        if (filtro.getModalidad() != null) {
            spec = spec.and((s, q, cb) -> cb.equal(s.get("modalidad"), filtro.getModalidad()));
        }
        if (filtro.getPrecioMinimo() != null) {
            spec = spec.and((s, q, cb) -> cb.ge(s.get("precioPropuesto"), filtro.getPrecioMinimo()));
        }
        if (filtro.getPrecioMaximo() != null) {
            spec = spec.and((s, q, cb) -> cb.le(s.get("precioPropuesto"), filtro.getPrecioMaximo()));
        }
        if (filtro.getDistrito() != null && !filtro.getDistrito().isBlank()) {
            spec = spec.and((s, q, cb) -> cb.equal(s.get("distrito"), filtro.getDistrito()));
        }
        if (filtro.isSoloLoQueSe()) {
            spec = spec.and(enCategorias(misCategorias));
        }
        return spec;
    }

    public static Specification<Solicitud> deCategoria(Long idCategoria) {
        return (s, q, cb) -> cb.equal(s.get("categoria").get("id"), idCategoria);
    }

    public static Specification<Solicitud> enCategorias(Collection<Long> idsCategoria) {
        if (idsCategoria.isEmpty()) {
            return (s, q, cb) -> cb.disjunction();
        }
        return (s, q, cb) -> s.get("categoria").get("id").in(idsCategoria);
    }

    public static Specification<Solicitud> presencialesConUbicacion() {
        return (s, q, cb) -> cb.and(
                cb.equal(s.get("modalidad"), Modalidad.PRESENCIAL),
                cb.isNotNull(s.get("latitud")),
                cb.isNotNull(s.get("longitud")));
    }
}
