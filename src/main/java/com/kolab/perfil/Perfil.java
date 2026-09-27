package com.kolab.perfil;

import com.kolab.categoria.Categoria;
import com.kolab.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Lo que la persona cuenta de sí misma y su reputación. Es uno por cuenta, no hay perfil de
 * cliente y perfil de experto aparte: el actor es uno solo.
 *
 * <p>Nace cuando alguien declara por primera vez qué sabe hacer, no al registrarse. Quien solo
 * pide servicios nunca necesita uno.
 *
 * <p>{@code califPromedio} y {@code totalCalificaciones} están guardados a propósito en vez de
 * calcularse al consultar: el promedio se lee en cada listado de expertos y recalcularlo cada vez
 * obligaría a recorrer todas las calificaciones de la persona.
 */
@Entity
@Table(name = "perfil")
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_perfil")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    @Column(length = 500)
    private String descripcion;

    @Column(length = 500)
    private String experiencia;

    @Column(name = "calif_promedio", nullable = false, precision = 3, scale = 2)
    private BigDecimal califPromedio = BigDecimal.ZERO;

    @Column(name = "total_calificaciones", nullable = false)
    private int totalCalificaciones = 0;

    protected Perfil() {
    }

    public Perfil(Usuario usuario) {
        this.usuario = usuario;
    }

    /**
     * Si todavía nadie lo calificó. Sirve para no mostrar un 0,00 que se lee como mala nota.
     */
    public boolean sinCalificar() {
        return totalCalificaciones == 0;
    }

    /**
     * Suma una calificación nueva al promedio sin releer las anteriores.
     *
     * @param puntaje del 1 al 5, el que puso la otra parte al cerrar el servicio
     */
    public void registrarCalificacion(int puntaje) {
        BigDecimal suma = califPromedio.multiply(BigDecimal.valueOf(totalCalificaciones))
                .add(BigDecimal.valueOf(puntaje));
        this.totalCalificaciones = totalCalificaciones + 1;
        this.califPromedio = suma.divide(BigDecimal.valueOf(totalCalificaciones), 2, RoundingMode.HALF_UP);
    }

    /**
     * Declara que esta persona sabe hacer algo de esa categoría.
     *
     * @return la fila de la tabla intermedia, lista para guardar
     */
    public PerfilCategoria declararCategoria(Categoria categoria) {
        return new PerfilCategoria(this, categoria);
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getExperiencia() {
        return experiencia;
    }

    public void setExperiencia(String experiencia) {
        this.experiencia = experiencia;
    }

    public BigDecimal getCalifPromedio() {
        return califPromedio;
    }

    public int getTotalCalificaciones() {
        return totalCalificaciones;
    }
}
