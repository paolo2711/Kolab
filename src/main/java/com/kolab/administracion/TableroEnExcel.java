package com.kolab.administracion;

import static com.kolab.reporte.LibroExcel.Columna.numero;
import static com.kolab.reporte.LibroExcel.Columna.soles;
import static com.kolab.reporte.LibroExcel.Columna.texto;

import com.kolab.common.Tiempo;
import com.kolab.reporte.LibroExcel;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * El panel en un libro de Excel, con las mismas cifras y tablas que se ven en pantalla.
 */
@Component
public class TableroEnExcel {

    public byte[] de(Tablero tablero) {
        try (LibroExcel libro = new LibroExcel()) {
            libro.hoja("Panel")
                    .titulo("KOLAB al " + Tiempo.fecha(LocalDateTime.now()))
                    .numero("Cuentas registradas", tablero.cuentas())
                    .numero("Solicitudes abiertas", tablero.solicitudesAbiertas())
                    .numero("Servicios cerrados", tablero.serviciosCerrados())
                    .soles("Comisión del mes", tablero.comisionDelMes())
                    .tabla("Últimos " + tablero.meses().size() + " meses",
                            List.of(texto("Mes"), numero("Solicitudes"), numero("Servicios cerrados"), soles("Comisión")),
                            tablero.meses().stream()
                                    .<List<?>>map(m -> List.of(m.nombre(), m.solicitudes(), m.serviciosCerrados(), m.comision()))
                                    .toList())
                    .tabla("Categorías más pedidas", List.of(texto("Categoría"), numero("Solicitudes")),
                            tablero.categorias().stream()
                                    .<List<?>>map(c -> List.of(c.nombre(), c.solicitudes()))
                                    .toList())
                    .ajustar();
            return libro.bytes();
        }
    }
}
