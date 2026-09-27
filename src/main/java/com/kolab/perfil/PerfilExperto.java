package com.kolab.perfil;

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

@Entity
@Table(name = "perfil")
public class PerfilExperto {

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

    protected PerfilExperto() {
    }

    public PerfilExperto(Usuario usuario) {
        this.usuario = usuario;
        this.califPromedio = BigDecimal.ZERO;
    }

    public boolean sinCalificar() {
        return califPromedio.compareTo(BigDecimal.ZERO) == 0;
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

    public void setCalifPromedio(BigDecimal califPromedio) {
        this.califPromedio = califPromedio;
    }
}
