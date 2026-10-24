package com.kolab.common;

/**
 * Una regla del negocio impide lo que se intentó hacer, por ejemplo ofertar dos veces en la misma
 * solicitud. El mensaje se muestra tal cual a la persona.
 */
public class OperacionNoPermitidaException extends RuntimeException {

    public OperacionNoPermitidaException(String motivo) {
        super(motivo);
    }
}
