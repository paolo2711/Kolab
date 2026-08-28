package com.kolab.common;

// las fotos son de relleno hasta que haya material propio. se piden por tema y en el tamaño exacto
// en que se van a ver, porque la misma imagen sirve de miniatura, de mosaico y de banner
public final class Foto {

    private static final String CATALOGO = "https://loremflickr.com/%d/%d/%s?lock=%d";
    private static final String RETRATO = "https://i.pravatar.cc/%d?img=%d";

    private Foto() {
    }

    public static String de(String tema, int ancho, int alto) {
        return String.format(CATALOGO, ancho, alto, tema, cerrojo(tema));
    }

    public static String retrato(int cara, int lado) {
        return String.format(RETRATO, lado, cara);
    }

    // sin el cerrojo el servicio devuelve una foto distinta en cada recarga
    private static int cerrojo(String tema) {
        return Math.abs(tema.hashCode() % 1000);
    }
}
