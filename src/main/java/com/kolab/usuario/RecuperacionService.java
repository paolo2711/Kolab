package com.kolab.usuario;

import java.util.Optional;

/**
 * Recuperar la contraseña con un enlace que vence. El enlace no se guarda en ninguna tabla: lleva
 * una firma que deja de valer cuando pasa el plazo o cuando la contraseña cambia.
 */
public interface RecuperacionService {

    /**
     * Genera el código del enlace para ese correo.
     *
     * @return vacío si el correo no tiene cuenta; quien llama no debe decirlo en pantalla
     */
    Optional<String> generarCodigo(String email);

    /**
     * Si el código sigue sirviendo: no venció, no fue alterado y la contraseña no cambió desde que
     * se generó.
     */
    boolean esValido(String codigo);

    /**
     * Pone la contraseña nueva. Después de esto el mismo código ya no sirve.
     *
     * @throws com.kolab.common.OperacionNoPermitidaException si el código ya no es válido
     */
    void cambiarClave(String codigo, String nuevaClave);
}
