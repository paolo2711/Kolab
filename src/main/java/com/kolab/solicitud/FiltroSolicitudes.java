package com.kolab.solicitud;

import java.math.BigDecimal;

public class FiltroSolicitudes {

    private Long idCategoria;
    private BigDecimal precioMinimo;
    private BigDecimal precioMaximo;
    private Modalidad modalidad;
    private String distrito;
    private boolean soloLoQueSe;

    public boolean vacio() {
        return idCategoria == null && precioMinimo == null && precioMaximo == null
                && modalidad == null && (distrito == null || distrito.isBlank()) && !soloLoQueSe;
    }

    public Long getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Long idCategoria) {
        this.idCategoria = idCategoria;
    }

    public BigDecimal getPrecioMinimo() {
        return precioMinimo;
    }

    public void setPrecioMinimo(BigDecimal precioMinimo) {
        this.precioMinimo = precioMinimo;
    }

    public BigDecimal getPrecioMaximo() {
        return precioMaximo;
    }

    public void setPrecioMaximo(BigDecimal precioMaximo) {
        this.precioMaximo = precioMaximo;
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

    public boolean isSoloLoQueSe() {
        return soloLoQueSe;
    }

    public void setSoloLoQueSe(boolean soloLoQueSe) {
        this.soloLoQueSe = soloLoQueSe;
    }
}
