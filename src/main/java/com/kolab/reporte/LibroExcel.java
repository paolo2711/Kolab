package com.kolab.reporte;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Un libro de Excel que se escribe de arriba hacia abajo: títulos, cifras sueltas y tablas. Los
 * montos van como número con formato de soles, así quien lo reciba puede sumarlos sin limpiarlos.
 */
public final class LibroExcel implements AutoCloseable {

    private static final String SOLES = "\"S/\" #,##0.00";
    private static final int ANCHO_ROTULO = 14000;
    private static final int ANCHO_DATO = 5000;

    private final Workbook libro = new XSSFWorkbook();
    private final CellStyle titulo;
    private final CellStyle encabezado;
    private final CellStyle soles;

    /** Cómo se escribe el valor de una columna. */
    public enum Formato { TEXTO, NUMERO, SOLES }

    public record Columna(String nombre, Formato formato) {

        public static Columna texto(String nombre) {
            return new Columna(nombre, Formato.TEXTO);
        }

        public static Columna numero(String nombre) {
            return new Columna(nombre, Formato.NUMERO);
        }

        public static Columna soles(String nombre) {
            return new Columna(nombre, Formato.SOLES);
        }
    }

    // los estilos se crean una vez por libro: Excel tiene un tope y recrearlos por celda lo revienta
    public LibroExcel() {
        Font negrita = libro.createFont();
        negrita.setBold(true);
        Font grande = libro.createFont();
        grande.setBold(true);
        grande.setFontHeightInPoints((short) 14);

        titulo = libro.createCellStyle();
        titulo.setFont(grande);
        encabezado = libro.createCellStyle();
        encabezado.setFont(negrita);
        soles = libro.createCellStyle();
        soles.setDataFormat(libro.createDataFormat().getFormat(SOLES));
    }

    public Hoja hoja(String nombre) {
        return new Hoja(libro.createSheet(nombre));
    }

    public byte[] bytes() {
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() {
        try {
            libro.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Una hoja que va bajando: cada bloque se escribe debajo del anterior sin saber en qué fila quedó.
     */
    public final class Hoja {

        private final Sheet hoja;
        private int fila;
        private int columnas = 2;

        private Hoja(Sheet hoja) {
            this.hoja = hoja;
        }

        public Hoja titulo(String texto) {
            escribir(siguiente(), 0, texto, Formato.TEXTO, titulo);
            fila++;
            return this;
        }

        public Hoja soles(String rotulo, Number valor) {
            return dato(rotulo, valor, Formato.SOLES);
        }

        public Hoja numero(String rotulo, Number valor) {
            return dato(rotulo, valor, Formato.NUMERO);
        }

        public Hoja texto(String rotulo, String valor) {
            return dato(rotulo, valor, Formato.TEXTO);
        }

        /**
         * Un título, la fila de encabezados y una fila por cada elemento de {@code filas}, con sus
         * valores en el orden de {@code columnas}. Sin filas deja una línea que lo dice.
         */
        public Hoja tabla(String nombre, List<Columna> columnas, List<List<?>> filas) {
            this.columnas = Math.max(this.columnas, columnas.size());
            fila++;
            escribir(siguiente(), 0, nombre, Formato.TEXTO, titulo);

            Row cabecera = siguiente();
            for (int i = 0; i < columnas.size(); i++) {
                escribir(cabecera, i, columnas.get(i).nombre(), Formato.TEXTO, encabezado);
            }
            if (filas.isEmpty()) {
                escribir(siguiente(), 0, "Todavía no hay nada que mostrar", Formato.TEXTO, null);
                return this;
            }
            for (List<?> valores : filas) {
                Row r = siguiente();
                for (int i = 0; i < columnas.size(); i++) {
                    Formato formato = columnas.get(i).formato();
                    escribir(r, i, valores.get(i), formato, formato == Formato.SOLES ? soles : null);
                }
            }
            return this;
        }

        public void ajustar() {
            hoja.setColumnWidth(0, ANCHO_ROTULO);
            for (int i = 1; i < columnas; i++) {
                hoja.setColumnWidth(i, ANCHO_DATO);
            }
        }

        private Hoja dato(String rotulo, Object valor, Formato formato) {
            Row r = siguiente();
            escribir(r, 0, rotulo, Formato.TEXTO, encabezado);
            escribir(r, 1, valor, formato, formato == Formato.SOLES ? soles : null);
            return this;
        }

        private Row siguiente() {
            return hoja.createRow(fila++);
        }

        private void escribir(Row r, int columna, Object valor, Formato formato, CellStyle estilo) {
            Cell celda = r.createCell(columna);
            if (formato == Formato.TEXTO) {
                celda.setCellValue(valor == null ? "" : valor.toString());
            } else {
                celda.setCellValue(valor == null ? 0 : ((Number) valor).doubleValue());
            }
            if (estilo != null) {
                celda.setCellStyle(estilo);
            }
        }
    }
}
