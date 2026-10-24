package com.kolab.perfil;

import java.math.BigDecimal;

/**
 * La tarjeta de alguien que ofrece: quién es, en qué trabaja, su reputación y lo último que le
 * dijeron. {@code ultimaResena} es {@code null} si nadie dejó comentario.
 */
public record ExpertoResumen(Persona persona, String distrito, String enQueTrabaja,
                             Reputacion reputacion, String ultimaResena) {

    public BigDecimal getCalificacion() {
        return reputacion.promedio();
    }

    public long getServicios() {
        return reputacion.serviciosDados();
    }

    public boolean sinCalificar() {
        return reputacion.sinCalificar();
    }
}
