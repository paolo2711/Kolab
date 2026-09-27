package com.kolab.categoria;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Rubro del catálogo: Música, Idiomas, Hogar y reparaciones. Crece agregando filas, no código.
 *
 * <p>{@code precioRefMin} y {@code precioRefMax} son un rango fijo que se escribe al crear la
 * categoría. Solo se muestra mientras la categoría todavía no tiene solicitudes de las que sacar
 * precios reales; en cuanto las hay, manda la vista {@code v_precios_categoria}.
 */
@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Long id;

    @Column(nullable = false, length = 60, unique = true)
    private String nombre;

    @Column(length = 200)
    private String descripcion;

    @Column(length = 40)
    private String icono;

    @Column(name = "precio_ref_min", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioRefMin;

    @Column(name = "precio_ref_max", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioRefMax;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCategoria estado = EstadoCategoria.ACTIVA;

    protected Categoria() {
    }

    public Categoria(String nombre, String icono, BigDecimal precioRefMin, BigDecimal precioRefMax) {
        this.nombre = nombre;
        this.icono = icono;
        this.precioRefMin = precioRefMin;
        this.precioRefMax = precioRefMax;
    }

    /**
     * Si se puede publicar una solicitud nueva en esta categoría.
     */
    public boolean estaActiva() {
        return estado == EstadoCategoria.ACTIVA;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getIcono() {
        return icono;
    }

    public void setIcono(String icono) {
        this.icono = icono;
    }

    public BigDecimal getPrecioRefMin() {
        return precioRefMin;
    }

    public void setPrecioRefMin(BigDecimal precioRefMin) {
        this.precioRefMin = precioRefMin;
    }

    public BigDecimal getPrecioRefMax() {
        return precioRefMax;
    }

    public void setPrecioRefMax(BigDecimal precioRefMax) {
        this.precioRefMax = precioRefMax;
    }

    public EstadoCategoria getEstado() {
        return estado;
    }

    public void setEstado(EstadoCategoria estado) {
        this.estado = estado;
    }
}
