package com.kolab.common;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Convierte fechas en la forma en que se leen en pantalla: «hace 2 horas», «ayer», «agosto de 2026».
 */
public final class Tiempo {

    private static final Locale PERU = Locale.forLanguageTag("es-PE");
    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter MES = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", PERU);
    private static final DateTimeFormatter DIA_LARGO = DateTimeFormatter.ofPattern("d 'de' MMMM", PERU);

    private Tiempo() {
    }

    /**
     * Cuánto pasó desde {@code cuando}, en palabras. Pasado un mes se muestra la fecha.
     */
    public static String hace(LocalDateTime cuando) {
        long minutos = Duration.between(cuando, LocalDateTime.now()).toMinutes();
        if (minutos < 1) {
            return "recién";
        }
        if (minutos < 60) {
            return minutos == 1 ? "hace 1 minuto" : "hace " + minutos + " minutos";
        }
        long horas = minutos / 60;
        if (horas < 24) {
            return horas == 1 ? "hace 1 hora" : "hace " + horas + " horas";
        }
        long dias = horas / 24;
        if (dias == 1) {
            return "ayer";
        }
        if (dias < 7) {
            return "hace " + dias + " días";
        }
        if (dias < 30) {
            long semanas = dias / 7;
            return semanas == 1 ? "hace 1 semana" : "hace " + semanas + " semanas";
        }
        return "el " + cuando.format(DIA);
    }

    /**
     * El día al que pertenece un mensaje, para separar la conversación por jornadas.
     */
    public static String dia(LocalDateTime cuando) {
        LocalDate fecha = cuando.toLocalDate();
        LocalDate hoy = LocalDate.now();
        if (fecha.equals(hoy)) {
            return "Hoy";
        }
        if (fecha.equals(hoy.minusDays(1))) {
            return "Ayer";
        }
        return cuando.format(fecha.getYear() == hoy.getYear() ? DIA_LARGO : DIA);
    }

    /**
     * Solo la hora. En la conversación el día lo dice el separador, así que la burbuja no lo repite.
     */
    public static String minuto(LocalDateTime cuando) {
        return cuando.format(HORA);
    }

    public static String fecha(LocalDateTime cuando) {
        return cuando.format(DIA);
    }

    public static String mesYAnio(LocalDateTime cuando) {
        return cuando.format(MES);
    }
}
