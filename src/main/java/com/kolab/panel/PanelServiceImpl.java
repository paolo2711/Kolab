package com.kolab.panel;

import com.kolab.panel.ResumenDelPanel.BarraDeCategoria;
import com.kolab.panel.ResumenDelPanel.PrecioDeCategoria;
import com.kolab.panel.ResumenDelPanel.SolicitudReciente;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de {@link PanelService}. Consulta la base directamente: el panel es un reporte y
 * no necesita cargar entidades enteras para contar.
 */
@Service
public class PanelServiceImpl implements PanelService {

    private static final int ULTIMAS = 5;

    private final EntityManager em;

    public PanelServiceImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenDelPanel resumen() {
        List<BarraDeCategoria> barras = barras();
        return new ResumenDelPanel(
                unNumero("select count(*) from solicitud where estado = 'ABIERTA'"),
                unNumero("select count(*) from servicio where estado = 'CERRADO'"),
                unDecimal("select coalesce(sum(comision), 0) from servicio"),
                unDecimal("select coalesce(avg(puntaje), 0) from calificacion").setScale(2, RoundingMode.HALF_UP),
                unNumero("select count(*) from perfil"),
                barras,
                precios(),
                ultimas());
    }

    private List<BarraDeCategoria> barras() {
        List<Object[]> filas = filas("""
                select c.nombre, count(s.id_solicitud)
                from categoria c
                left join solicitud s on s.id_categoria = c.id_categoria and s.estado = 'ABIERTA'
                group by c.id_categoria, c.nombre
                order by count(s.id_solicitud) desc, c.nombre
                """);

        long mayor = filas.stream().mapToLong(f -> numero(f[1])).max().orElse(0);
        return filas.stream()
                .map(f -> new BarraDeCategoria((String) f[0], numero(f[1]),
                        mayor == 0 ? 0 : (int) Math.round(numero(f[1]) * 100.0 / mayor)))
                .toList();
    }

    private List<PrecioDeCategoria> precios() {
        return filas("""
                select nombre, solicitudes, precio_bajo, precio_tipico, precio_alto
                from v_precios_categoria
                order by nombre
                """).stream()
                .map(f -> new PrecioDeCategoria((String) f[0], numero(f[1]), decimal(f[2]),
                        decimal(f[3]), decimal(f[4])))
                .toList();
    }

    private List<SolicitudReciente> ultimas() {
        return filas("""
                select s.id_solicitud, s.titulo, c.nombre, u.nombre || ' ' || u.apellidos,
                       s.precio_propuesto, s.estado
                from solicitud s
                join categoria c on c.id_categoria = s.id_categoria
                join usuario u on u.id_usuario = s.id_autor
                order by s.fecha_publicacion desc, s.id_solicitud desc
                """).stream()
                .limit(ULTIMAS)
                .map(f -> new SolicitudReciente(numero(f[0]), (String) f[1], (String) f[2],
                        (String) f[3], decimal(f[4]), (String) f[5]))
                .toList();
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> filas(String sql) {
        return em.createNativeQuery(sql).getResultList();
    }

    private long unNumero(String sql) {
        return numero(em.createNativeQuery(sql).getSingleResult());
    }

    private BigDecimal unDecimal(String sql) {
        return decimal(em.createNativeQuery(sql).getSingleResult());
    }

    private long numero(Object valor) {
        return valor == null ? 0 : ((Number) valor).longValue();
    }

    private BigDecimal decimal(Object valor) {
        if (valor == null) {
            return BigDecimal.ZERO;
        }
        return valor instanceof BigDecimal b ? b : BigDecimal.valueOf(((Number) valor).doubleValue());
    }
}
