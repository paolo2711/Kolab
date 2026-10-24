package com.kolab.solicitud;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Lo que una persona hace con sus propias solicitudes, y cómo se ve una solicitud al abrirla.
 */
public interface SolicitudService {

    /**
     * Publica la solicitud con el precio que propone quien la escribe. Si trae destinatario, es una
     * propuesta directa y solo esa persona la ve.
     *
     * @return el id de la solicitud nueva
     */
    Long publicar(SolicitudForm form, Long idAutor);

    /**
     * La solicitud tal como la ve quien va a ofertar, con las ofertas contra las que compite.
     */
    SolicitudDetalle paraOfertar(Long idSolicitud, Long idUsuario);

    /**
     * La solicitud tal como la ve quien la publicó, con las ofertas recibidas y sus mensajes sin leer.
     */
    SolicitudDetalle propia(Long idSolicitud, Long idAutor);

    boolean esSuya(Long idSolicitud, Long idUsuario);

    SolicitudForm formularioDe(Long idSolicitud, Long idAutor);

    void editar(Long idSolicitud, SolicitudForm form, Long idAutor);

    /**
     * La retira del catálogo. No se borra: queda cancelada para quien ya había ofertado.
     */
    void cancelar(Long idSolicitud, Long idAutor);

    Page<MiSolicitud> mias(Long idAutor, Pageable pagina);

    long cuantasMias(Long idAutor);
}
