package com.kolab.administracion;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.List;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class TableroEnExcelTest {

    private final TableroEnExcel enExcel = new TableroEnExcel();

    @Test
    void elLibroTraeLasCifrasComoNumero() throws Exception {
        try (Workbook libro = abrir()) {
            Sheet hoja = libro.getSheet("Panel");

            assertThat(fila(hoja, "Cuentas registradas").getCell(1).getNumericCellValue()).isEqualTo(15);
            assertThat(fila(hoja, "Comisión del mes").getCell(1).getCellType()).isEqualTo(CellType.NUMERIC);
            assertThat(fila(hoja, "Comisión del mes").getCell(1).getNumericCellValue()).isEqualTo(8.50);
        }
    }

    @Test
    void cadaMesTraeSusTresCifras() throws Exception {
        try (Workbook libro = abrir()) {
            Row octubre = fila(libro.getSheet("Panel"), "octubre de 2026");

            assertThat(octubre.getCell(1).getNumericCellValue()).isEqualTo(5);
            assertThat(octubre.getCell(2).getNumericCellValue()).isEqualTo(2);
            assertThat(octubre.getCell(3).getNumericCellValue()).isEqualTo(8.50);
        }
    }

    @Test
    void lasCategoriasMasPedidasEntranConSuCantidad() throws Exception {
        try (Workbook libro = abrir()) {
            assertThat(fila(libro.getSheet("Panel"), "Idiomas").getCell(1).getNumericCellValue()).isEqualTo(8);
        }
    }

    private Workbook abrir() throws Exception {
        Tablero tablero = new Tablero(15, 12, 9, new BigDecimal("8.50"),
                List.of(new Tablero.Mes("setiembre de 2026", 3, 0, BigDecimal.ZERO),
                        new Tablero.Mes("octubre de 2026", 5, 2, new BigDecimal("8.50"))),
                List.of(new Tablero.CategoriaPedida("Idiomas", 8)));
        return new XSSFWorkbook(new ByteArrayInputStream(enExcel.de(tablero)));
    }

    private Row fila(Sheet hoja, String rotulo) {
        for (Row r : hoja) {
            if (r.getCell(0) != null && rotulo.equals(r.getCell(0).getStringCellValue())) {
                return r;
            }
        }
        throw new AssertionError("no hay una fila que diga " + rotulo);
    }
}
