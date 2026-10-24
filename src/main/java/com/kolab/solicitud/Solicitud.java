package com.kolab.solicitud;

import com.kolab.categoria.Categoria;
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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Lo que alguien necesita y cuánto propone pagar. Es la pieza con la que arranca todo el flujo:
 * quien la publica fija el precio y quien sabe hacerlo decide si lo toma.
 *
 * <p>Si {@code destinatario} viene vacío la ve todo el mundo. Si trae a alguien es una propuesta
 * directa: solo esa persona la ve y solo ella puede ofertar.
 */
@Entity
@Table(name = "solicitud")
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_autor", nullable = false)
    private Usuario autor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_destinatario")
    private Usuario destinatario;

    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(nullable = false, length = 1000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Modalidad modalidad;

    @Column(length = 60)
    private String distrito;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitud;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitud;

    @Column(name = "fecha_deseada")
    private LocalDate fechaDeseada;

    @Column(name = "precio_propuesto", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioPropuesto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado = EstadoSolicitud.ABIERTA;

    @Column(name = "fecha_publicacion", nullable = false)
    private LocalDateTime fechaPublicacion = LocalDateTime.now();

    protected Solicitud() {
    }

    public Solicitud(Categoria categoria, Usuario autor, String titulo, String descripcion,
                     Modalidad modalidad, BigDecimal precioPropuesto) {
        this.categoria = categoria;
        this.autor = autor;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.modalidad = modalidad;
        this.precioPropuesto = precioPropuesto;
    }

    /**
     * Si va dirigida a alguien en concreto en vez de salir al catálogo abierto.
     */
    public boolean esPropuestaDirecta() {
        return destinatario != null;
    }

    /**
     * Si todavía se puede cambiar o cancelar. Deja de poder en cuanto se acepta una oferta, porque
     * a partir de ahí hay alguien trabajando sobre lo acordado.
     */
    public boolean puedeEditarse() {
        return estado == EstadoSolicitud.ABIERTA;
    }

    /**
     * Retira la solicitud. No se borra: queda como CANCELADA para que quien ya había ofertado
     * entienda qué pasó.
     *
     * @throws IllegalStateException si ya se aceptó una oferta
     */
    public void cancelar() {
        if (!puedeEditarse()) {
            throw new IllegalStateException("La solicitud " + id + " ya no admite cambios");
        }
        this.estado = EstadoSolicitud.CANCELADA;
    }

    /**
     * Marca que el trato se cerró con una oferta. La solicitud sale del catálogo y pasa a EN_CURSO.
     */
    public void aceptarTrato() {
        this.estado = EstadoSolicitud.EN_CURSO;
    }

    /**
     * El servicio que salió de esta solicitud terminó. Queda como registro y no vuelve a cambiar.
     */
    public void cerrar() {
        this.estado = EstadoSolicitud.CERRADA;
    }

    public boolean esDe(Long idUsuario) {
        return autor.getId().equals(idUsuario);
    }

    /**
     * Si esta persona la puede ver en el catálogo y ofertar: no es suya y, si es una propuesta
     * directa, va para ella.
     */
    public boolean laPuedeOfertar(Long idUsuario) {
        return estado == EstadoSolicitud.ABIERTA && !esDe(idUsuario)
                && (destinatario == null || destinatario.getId().equals(idUsuario));
    }

    public Long getId() {
        return id;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Usuario getAutor() {
        return autor;
    }

    public Usuario getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(Usuario destinatario) {
        this.destinatario = destinatario;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Modalidad getModalidad() {
        return modalidad;
    }

    public void setModalidad(Modalidad modalidad) {
        this.modalidad = modalidad;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public BigDecimal getLatitud() {
        return latitud;
    }

    public void setLatitud(BigDecimal latitud) {
        this.latitud = latitud;
    }

    public BigDecimal getLongitud() {
        return longitud;
    }

    public void setLongitud(BigDecimal longitud) {
        this.longitud = longitud;
    }

    public LocalDate getFechaDeseada() {
        return fechaDeseada;
    }

    public void setFechaDeseada(LocalDate fechaDeseada) {
        this.fechaDeseada = fechaDeseada;
    }

    public BigDecimal getPrecioPropuesto() {
        return precioPropuesto;
    }

    public void setPrecioPropuesto(BigDecimal precioPropuesto) {
        this.precioPropuesto = precioPropuesto;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }
}
