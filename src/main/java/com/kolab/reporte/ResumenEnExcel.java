package com.kolab.reporte;

import static com.kolab.reporte.LibroExcel.Columna.soles;
import static com.kolab.reporte.LibroExcel.Columna.texto;

import com.kolab.reporte.MiResumen.Barra;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Pasa Mi resumen a un libro de Excel, una hoja por cada lado de la cuenta.
 */
@Component
public class ResumenEnExcel {

    public byte[] de(MiResumen resumen, String nombreDePersona) {
        try (LibroExcel libro = new LibroExcel()) {
            loQueOfrezco(libro.hoja("Lo que ofrezco"), resumen.ofrezco(), nombreDePersona);
            loQuePido(libro.hoja("Lo que pido"), resumen.pido(), nombreDePersona);
            return libro.bytes();
        }
    }

    private void loQueOfrezco(LibroExcel.Hoja hoja, MiResumen.LoQueOfrezco ofrezco, String persona) {
        hoja.titulo("Lo que ofrezco · " + persona)
                .soles("Ganado", ofrezco.ganado())
                .numero("Ofertas enviadas", ofrezco.ofertasEnviadas())
                .numero("Ofertas aceptadas", ofrezco.ofertasAceptadas())
                .numero("Ofertas que terminaron en trato (%)", ofrezco.getTasaDeAceptacion())
                .numero("Clientes", ofrezco.clientes())
                .numero("Clientes que volvieron", ofrezco.clientesQueVolvieron())
                .soles("Frente a lo que piden", ofrezco.diferenciaConLoPedido())
                .tabla("Ganado mes a mes", List.of(texto("Mes"), soles("Monto")), filas(ofrezco.ganadoPorMes()))
                .tabla("De dónde viene lo que ganas", List.of(texto("Categoría"), soles("Monto")),
                        filas(ofrezco.ganadoPorCategoria()))
                .ajustar();
    }

    private void loQuePido(LibroExcel.Hoja hoja, MiResumen.LoQuePido pido, String persona) {
        hoja.titulo("Lo que pido · " + persona)
                .soles("Pagado", pido.pagado())
                .numero("Solicitudes publicadas", pido.publicadas())
                .numero("Solicitudes con trato", pido.conTrato())
                .numero("Solicitudes que terminaron en trato (%)", pido.getTasaDeTrato())
                .soles("Frente a tu precio", pido.diferenciaConLoPropuesto())
                .texto("Lo que tarda la primera oferta", pido.getEsperaPrimeraOferta())
                .tabla("En qué gastas", List.of(texto("Categoría"), soles("Monto")), filas(pido.pagadoPorCategoria()))
                .ajustar();
    }

    private static List<List<?>> filas(List<Barra> barras) {
        return barras == null ? List.of()
                : barras.stream().<List<?>>map(b -> Arrays.asList(b.rotulo(), b.valor())).toList();
    }
}
