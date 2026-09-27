package com.kolab.servicio;

import com.kolab.oferta.Oferta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * El trato ya cerrado. Nace cuando se acepta una oferta y desde ahí el camino es el mismo para las
 * dos partes: coordinan por mensajería, se ejecuta y se califican.
 *
 * <p>Guarda {@code montoFinal} y {@code comision} copiados del momento del acuerdo. No se calculan
 * al consultar a propósito: si mañana cambia el porcentaje de la plataforma, los servicios viejos
 * tienen que seguir diciendo lo que se cobró de verdad.
 */
@Entity
@Table(name = "servicio")
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servicio")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_oferta", nullable = false, unique = true)
    private Oferta oferta;

    @Column(name = "monto_final", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoFinal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal comision;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio = LocalDateTime.now();

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoServicio estado = EstadoServicio.EN_CURSO;

    protected Servicio() {
    }

    public Servicio(Oferta oferta, BigDecimal montoFinal, BigDecimal comision) {
        this.oferta = oferta;
        this.montoFinal = montoFinal;
        this.comision = comision;
    }

    /**
     * Da el servicio por terminado y sella la fecha de cierre. A partir de aquí las dos partes
     * pueden calificarse.
     *
     * @throws IllegalStateException si ya estaba cerrado
     */
    public void cerrar() {
        if (estaCerrado()) {
            throw new IllegalStateException("El servicio " + id + " ya está cerrado");
        }
        this.estado = EstadoServicio.CERRADO;
        this.fechaCierre = LocalDateTime.now();
    }

    public boolean estaCerrado() {
        return estado == EstadoServicio.CERRADO;
    }

    /**
     * Lo que le queda a quien hizo el trabajo, ya descontada la comisión de la plataforma.
     */
    public BigDecimal getNeto() {
        return montoFinal.subtract(comision);
    }

    public Long getId() {
        return id;
    }

    public Oferta getOferta() {
        return oferta;
    }

    public BigDecimal getMontoFinal() {
        return montoFinal;
    }

    public BigDecimal getComision() {
        return comision;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public EstadoServicio getEstado() {
        return estado;
    }
}
