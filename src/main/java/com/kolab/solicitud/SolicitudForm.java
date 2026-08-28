package com.kolab.solicitud;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class SolicitudForm {

    @NotBlank(message = "Ponle un título a tu solicitud")
    @Size(max = 120, message = "Máximo 120 caracteres")
    private String titulo;

    @NotNull(message = "Elige una categoría")
    private Long idCategoria;

    @NotNull(message = "Elige la modalidad")
    private Modalidad modalidad = Modalidad.PRESENCIAL;

    @Size(max = 60, message = "Máximo 60 caracteres")
    private String distrito;

    @NotBlank(message = "Cuenta qué necesitas")
    @Size(min = 20, max = 1000, message = "Entre 20 y 1000 caracteres")
    private String descripcion;

    @NotNull(message = "Indica para cuándo lo necesitas")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaDeseada;

    @NotNull(message = "Propón un precio")
    @DecimalMin(value = "10.00", message = "El mínimo es S/ 10")
    @DecimalMax(value = "9999.00", message = "El máximo es S/ 9999")
    private BigDecimal precioPropuesto;

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Long getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Long idCategoria) {
        this.idCategoria = idCategoria;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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
}
