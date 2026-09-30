package com.kolab.solicitud;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Las solicitudes abiertas que cada persona puede ver y ofertar. Una propuesta directa solo la ve
 * su destinatario, y nadie ve las suyas propias.
 */
public interface CatalogoService {

    Page<SolicitudResumen> explorar(FiltroSolicitudes filtro, Long idUsuario, Pageable pagina);

    /**
     * Las más recientes en las categorías que la persona declaró. Vacío si no declaró ninguna.
     */
    List<SolicitudResumen> enMisCategorias(Long idUsuario, int cuantas);

    List<SolicitudResumen> recientes(Long idUsuario, int cuantas);

    /**
     * Las presenciales más cerca de donde vive la persona. Vacío si no se sabe dónde vive.
     */
    List<SolicitudResumen> cercanas(Long idUsuario, int cuantas);

    List<SolicitudResumen> abiertasEn(Long idCategoria, Long idUsuario, int cuantas);

    /**
     * Lo que ve quien todavía no entró: solo las que están abiertas a todos.
     */
    List<SolicitudResumen> recientesPublicas(int cuantas);

    List<String> distritos();
}
