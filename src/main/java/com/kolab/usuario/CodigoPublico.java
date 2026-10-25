package com.kolab.usuario;

import java.security.SecureRandom;
import java.util.regex.Pattern;

/**
 * codigo con que se ve un perfil desde afuera, el id dejaba recorrer las cuentas una por una
 * 10 caracteres al azar con SecureRandom, no sale del nombre y no cambia nunca
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
