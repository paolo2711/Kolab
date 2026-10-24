package com.kolab.mensaje;

/**
 * Un mensaje como se ve en pantalla. {@code dia} agrupa la conversación por jornadas y
 * {@code minuto} es lo que lleva la burbuja, que no repite el día.
 */
public record MensajeResumen(String contenido, String hora, boolean mio, String dia, String minuto) {
}
