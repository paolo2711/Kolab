package com.kolab.common;

/**
 * Arma la direccion de las fotos de ejemplo.
 *
 * <p>Los archivos viven en {@code static/img}, dentro del proyecto. Antes se pedian a un servicio
 * de imagenes de relleno y la aplicacion quedaba sin fotos cuando ese servicio fallaba o no habia
 * internet. Cuando haya material propio se reemplazan los archivos y no se toca codigo.
 */
public final class Foto {

    private static final String CATEGORIA = "/img/categoria/%s.jpg";
    private static final String PERSONA = "/img/persona/%d.jpg";

    private Foto() {
    }

    /**
     * La foto de una categoria. El tamano lo resuelve el CSS con {@code object-fit}, asi que un
     * mismo archivo sirve de miniatura, de mosaico y de banner.
     */
    public static String de(String tema) {
        return String.format(CATEGORIA, tema);
    }

    /**
     * El retrato de una persona. Quien no subio ninguno se muestra con sus iniciales, asi que
     * conviene preguntar antes por {@code tieneFoto()}.
     */
    public static String retrato(int cara) {
        return String.format(PERSONA, cara);
    }
}
