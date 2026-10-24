package com.kolab.reporte;

import static org.assertj.core.api.Assertions.assertThat;

import com.kolab.reporte.MiResumen.Barra;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.List;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class ResumenEnExcelTest {

    private final ResumenEnExcel enExcel = new ResumenEnExcel();

    @Test
    void elLibroTraeUnaHojaPorCadaLadoDeLaCuenta() throws Exception {
        try (Workbook libro = abrir(enExcel.de(unResumen(), "Luis Mendoza"))) {
            assertThat(libro.getNumberOfSheets()).isEqualTo(2);
            assertThat(libro.getSheetName(0)).isEqualTo("Lo que ofrezco");
            assertThat(libro.getSheetName(1)).isEqualTo("Lo que pido");
        }
    }

    @Test
    void losMontosVanComoNumeroYNoComoTexto() throws Exception {
        try (Workbook libro = abrir(enExcel.de(unResumen(), "Luis Mendoza"))) {
            Sheet ofrezco = libro.getSheet("Lo que ofrezco");

            assertThat(valorDe(ofrezco, "Ganado")).isEqualTo(320.50);
            assertThat(tipoDe(ofrezco, "Ganado")).isEqualTo(CellType.NUMERIC);
            assertThat(tipoDe(libro.getSheet("Lo que pido"), "Pagado")).isEqualTo(CellType.NUMERIC);
        }
    }

    @Test
    void elDetallePorCategoriaEntraConSusFilas() throws Exception {
        try (Workbook libro = abrir(enExcel.de(unResumen(), "Luis Mendoza"))) {
            assertThat(valorDe(libro.getSheet("Lo que ofrezco"), "Idiomas")).isEqualTo(200.00);
            assertThat(valorDe(libro.getSheet("Lo que pido"), "Hogar y reparaciones")).isEqualTo(160.00);
        }
    }

    private Workbook abrir(byte[] bytes) throws Exception {
        return new XSSFWorkbook(new ByteArrayInputStream(bytes));
    }

    private double valorDe(Sheet hoja, String rotulo) {
        return buscar(hoja, rotulo).getCell(1).getNumericCellValue();
    }

    private CellType tipoDe(Sheet hoja, String rotulo) {
        return buscar(hoja, rotulo).getCell(1).getCellType();
    }

    private org.apache.poi.ss.usermodel.Row buscar(Sheet hoja, String rotulo) {
        for (org.apache.poi.ss.usermodel.Row fila : hoja) {
            if (fila.getCell(0) != null && rotulo.equals(fila.getCell(0).getStringCellValue())) {
                return fila;
            }
        }
        throw new AssertionError("no hay una fila que diga " + rotulo + " en " + hoja.getSheetName());
    }

    private MiResumen unResumen() {
        MiResumen.LoQueOfrezco ofrezco = new MiResumen.LoQueOfrezco(10, 4, new BigDecimal("320.50"),
                3, 1, new BigDecimal("5.00"),
                List.of(new Barra("Setiembre", new BigDecimal("120.50"), 60)),
                List.of(new Barra("Idiomas", new BigDecimal("200.00"), 100)));
        MiResumen.LoQuePido pido = new MiResumen.LoQuePido(5, 3, new BigDecimal("160.00"),
                new BigDecimal("-3.33"), 4.0,
                List.of(new Barra("Hogar y reparaciones", new BigDecimal("160.00"), 100)));
        return new MiResumen(new MiResumen.Pendientes(2, 1, 0), pido, ofrezco);
    }
}
