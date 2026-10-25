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

// la politica de contenido ya no admite estilos en linea: uno nuevo no rompe la compilacion, rompe
// la pantalla en silencio. esta prueba lo atrapa antes
class PlantillasSinEstilosEnLineaTest {

    private static final Path PLANTILLAS = Path.of("src/main/resources/templates");
    private static final Path CSS = Path.of("src/main/resources/static/css/kolab.css");

    // style=, th:style=, data-th-style= y th:attr="style=..."
    private static final Pattern ESTILO_EN_LINEA =
            Pattern.compile("(?<![\\w-])(?:th:|data-th-)?style\\s*=|th:attr\\s*=\\s*\"[^\"]*\\bstyle\\s*=");
    private static final Pattern BLOQUE_STYLE = Pattern.compile("<style[\\s>]", Pattern.CASE_INSENSITIVE);
    private static final Pattern TAMANO_DE_CARA = Pattern.compile(":: cara\\([^,]+,\\s*'([0-9.]+)rem'\\)");

    @Test
    void ningunaPlantillaTieneEstilosEnLinea() throws IOException {
        assertThat(encontrar(ESTILO_EN_LINEA)).as("estilos en linea").isEmpty();
    }

    @Test
    void ningunaPlantillaTieneBloquesStyle() throws IOException {
        assertThat(encontrar(BLOQUE_STYLE)).as("bloques <style>").isEmpty();
    }

    @Test
    void cadaTamanoDeCaraTieneSuClase() throws IOException {
        String css = Files.readString(CSS, StandardCharsets.UTF_8);
        List<String> sinClase = new ArrayList<>();
        for (String hallazgo : encontrar(TAMANO_DE_CARA)) {
            Matcher m = TAMANO_DE_CARA.matcher(hallazgo);
            m.find();
            String clase = ".k-cara-" + m.group(1).replace(".", "");
            if (!css.contains(clase + " ")) {
                sinClase.add(clase + " (" + hallazgo + ")");
            }
        }
        assertThat(sinClase).as("tamanos de cara sin su clase en kolab.css").isEmpty();
    }

    // cada linea que coincide, con el archivo y el numero de linea, para que el error diga donde
    private static List<String> encontrar(Pattern patron) throws IOException {
        List<String> hallazgos = new ArrayList<>();
        try (Stream<Path> archivos = Files.walk(PLANTILLAS)) {
            for (Path archivo : archivos.filter(a -> a.toString().endsWith(".html")).toList()) {
                List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
                for (int i = 0; i < lineas.size(); i++) {
                    if (patron.matcher(lineas.get(i)).find()) {
                        hallazgos.add(PLANTILLAS.relativize(archivo) + ":" + (i + 1) + "  " + lineas.get(i).trim());
                    }
                }
            }
        }
        return hallazgos;
    }
}
