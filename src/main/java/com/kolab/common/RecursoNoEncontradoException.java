package com.kolab.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Lo que se pidió no existe, o existe pero quien lo pide no tiene por qué verlo. En los dos casos
 * la respuesta es la misma, para no revelar qué ids existen.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String que, Long id) {
        super("No hay " + que + " con id " + id);
    }
}
