package com.kolab.perfil;

import com.kolab.categoria.Categoria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Una categoría que la persona declaró saber hacer. Es la tabla intermedia entre {@link Perfil} y
 * {@link Categoria}, y de ella sale lo que a cada quien le aparece para ofertar.
 */
@Entity
@Table(name = "perfil_categoria")
public class PerfilCategoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_perfil_categoria")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_perfil", nullable = false)
    private Perfil perfil;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    protected PerfilCategoria() {
    }

    public PerfilCategoria(Perfil perfil, Categoria categoria) {
        this.perfil = perfil;
        this.categoria = categoria;
    }

    public Long getId() {
        return id;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    /**
     * El id de la categoría sin cargarla: sobre un proxy perezoso no dispara consulta.
     */
    public Long getIdCategoria() {
        return categoria.getId();
    }
}
