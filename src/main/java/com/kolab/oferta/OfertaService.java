package com.kolab.oferta;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * El trato: ofertar sobre una solicitud y, del otro lado, aceptar o rechazar lo que llega.
 */
public interface OfertaService {

    /**
     * Registra la oferta. Una persona oferta una sola vez por solicitud, nunca en la suya, y en una
     * propuesta directa solo puede ofertar el destinatario.
     *
     * @return el id de la oferta nueva
     */
    Long ofertar(Long idSolicitud, OfertaForm form, Long idUsuario);

    Optional<OfertaResumen> miOfertaEn(Long idSolicitud, Long idUsuario);

    /**
     * Acepta la oferta: nace el servicio con la comisión calculada sobre el monto acordado, la
     * solicitud deja de recibir ofertas y las demás quedan como no elegidas.
     *
     * @return el id del servicio creado
     */
    Long aceptar(Long idOferta, Long idAutor);

    void rechazar(Long idOferta, Long idAutor);

    Page<OfertaEnviada> mias(Long idUsuario, Pageable pagina);

    long cuantasMias(Long idUsuario);
}
