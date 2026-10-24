package com.kolab.reporte;

import com.kolab.reporte.MiResumen.Barra;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

/**
 * Pasa Mi resumen a un libro de Excel, una hoja por cada lado de la cuenta.
 *
 * <p>Los montos van como número con formato de soles, no como texto: así quien reciba el archivo
 * puede sumarlos o graficarlos sin tener que limpiarlos antes.
 */
@Component
public class ResumenEnExcel {

    private static final String SOLES = "\"S/\" #,##0.00";

    public byte[] de(MiResumen resumen, String nombreDePersona) {
        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Estilos estilos = new Estilos(libro);
            hojaDeLoQueOfrezco(libro, estilos, resumen.ofrezco(), nombreDePersona);
            hojaDeLoQuePido(libro, estilos, resumen.pido(), nombreDePersona);
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void hojaDeLoQueOfrezco(Workbook libro, Estilos estilos, MiResumen.LoQueOfrezco ofrezco,
                                    String persona) {
        Sheet hoja = libro.createSheet("Lo que ofrezco");
        Cursor cursor = new Cursor(hoja, estilos);
        cursor.titulo("Lo que ofrezco · " + persona);

        cursor.cifra("Ganado", ofrezco.ganado());
        cursor.numero("Ofertas enviadas", ofrezco.ofertasEnviadas());
        cursor.numero("Ofertas aceptadas", ofrezco.ofertasAceptadas());
        cursor.numero("Ofertas que terminaron en trato (%)", ofrezco.getTasaDeAceptacion());
        cursor.numero("Clientes", ofrezco.clientes());
        cursor.numero("Clientes que volvieron", ofrezco.clientesQueVolvieron());
        cursor.cifra("Frente a lo que piden", ofrezco.diferenciaConLoPedido());

        cursor.tabla("Ganado mes a mes", "Mes", ofrezco.ganadoPorMes());
        cursor.tabla("De dónde viene lo que ganas", "Categoría", ofrezco.ganadoPorCategoria());
        cursor.ajustar();
    }

    private void hojaDeLoQuePido(Workbook libro, Estilos estilos, MiResumen.LoQuePido pido, String persona) {
        Sheet hoja = libro.createSheet("Lo que pido");
        Cursor cursor = new Cursor(hoja, estilos);
        cursor.titulo("Lo que pido · " + persona);

        cursor.cifra("Pagado", pido.pagado());
        cursor.numero("Solicitudes publicadas", pido.publicadas());
        cursor.numero("Solicitudes con trato", pido.conTrato());
        cursor.numero("Solicitudes que terminaron en trato (%)", pido.getTasaDeTrato());
        cursor.cifra("Frente a tu precio", pido.diferenciaConLoPropuesto());
        cursor.texto("Lo que tarda la primera oferta", pido.getEsperaPrimeraOferta());

        cursor.tabla("En qué gastas", "Categoría", pido.pagadoPorCategoria());
        cursor.ajustar();
    }

    /** Los estilos se crean una vez por libro: Excel tiene un tope y recrearlos lo revienta. */
    private static final class Estilos {

        private final CellStyle titulo;
        private final CellStyle encabezado;
        private final CellStyle soles;

        private Estilos(Workbook libro) {
            Font negrita = libro.createFont();
            negrita.setBold(true);

            this.titulo = libro.createCellStyle();
            Font grande = libro.createFont();
            grande.setBold(true);
            grande.setFontHeightInPoints((short) 14);
            this.titulo.setFont(grande);

            this.encabezado = libro.createCellStyle();
            this.encabezado.setFont(negrita);

            this.soles = libro.createCellStyle();
            this.soles.setDataFormat(libro.createDataFormat().getFormat(SOLES));
        }
    }

    /** Va bajando por la hoja; así cada bloque no tiene que saber en qué fila quedó el anterior. */
    private static final class Cursor {

        private final Sheet hoja;
        private final Estilos estilos;
        private int fila;

        private Cursor(Sheet hoja, Estilos estilos) {
            this.hoja = hoja;
            this.estilos = estilos;
        }

        private void titulo(String texto) {
            celdaDeTexto(siguiente(), 0, texto, estilos.titulo);
            fila++;
        }

        private void cifra(String rotulo, BigDecimal valor) {
            Row r = siguiente();
            celdaDeTexto(r, 0, rotulo, estilos.encabezado);
            Cell celda = r.createCell(1);
            celda.setCellValue(valor == null ? 0 : valor.doubleValue());
            celda.setCellStyle(estilos.soles);
        }

        private void numero(String rotulo, long valor) {
            Row r = siguiente();
            celdaDeTexto(r, 0, rotulo, estilos.encabezado);
            r.createCell(1).setCellValue(valor);
        }

        private void texto(String rotulo, String valor) {
            Row r = siguiente();
            celdaDeTexto(r, 0, rotulo, estilos.encabezado);
            r.createCell(1).setCellValue(valor);
        }

        private void tabla(String titulo, String queEsCadaFila, List<Barra> barras) {
            fila++;
            celdaDeTexto(siguiente(), 0, titulo, estilos.titulo);
            Row cabecera = siguiente();
            celdaDeTexto(cabecera, 0, queEsCadaFila, estilos.encabezado);
            celdaDeTexto(cabecera, 1, "Monto", estilos.encabezado);

            if (barras == null || barras.isEmpty()) {
                celdaDeTexto(siguiente(), 0, "Todavía no hay nada que mostrar", null);
                return;
            }
            for (Barra barra : barras) {
                Row r = siguiente();
                celdaDeTexto(r, 0, barra.rotulo(), null);
                Cell celda = r.createCell(1);
                celda.setCellValue(barra.valor() == null ? 0 : barra.valor().doubleValue());
                celda.setCellStyle(estilos.soles);
            }
        }

        private void ajustar() {
            hoja.setColumnWidth(0, 14000);
            hoja.setColumnWidth(1, 5000);
        }

        private Row siguiente() {
            return hoja.createRow(fila++);
        }

        private void celdaDeTexto(Row fila, int columna, String texto, CellStyle estilo) {
            Cell celda = fila.createCell(columna);
            celda.setCellValue(texto);
            if (estilo != null) {
                celda.setCellStyle(estilo);
            }
        }
    }
}
