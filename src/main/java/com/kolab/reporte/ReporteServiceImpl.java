package com.kolab.reporte;

import com.kolab.calificacion.CalificacionRepository;
import com.kolab.oferta.EstadoOferta;
import com.kolab.oferta.OfertaRepository;
import com.kolab.perfil.PerfilRepository;
import com.kolab.servicio.EstadoServicio;
import com.kolab.servicio.ServicioRepository;
import com.kolab.solicitud.EstadoSolicitud;
import com.kolab.solicitud.SolicitudRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de {@link ReporteService}. Cada cifra es un conteo o una suma sobre las tablas del
 * negocio, calculada al consultar.
 */
@Service
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {

    private final SolicitudRepository solicitudRepository;
    private final OfertaRepository ofertaRepository;
    private final ServicioRepository servicioRepository;
    private final CalificacionRepository calificacionRepository;
    private final PerfilRepository perfilRepository;
    private final SeriesDelResumen series;

    public ReporteServiceImpl(SolicitudRepository solicitudRepository, OfertaRepository ofertaRepository,
                              ServicioRepository servicioRepository, CalificacionRepository calificacionRepository,
                              PerfilRepository perfilRepository, SeriesDelResumen series) {
        this.solicitudRepository = solicitudRepository;
        this.ofertaRepository = ofertaRepository;
        this.servicioRepository = servicioRepository;
        this.calificacionRepository = calificacionRepository;
        this.perfilRepository = perfilRepository;
        this.series = series;
    }

    @Override
    public IndicadoresDePlataforma plataforma() {
        BigDecimal promedio = BigDecimal.valueOf(calificacionRepository.promedioGeneral())
                .setScale(1, RoundingMode.HALF_UP);
        long abiertas = solicitudRepository.contarAbiertasPorCategoria().stream()
                .mapToLong(fila -> (Long) fila[1]).sum();
        return new IndicadoresDePlataforma(servicioRepository.countByEstado(EstadoServicio.CERRADO), promedio,
                perfilRepository.contarQuienesOfrecen(), abiertas);
    }

    @Override
    public MiResumen resumenDe(Long idUsuario) {
        return new MiResumen(pendientes(idUsuario), pido(idUsuario), ofrezco(idUsuario));
    }

    private MiResumen.Pendientes pendientes(Long idUsuario) {
        return new MiResumen.Pendientes(
                ofertaRepository.countBySolicitudAutorIdAndEstadoAndSolicitudEstado(
                        idUsuario, EstadoOferta.ENVIADA, EstadoSolicitud.ABIERTA),
                servicioRepository.countByOfertaSolicitudAutorIdAndEstado(idUsuario, EstadoServicio.EN_CURSO),
                servicioRepository.porCalificar(idUsuario));
    }

    private MiResumen.LoQuePido pido(Long idUsuario) {
        return new MiResumen.LoQuePido(
                solicitudRepository.countByAutorId(idUsuario),
                servicioRepository.countByOfertaSolicitudAutorId(idUsuario),
                servicioRepository.totalPagado(idUsuario),
                diferencia(servicioRepository.diferenciaPromedioConLoPropuesto(idUsuario)),
                series.horasHastaPrimeraOferta(idUsuario),
                series.pagadoPorCategoria(idUsuario));
    }

    private MiResumen.LoQueOfrezco ofrezco(Long idUsuario) {
        List<Long> porCliente = servicioRepository.serviciosPorCliente(idUsuario);
        return new MiResumen.LoQueOfrezco(
                ofertaRepository.countByUsuarioId(idUsuario),
                ofertaRepository.countByUsuarioIdAndEstado(idUsuario, EstadoOferta.ACEPTADA),
                servicioRepository.totalGanado(idUsuario),
                porCliente.size(),
                porCliente.stream().filter(n -> n > 1).count(),
                diferencia(ofertaRepository.diferenciaPromedioConLoPedido(idUsuario)),
                series.ganadoPorMes(idUsuario),
                series.ganadoPorCategoria(idUsuario));
    }

    private static BigDecimal diferencia(Double promedio) {
        return promedio == null ? null : BigDecimal.valueOf(promedio).setScale(2, RoundingMode.HALF_UP);
    }
}
