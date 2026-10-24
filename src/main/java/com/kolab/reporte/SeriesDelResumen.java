package com.kolab.reporte;

import com.kolab.oferta.OfertaRepository;
import com.kolab.reporte.MiResumen.Barra;
import com.kolab.servicio.Servicio;
import com.kolab.servicio.ServicioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import org.springframework.stereotype.Component;

/**
 * Las series de Mi resumen: lo ganado mes a mes, lo ganado y lo pagado por categoría, y cuánto
 * tarda en llegar la primera oferta.
 */
@Component
class SeriesDelResumen {

    // el tramo que muestran los gráficos por mes
    private static final int MESES = 6;
    private static final DateTimeFormatter MES = DateTimeFormatter.ofPattern("MMM", Locale.forLanguageTag("es-PE"));

    private final ServicioRepository servicioRepository;
    private final OfertaRepository ofertaRepository;

    SeriesDelResumen(ServicioRepository servicioRepository, OfertaRepository ofertaRepository) {
        this.servicioRepository = servicioRepository;
        this.ofertaRepository = ofertaRepository;
    }

    List<Barra> ganadoPorMes(Long idUsuario) {
        return porMes(servicioRepository.cerradosQueDiDesde(idUsuario, desde()), Servicio::getNeto);
    }

    List<Barra> ganadoPorCategoria(Long idUsuario) {
        return porCategoria(servicioRepository.ganadoPorCategoria(idUsuario));
    }

    List<Barra> pagadoPorCategoria(Long idUsuario) {
        return porCategoria(servicioRepository.pagadoPorCategoria(idUsuario));
    }

    Double horasHastaPrimeraOferta(Long idUsuario) {
        return ofertaRepository.primerasOfertas(idUsuario).stream()
                .mapToLong(f -> Duration.between((LocalDateTime) f[0], (LocalDateTime) f[1]).toMinutes())
                .average()
                .stream()
                .map(minutos -> minutos / 60)
                .boxed()
                .findFirst()
                .orElse(null);
    }

    // de la que más deja a la que menos
    private static List<Barra> porCategoria(List<Object[]> filas) {
        List<Object[]> ordenadas = new ArrayList<>(filas);
        ordenadas.sort((a, b) -> ((BigDecimal) b[1]).compareTo((BigDecimal) a[1]));
        return barras(ordenadas.stream().map(f -> (String) f[0]).toList(),
                ordenadas.stream().map(f -> (BigDecimal) f[1]).toList());
    }

    private List<Barra> porMes(List<Servicio> servicios, Function<Servicio, BigDecimal> monto) {
        YearMonth actual = YearMonth.now();
        Map<YearMonth, BigDecimal> suma = new HashMap<>();
        for (Servicio s : servicios) {
            suma.merge(YearMonth.from(s.getFechaCierre()), monto.apply(s), BigDecimal::add);
        }
        List<String> rotulos = new ArrayList<>();
        List<BigDecimal> valores = new ArrayList<>();
        for (int i = MESES - 1; i >= 0; i--) {
            YearMonth mes = actual.minusMonths(i);
            rotulos.add(mes.format(MES));
            valores.add(suma.getOrDefault(mes, BigDecimal.ZERO));
        }
        return barras(rotulos, valores);
    }

    private static List<Barra> barras(List<String> rotulos, List<BigDecimal> valores) {
        BigDecimal mayor = valores.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        List<Barra> barras = new ArrayList<>();
        for (int i = 0; i < rotulos.size(); i++) {
            BigDecimal v = valores.get(i);
            int largo = mayor.signum() == 0 ? 0
                    : v.multiply(BigDecimal.valueOf(100)).divide(mayor, 0, RoundingMode.HALF_UP).intValue();
            barras.add(new Barra(rotulos.get(i), v, largo));
        }
        return barras;
    }

    private static LocalDateTime desde() {
        return YearMonth.now().minusMonths(MESES - 1L).atDay(1).atStartOfDay();
    }
}
