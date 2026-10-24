package com.kolab.servicio;

import com.kolab.calificacion.CalificacionForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Los servicios que nacen al aceptar una oferta: seguirlos, cerrarlos y calificarlos.
 */
public interface ServicioService {

    /**
     * El servicio visto por una de sus partes. Para cualquier otra persona no existe.
     */
    ServicioDetalle detalle(Long idServicio, Long idUsuario);

    Page<ServicioResumen> queContrate(Long idUsuario, Pageable pagina);

    Page<ServicioResumen> queDoy(Long idUsuario, Pageable pagina);

    /**
     * Quien pidió el servicio confirma que se hizo: se cierra el servicio, se cierra la solicitud y
     * queda su calificación. La otra parte, con el servicio ya cerrado, solo califica.
     */
    void cerrarYCalificar(Long idServicio, CalificacionForm form, Long idUsuario);
}
