package com.kolab.administracion;

import static org.assertj.core.api.Assertions.assertThat;

import com.kolab.common.Tiempo;
import com.kolab.servicio.EstadoServicio;
import com.kolab.servicio.ServicioRepository;
import com.kolab.solicitud.EstadoSolicitud;
import com.kolab.solicitud.SolicitudRepository;
import com.kolab.usuario.UsuarioRepository;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AdministracionServiceImplTest {

    private final UsuarioRepository usuarios = Mockito.mock(UsuarioRepository.class);
    private final SolicitudRepository solicitudes = Mockito.mock(SolicitudRepository.class);
    private final ServicioRepository servicios = Mockito.mock(ServicioRepository.class);
    private final AdministracionService administracion = new AdministracionServiceImpl(usuarios, solicitudes, servicios);

    private final YearMonth actual = YearMonth.now();
    private final YearMonth haceDos = actual.minusMonths(2);

    @BeforeEach
    void plataforma() {
        Mockito.when(usuarios.count()).thenReturn(15L);
        Mockito.when(solicitudes.countByEstado(EstadoSolicitud.ABIERTA)).thenReturn(12L);
        Mockito.when(servicios.countByEstado(EstadoServicio.CERRADO)).thenReturn(9L);
        // la base solo devuelve los meses que tuvieron algo
        Mockito.when(solicitudes.publicadasPorMesDesde(Mockito.any())).thenReturn(List.of(
                fila(haceDos, 3L), fila(actual, 5L)));
        Mockito.when(servicios.cerradosPorMesDesde(Mockito.any())).thenReturn(List.<Object[]>of(
                new Object[] {actual.getYear(), actual.getMonthValue(), 2L, new BigDecimal("8.50")}));
        Mockito.when(solicitudes.categoriasMasPedidas(Mockito.any())).thenReturn(List.of(
                new Object[] {"Idiomas", 8L}, new Object[] {"Música", 5L}));
    }

    @Test
    void lasCifrasDeHoySalenDeLaBase() {
        Tablero t = administracion.tablero();

        assertThat(t.cuentas()).isEqualTo(15);
        assertThat(t.solicitudesAbiertas()).isEqualTo(12);
        assertThat(t.serviciosCerrados()).isEqualTo(9);
    }

    @Test
    void losMesesSonSiempreSeisDelMasAntiguoAlActualYLosVaciosEnCero() {
        List<Tablero.Mes> meses = administracion.tablero().meses();

        assertThat(meses).hasSize(6);
        assertThat(meses.get(0).nombre()).isEqualTo(nombre(actual.minusMonths(5)));
        assertThat(meses.get(5).nombre()).isEqualTo(nombre(actual));
        assertThat(meses).extracting(Tablero.Mes::solicitudes).containsExactly(0L, 0L, 0L, 3L, 0L, 5L);
        assertThat(meses).extracting(Tablero.Mes::serviciosCerrados).containsExactly(0L, 0L, 0L, 0L, 0L, 2L);
        assertThat(meses.get(0).comision()).isEqualByComparingTo("0");
    }

    @Test
    void laComisionDelMesEsLaDelMesActualDeLaTabla() {
        Tablero t = administracion.tablero();

        assertThat(t.comisionDelMes()).isEqualByComparingTo("8.50");
        assertThat(t.comisionDelMes()).isEqualByComparingTo(t.meses().get(5).comision());
    }

    @Test
    void lasCategoriasVanDeLaMasPedidaALaMenos() {
        assertThat(administracion.tablero().categorias())
                .containsExactly(new Tablero.CategoriaPedida("Idiomas", 8), new Tablero.CategoriaPedida("Música", 5));
    }

    private static Object[] fila(YearMonth mes, long cuantas) {
        return new Object[] {mes.getYear(), mes.getMonthValue(), cuantas};
    }

    private static String nombre(YearMonth mes) {
        return Tiempo.mesYAnio(mes.atDay(1).atStartOfDay());
    }
}
