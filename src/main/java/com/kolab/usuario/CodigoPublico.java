package com.kolab.usuario;

import java.security.SecureRandom;
import java.util.regex.Pattern;

/**
 * El código con que se ve el perfil de alguien desde afuera, en vez del id de la base: con el id
 * cualquiera podía recorrer los perfiles de uno en uno y saber cuántas cuentas hay. Son 10
 * caracteres al azar sacados de {@link SecureRandom}, no salen del nombre y no cambian nunca.
 */
public final class CodigoPublico {

    public static final int LARGO = 10;
    public static final Pattern FORMA = Pattern.compile("[a-z0-9]{" + LARGO + "}");

    private static final String LETRAS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom AZAR = new SecureRandom();

    private CodigoPublico() {
    }

    public static String nuevo() {
        StringBuilder codigo = new StringBuilder(LARGO);
        for (int i = 0; i < LARGO; i++) {
            codigo.append(LETRAS.charAt(AZAR.nextInt(LETRAS.length())));
        }
        return codigo.toString();
    }
}
