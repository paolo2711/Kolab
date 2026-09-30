package com.kolab.panel;

/**
 * Las cifras que muestra el panel. Sale todo de la base, no de los datos de ejemplo en JSON.
 */
public interface PanelService {

    /**
     * Cuenta solicitudes, servicios, comisiones y calificaciones tal como estan ahora.
     */
    ResumenDelPanel resumen();
}
