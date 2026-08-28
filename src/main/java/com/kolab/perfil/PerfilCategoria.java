package com.kolab.perfil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// lo que el usuario dice que sabe hacer. es la versión simplificada de HABILIDAD_EXPERTO:
// por ahora se guarda la categoría, las habilidades finas entran con el catálogo real.
@Entity
@Table(name = "perfil_categoria")
public class PerfilCategoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_perfil_categoria")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_perfil", nullable = false)
    private PerfilExperto perfil;

    @Column(name = "id_categoria", nullable = false)
    private Long idCategoria;

    protected PerfilCategoria() {
    }

    public PerfilCategoria(PerfilExperto perfil, Long idCategoria) {
        this.perfil = perfil;
        this.idCategoria = idCategoria;
    }

    public Long getId() {
        return id;
    }

    public PerfilExperto getPerfil() {
        return perfil;
    }

    public Long getIdCategoria() {
        return idCategoria;
    }
}
