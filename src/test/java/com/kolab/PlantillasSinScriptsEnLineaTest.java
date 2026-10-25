package com.kolab;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

// script-src no admite codigo escrito en la plantilla, ni en etiquetas ni en atributos onclick,
// onsubmit y demas: el navegador lo ignora sin avisar y la pantalla queda a medias. todo va en static/js
class PlantillasSinScriptsEnLineaTest {

    private static final Path PLANTILLAS = Path.of("src/main/resources/templates");
    private static final Path ESTATICOS = Path.of("src/main/resources/static");

    private static final Pattern ETIQUETA_SCRIPT = Pattern.compile("<script\\b[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern CON_ARCHIVO = Pattern.compile("\\b(?:th:)?src\\s*=");
    // onclick=, th:onclick=, data-th-onsubmit=...
    private static final Pattern MANEJADOR = Pattern.compile("(?<![\\w-])(?:th:|data-th-)?on[a-z]+\\s*=\"");
    private static final Pattern ARCHIVO_PROPIO = Pattern.compile("th:src=\"@\\{(/js/[^}]+)}\"");

    @Test
    void ningunScriptVieneEscritoEnLaPlantilla() throws IOException {
        List<String> enLinea = new ArrayList<>();
        recorrer((lugar, linea) -> {
            Matcher m = ETIQUETA_SCRIPT.matcher(linea);
            while (m.find()) {
                if (!CON_ARCHIVO.matcher(m.group()).find()) {
                    enLinea.add(lugar);
                }
            }
        });
        assertThat(enLinea).as("scripts escritos en la plantilla").isEmpty();
    }

    @Test
    void ningunAtributoOnEnLasPlantillas() throws IOException {
        List<String> conManejador = new ArrayList<>();
        recorrer((lugar, linea) -> {
            if (MANEJADOR.matcher(linea).find()) {
                conManejador.add(lugar);
            }
        });
        assertThat(conManejador).as("atributos on* en la plantilla").isEmpty();
    }

    @Test
    void cadaScriptPropioExisteEnStaticJs() throws IOException {
        List<String> faltan = new ArrayList<>();
        recorrer((lugar, linea) -> {
            Matcher m = ARCHIVO_PROPIO.matcher(linea);
            while (m.find()) {
                if (!Files.exists(ESTATICOS.resolve(m.group(1).substring(1)))) {
                    faltan.add(m.group(1) + " (" + lugar + ")");
                }
            }
        });
        assertThat(faltan).as("scripts que la plantilla pide y no existen").isEmpty();
    }

    private interface PorLinea {
        void revisar(String lugar, String linea);
    }

    private static void recorrer(PorLinea porLinea) throws IOException {
        try (Stream<Path> archivos = Files.walk(PLANTILLAS)) {
            for (Path archivo : archivos.filter(a -> a.toString().endsWith(".html")).toList()) {
                List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
                for (int i = 0; i < lineas.size(); i++) {
                    porLinea.revisar(PLANTILLAS.relativize(archivo) + ":" + (i + 1) + "  " + lineas.get(i).trim(),
                            lineas.get(i));
                }
            }
        }
    }
}
