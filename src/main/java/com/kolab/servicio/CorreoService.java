package com.kolab.servicio;

/**
 * manda los correos de kolab, como el enlace para recuperar la contrasena
 */
public interface CorreoService {

    /**
     * manda el enlace a ese correo. si falla queda en el log y la pantalla sigue igual
     */
    void enviarEnlaceDeRecuperacion(String email, String enlace);
}
