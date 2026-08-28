package com.kolab.oferta;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class OfertaForm {

    @NotNull(message = "Indica por cuánto lo haces")
    @DecimalMin(value = "10.00", message = "El mínimo es S/ 10")
    private BigDecimal monto;

    @Size(max = 300, message = "Máximo 300 caracteres")
    private String mensaje;

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
