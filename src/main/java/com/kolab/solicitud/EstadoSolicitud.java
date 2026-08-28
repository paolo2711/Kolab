package com.kolab.solicitud;

// cada estado trae su marca: un aro si sigue en juego, un visto si terminó bien, una raya si
// quedó fuera. la forma dice el estado, el color solo acompaña
public enum EstadoSolicitud {

    ABIERTA("Abierta", "espera", "bi-circle"),
    EN_CURSO("En curso", "espera", "bi-circle-half"),
    CERRADA("Cerrada", "bien", "bi-check-circle-fill");

    private final String etiqueta;
    private final String tono;
    private final String icono;

    EstadoSolicitud(String etiqueta, String tono, String icono) {
        this.etiqueta = etiqueta;
        this.tono = tono;
        this.icono = icono;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getTono() {
        return tono;
    }

    public String getIcono() {
        return icono;
    }
}
