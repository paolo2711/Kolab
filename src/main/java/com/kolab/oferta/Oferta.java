package com.kolab.oferta;

import com.kolab.servicio.Servicio;
import com.kolab.solicitud.Solicitud;
import com.kolab.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * La respuesta de quien sabe hacer el trabajo: acepta el precio que pidió el cliente o propone
 * otro monto. Una persona hace una sola oferta por solicitud, lo garantiza la restricción
 * {@code uk_oferta_solicitud_usuario}.
 */
@Entity
@Table(name = "oferta")
public class Oferta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_oferta")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_solicitud", nullable = false)
    private Solicitud solicitud;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "monto_propuesto", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoPropuesto;

    @Column(length = 500)
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoOferta estado = EstadoOferta.ENVIADA;

    @Column(name = "fecha_oferta", nullable = false)
    private LocalDateTime fechaOferta = LocalDateTime.now();

    protected Oferta() {
    }

    public Oferta(Solicitud solicitud, Usuario usuario, BigDecimal montoPropuesto, String mensaje) {
        this.solicitud = solicitud;
        this.usuario = usuario;
        this.montoPropuesto = montoPropuesto;
        this.mensaje = mensaje;
    }

    /**
     * Si quien ofertó tomó el precio tal como lo puso el cliente, sin regatear.
     */
    public boolean aceptaElPrecioDelCliente() {
        return montoPropuesto.compareTo(solicitud.getPrecioPropuesto()) == 0;
    }

    /**
     * Cierra el trato con esta oferta: la marca aceptada, pasa la solicitud a EN_CURSO y crea el
     * servicio con la comisión calculada sobre el monto acordado.
     *
     * @param porcentajeComision lo que cobra la plataforma, por ejemplo 5 para el 5 %
     * @return el servicio recién creado, que es lo que ambas partes van a seguir desde ahí
     * @throws IllegalStateException si la oferta ya fue aceptada o rechazada
     */
    public Servicio aceptar(BigDecimal porcentajeComision) {
        if (estado != EstadoOferta.ENVIADA) {
            throw new IllegalStateException("La oferta " + id + " ya fue " + estado);
        }
        this.estado = EstadoOferta.ACEPTADA;
        solicitud.aceptarTrato();
        return new Servicio(this, montoPropuesto, comisionDe(porcentajeComision));
    }

    /**
     * Descarta la oferta. La solicitud sigue abierta para el resto.
     */
    public void rechazar() {
        this.estado = EstadoOferta.RECHAZADA;
    }

    private BigDecimal comisionDe(BigDecimal porcentaje) {
        return montoPropuesto.multiply(porcentaje)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public Long getId() {
        return id;
    }

    public Solicitud getSolicitud() {
        return solicitud;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public BigDecimal getMontoPropuesto() {
        return montoPropuesto;
    }

    public void setMontoPropuesto(BigDecimal montoPropuesto) {
        this.montoPropuesto = montoPropuesto;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public EstadoOferta getEstado() {
        return estado;
    }

    public LocalDateTime getFechaOferta() {
        return fechaOferta;
    }
}
