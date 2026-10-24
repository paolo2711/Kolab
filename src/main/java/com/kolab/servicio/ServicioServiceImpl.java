package com.kolab.servicio;

import com.kolab.calificacion.CalificacionForm;
import com.kolab.calificacion.CalificacionService;
import com.kolab.common.OperacionNoPermitidaException;
import com.kolab.common.RecursoNoEncontradoException;
import com.kolab.common.Tiempo;
import com.kolab.mensaje.MensajeService;
import com.kolab.perfil.Personas;
import com.kolab.perfil.ReputacionService;
import com.kolab.solicitud.Solicitud;
import com.kolab.usuario.Usuario;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de {@link ServicioService}.
 */
@Service
public class ServicioServiceImpl implements ServicioService {

    private final ServicioRepository servicioRepository;
    private final CalificacionService calificacionService;
    private final ReputacionService reputacionService;
    private final MensajeService mensajeService;
    private final Personas personas;

    public ServicioServiceImpl(ServicioRepository servicioRepository, CalificacionService calificacionService,
                               ReputacionService reputacionService, MensajeService mensajeService,
                               Personas personas) {
        this.servicioRepository = servicioRepository;
        this.calificacionService = calificacionService;
        this.reputacionService = reputacionService;
        this.mensajeService = mensajeService;
        this.personas = personas;
    }

    @Override
    @Transactional(readOnly = true)
    public ServicioDetalle detalle(Long idServicio, Long idUsuario) {
        Servicio s = deUnaParte(idServicio, idUsuario);
        Solicitud solicitud = s.getOferta().getSolicitud();
        boolean pido = solicitud.esDe(idUsuario);
        Usuario otro = pido ? s.getOferta().getUsuario() : solicitud.getAutor();
        Long idOferta = s.getOferta().getId();
        return new ServicioDetalle(s.getId(), idOferta, solicitud.getId(), solicitud.getTitulo(),
                solicitud.getCategoria().getNombre(), personas.de(otro), reputacionService.de(otro.getId()),
                Tiempo.mesYAnio(otro.getFechaRegistro()), s.getMontoFinal(), s.getComision(), s.getEstado(),
                "empezó " + Tiempo.hace(s.getFechaInicio()),
                s.getFechaCierre() == null ? null : "cerrado " + Tiempo.hace(s.getFechaCierre()),
                pido, calificacionService.yaCalifico(idServicio, idUsuario),
                mensajeService.hilos(List.of(idOferta), idUsuario).getOrDefault(idOferta, List.of()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServicioResumen> queContrate(Long idUsuario, Pageable pagina) {
        return servicioRepository.findByOfertaSolicitudAutorIdOrderByFechaInicioDesc(idUsuario, pagina)
                .map(s -> aResumen(s, s.getOferta().getUsuario(), false));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServicioResumen> queDoy(Long idUsuario, Pageable pagina) {
        return servicioRepository.findByOfertaUsuarioIdOrderByFechaInicioDesc(idUsuario, pagina)
                .map(s -> aResumen(s, s.getOferta().getSolicitud().getAutor(), true));
    }

    @Override
    @Transactional
    public void cerrarYCalificar(Long idServicio, CalificacionForm form, Long idUsuario) {
        Servicio s = deUnaParte(idServicio, idUsuario);
        Solicitud solicitud = s.getOferta().getSolicitud();
        if (!s.estaCerrado()) {
            if (!solicitud.esDe(idUsuario)) {
                throw new OperacionNoPermitidaException(
                        "El servicio lo cierra quien lo pidió, cuando confirma que se hizo.");
            }
            s.cerrar();
            solicitud.cerrar();
        }
        calificacionService.registrar(s, idUsuario, form);
    }

    private Servicio deUnaParte(Long idServicio, Long idUsuario) {
        Servicio s = servicioRepository.findConPartesById(idServicio)
                .orElseThrow(() -> new RecursoNoEncontradoException("servicio", idServicio));
        boolean esParte = s.getOferta().getUsuario().getId().equals(idUsuario)
                || s.getOferta().getSolicitud().esDe(idUsuario);
        if (!esParte) {
            throw new RecursoNoEncontradoException("servicio", idServicio);
        }
        return s;
    }

    private ServicioResumen aResumen(Servicio s, Usuario otro, boolean loDoy) {
        String cuando = s.estaCerrado() && s.getFechaCierre() != null
                ? "cerrado " + Tiempo.hace(s.getFechaCierre())
                : "empezó " + Tiempo.hace(s.getFechaInicio());
        return new ServicioResumen(s.getId(), s.getOferta().getSolicitud().getTitulo(), personas.de(otro),
                s.getMontoFinal(), s.getComision(), s.getEstado(), cuando, loDoy);
    }
}
