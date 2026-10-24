package com.kolab.solicitud;

import jakarta.persistence.criteria.JoinType;
import java.util.Collection;
import org.springframework.data.jpa.domain.Specification;

/**
 * Las condiciones con las que se busca en el catálogo. Cada una es una pieza que se combina con
 * las demás, así un filtro nuevo es un método más y no otra consulta.
 */
final class SolicitudesVisibles {

    private SolicitudesVisibles() {
    }

    /**
     * Las abiertas que esta persona puede ofertar: no son suyas, y si son una propuesta directa,
     * van dirigidas a ella.
     */
    static Specification<Solicitud> paraOfertar(Long idUsuario) {
        return (s, consulta, cb) -> cb.and(
                cb.equal(s.get("estado"), EstadoSolicitud.ABIERTA),
                cb.notEqual(s.get("autor").get("id"), idUsuario),
                cb.or(cb.isNull(s.get("destinatario")),
                        cb.equal(s.join("destinatario", JoinType.LEFT).get("id"), idUsuario)));
    }

    // lo que se muestra a quien todavía no entró
    static Specification<Solicitud> publicas() {
        return (s, consulta, cb) -> cb.and(
                cb.equal(s.get("estado"), EstadoSolicitud.ABIERTA),
                cb.isNull(s.get("destinatario")));
    }

    static Specification<Solicitud> segun(FiltroSolicitudes filtro, Collection<Long> misCategorias) {
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

    static Specification<Solicitud> deCategoria(Long idCategoria) {
        return (s, q, cb) -> cb.equal(s.get("categoria").get("id"), idCategoria);
    }

    static Specification<Solicitud> enCategorias(Collection<Long> idsCategoria) {
        if (idsCategoria.isEmpty()) {
            return (s, q, cb) -> cb.disjunction();
        }
        return (s, q, cb) -> s.get("categoria").get("id").in(idsCategoria);
    }

    static Specification<Solicitud> presencialesConUbicacion() {
        return (s, q, cb) -> cb.and(
                cb.equal(s.get("modalidad"), Modalidad.PRESENCIAL),
                cb.isNotNull(s.get("latitud")),
                cb.isNotNull(s.get("longitud")));
    }
}
