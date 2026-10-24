package com.kolab.reporte;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * La respuesta que hace que el navegador baje un libro de Excel con el nombre dado.
 */
public final class DescargaExcel {

    private static final MediaType EXCEL =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private DescargaExcel() {
    }

    public static ResponseEntity<byte[]> de(String archivo, byte[] libro) {
        return ResponseEntity.ok()
                .contentType(EXCEL)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(archivo).build().toString())
                .body(libro);
    }
}
