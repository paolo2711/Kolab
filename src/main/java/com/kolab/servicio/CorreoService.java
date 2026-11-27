package com.kolab.servicio;

/**
 * Manda los correos de KOLAB. Por ahora solo el enlace para recuperar la contraseña.
 */
public interface CorreoService {

    /**
     * Manda el enlace a ese correo. Si el envio falla queda en el log y la pantalla sigue igual.
     */
    void enviarEnlaceDeRecuperacion(String email, String enlace);
}
