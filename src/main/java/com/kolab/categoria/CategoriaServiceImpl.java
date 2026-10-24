package com.kolab.categoria;

import com.kolab.common.Fotos;
import com.kolab.common.RecursoNoEncontradoException;
import com.kolab.solicitud.SolicitudRepository;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de {@link CategoriaService}.
 */
@Service
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final SolicitudRepository solicitudRepository;
    private final Fotos fotos;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository,
                                SolicitudRepository solicitudRepository,
                                Fotos fotos) {
        this.categoriaRepository = categoriaRepository;
        this.solicitudRepository = solicitudRepository;
        this.fotos = fotos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResumen> activas() {
        Map<Long, Long> abiertas = abiertasPorCategoria();
        return categoriaRepository.findByEstadoOrderByNombre(EstadoCategoria.ACTIVA).stream()
                .map(c -> aResumen(c, abiertas.getOrDefault(c.getId(), 0L)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResumen resumen(Long idCategoria) {
        Categoria c = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new RecursoNoEncontradoException("categoría", idCategoria));
        return aResumen(c, abiertasPorCategoria().getOrDefault(idCategoria, 0L));
    }

    @Override
    @Transactional(readOnly = true)
    public PreciosDeCategoria precios(Long idCategoria) {
        Object[] fila = categoriaRepository.precios(idCategoria).stream()
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("categoría", idCategoria));
        return new PreciosDeCategoria(decimal(fila[0]), decimal(fila[1]), decimal(fila[2]),
                ((Number) fila[3]).longValue());
    }

    private Map<Long, Long> abiertasPorCategoria() {
        Map<Long, Long> cuenta = new HashMap<>();
        for (Object[] fila : solicitudRepository.contarAbiertasPorCategoria()) {
            cuenta.put((Long) fila[0], (Long) fila[1]);
        }
        return cuenta;
    }

    private CategoriaResumen aResumen(Categoria c, long abiertas) {
        return new CategoriaResumen(c.getId(), c.getNombre(), c.getIcono(), fotos.deCategoria(c.getNombre()),
                c.getPrecioRefMin(), c.getPrecioRefMax(), abiertas);
    }

    private BigDecimal decimal(Object valor) {
        return new BigDecimal(valor.toString()).setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
