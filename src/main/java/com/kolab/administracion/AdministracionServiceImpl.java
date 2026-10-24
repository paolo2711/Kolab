package com.kolab.administracion;

import com.kolab.common.Tiempo;
import com.kolab.servicio.EstadoServicio;
import com.kolab.servicio.ServicioRepository;
import com.kolab.solicitud.EstadoSolicitud;
import com.kolab.solicitud.SolicitudRepository;
import com.kolab.usuario.EstadoUsuario;
import com.kolab.usuario.Usuario;
import com.kolab.usuario.UsuarioRepository;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdministracionServiceImpl implements AdministracionService {

    private static final int MESES = 6;
    private static final int CATEGORIAS = 5;

    private final UsuarioRepository usuarioRepository;
    private final SolicitudRepository solicitudRepository;
    private final ServicioRepository servicioRepository;

    public AdministracionServiceImpl(UsuarioRepository usuarioRepository, SolicitudRepository solicitudRepository,
                                     ServicioRepository servicioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.solicitudRepository = solicitudRepository;
        this.servicioRepository = servicioRepository;
    }

    @Override
    public Page<CuentaResumen> cuentas(Pageable pagina) {
        Pageable nuevasPrimero = PageRequest.of(pagina.getPageNumber(), pagina.getPageSize(),
                Sort.by(Sort.Direction.DESC, "fechaRegistro"));
        return usuarioRepository.findAll(nuevasPrimero).map(this::aResumen);
    }

    @Override
    public Tablero tablero() {
        List<Tablero.Mes> meses = ultimosMeses();
        return new Tablero(
                usuarioRepository.count(),
                solicitudRepository.countByEstado(EstadoSolicitud.ABIERTA),
                servicioRepository.countByEstado(EstadoServicio.CERRADO),
                meses.get(meses.size() - 1).comision(),
                meses,
                solicitudRepository.categoriasMasPedidas(PageRequest.of(0, CATEGORIAS)).stream()
                        .map(f -> new Tablero.CategoriaPedida((String) f[0], ((Number) f[1]).longValue()))
                        .toList());
    }

    // la base solo trae los meses con movimiento; los demás van en cero para que siempre salgan los seis
    private List<Tablero.Mes> ultimosMeses() {
        YearMonth actual = YearMonth.now();
        YearMonth primero = actual.minusMonths(MESES - 1L);

        Map<YearMonth, Long> publicadas = new HashMap<>();
        for (Object[] f : solicitudRepository.publicadasPorMesDesde(primero.atDay(1).atStartOfDay())) {
            publicadas.put(mes(f), ((Number) f[2]).longValue());
        }
        Map<YearMonth, Object[]> cerrados = new HashMap<>();
        for (Object[] f : servicioRepository.cerradosPorMesDesde(primero.atDay(1).atStartOfDay())) {
            cerrados.put(mes(f), f);
        }

        List<Tablero.Mes> meses = new ArrayList<>();
        for (YearMonth m = primero; !m.isAfter(actual); m = m.plusMonths(1)) {
            Object[] servicios = cerrados.get(m);
            meses.add(new Tablero.Mes(Tiempo.mesYAnio(m.atDay(1).atStartOfDay()),
                    publicadas.getOrDefault(m, 0L),
                    servicios == null ? 0 : ((Number) servicios[2]).longValue(),
                    servicios == null ? BigDecimal.ZERO : (BigDecimal) servicios[3]));
        }
        return meses;
    }

    private static YearMonth mes(Object[] fila) {
        return YearMonth.of(((Number) fila[0]).intValue(), ((Number) fila[1]).intValue());
    }

    private CuentaResumen aResumen(Usuario u) {
        return new CuentaResumen(u.getNombreCompleto(), u.getEmail(),
                Tiempo.fecha(u.getFechaRegistro()), u.getEstado() == EstadoUsuario.ACTIVO);
    }
}
