package com.kolab.calificacion;

import com.kolab.servicio.Servicio;
import com.kolab.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * La nota que una parte le pone a la otra al cerrar un servicio, del 1 al 5.
 *
 * <p>Cada parte califica una sola vez por servicio, así que un servicio tiene dos como máximo. Al
 * guardarla se actualiza el promedio del perfil del evaluado en la misma transacción, para no
 * recalcularlo cada vez que alguien abre ese perfil.
 */
@Entity
@Table(name = "calificacion")
public class Calificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_calificacion")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_servicio", nullable = false)
    private Servicio servicio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_evaluador", nullable = false)
    private Usuario evaluador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_evaluado", nullable = false)
    private Usuario evaluado;

    // el diagrama dice int y la tabla dice smallint: las dos cosas son ciertas, solo hay que
    // decirle a JPA cual es el tipo de la columna
    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.SMALLINT)
    private int puntaje;

    @Column(length = 500)
    private String comentario;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    protected Calificacion() {
    }

    public Calificacion(Servicio servicio, Usuario evaluador, Usuario evaluado, int puntaje,
                        String comentario) {
        this.servicio = servicio;
        this.evaluador = evaluador;
        this.evaluado = evaluado;
        this.puntaje = puntaje;
        this.comentario = comentario;
    }

    /**
     * Si la calificación se puede guardar: el puntaje está en rango y nadie se está calificando a
     * sí mismo. Son las dos reglas que la base también controla.
     */
    public boolean esValida() {
        return puntaje >= 1 && puntaje <= 5 && !evaluador.getId().equals(evaluado.getId());
    }

    public Long getId() {
        return id;
    }

    public Servicio getServicio() {
        return servicio;
    }

    public Usuario getEvaluador() {
        return evaluador;
    }

    public Usuario getEvaluado() {
        return evaluado;
    }

    public int getPuntaje() {
        return puntaje;
    }

    public String getComentario() {
        return comentario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}
