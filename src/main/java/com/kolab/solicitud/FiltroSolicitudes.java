package com.kolab.solicitud;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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

    // los filtros activos como parte de una dirección, para que al pasar de página no se pierdan
    public String aConsulta() {
        StringBuilder consulta = new StringBuilder();
        agregar(consulta, "idCategoria", idCategoria);
        agregar(consulta, "precioMinimo", precioMinimo);
        agregar(consulta, "precioMaximo", precioMaximo);
        agregar(consulta, "modalidad", modalidad);
        agregar(consulta, "distrito", distrito == null || distrito.isBlank() ? null : distrito);
        agregar(consulta, "soloLoQueSe", soloLoQueSe ? true : null);
        return consulta.toString();
    }

    private static void agregar(StringBuilder consulta, String nombre, Object valor) {
        if (valor != null) {
            consulta.append(nombre).append('=')
                    .append(URLEncoder.encode(valor.toString(), StandardCharsets.UTF_8)).append('&');
        }
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
