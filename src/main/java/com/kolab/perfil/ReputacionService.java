package com.kolab.perfil;

import java.util.Collection;
import java.util.Map;

/**
 * La reputación de las personas, leída del perfil y de los servicios cerrados.
 */
public interface ReputacionService {

    /**
     * La reputación de varias personas en dos consultas, sea cual sea el tamaño de la lista.
     */
    Map<Long, Reputacion> de(Collection<Long> idsUsuario);

    Reputacion de(Long idUsuario);

    /**
     * Suma una calificación al promedio de quien la recibió.
     */
    void sumarCalificacion(Long idUsuario, int puntaje);
}
