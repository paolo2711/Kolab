package com.kolab.calificacion;

import com.kolab.servicio.Servicio;
import java.util.Collection;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Las calificaciones que cada parte le pone a la otra al cerrar un servicio.
 */
public interface CalificacionService {

    /**
     * Guarda la calificación de una de las partes a la otra y la suma a su promedio. Cada parte
     * califica una sola vez por servicio, y solo cuando el servicio ya está cerrado.
     */
    void registrar(Servicio servicio, Long idEvaluador, CalificacionForm form);

    boolean yaCalifico(Long idServicio, Long idEvaluador);

    Page<ResenaResumen> recibidas(Long idUsuario, Pageable pagina);

    /**
     * El último comentario que recibió cada persona, para mostrarlo en su tarjeta.
     */
    Map<Long, String> ultimoComentarioDe(Collection<Long> idsUsuario);
}
