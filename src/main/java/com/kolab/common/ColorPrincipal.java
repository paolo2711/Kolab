package com.kolab.common;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import javax.imageio.ImageIO;

/**
 * el color que mas pesa en una foto, para pintar la franja del perfil cuando no hay portada
 */
public final class ColorPrincipal {

    // la foto se mira en chico, de sobra para sacar un color y no recorre millones de pixeles
    private static final int LADO = 48;
    // cada canal se agrupa de a 32 tonos, si no cada pixel seria su propio color
    private static final int PASO = 32;
    // casi blanco y casi negro son fondo o sombra, no dicen de que color es la foto
    private static final int CLARO = 235;
    private static final int OSCURO = 20;
    // la franja es grande, va mezclada con blanco para que no quede un bloque oscuro
    private static final double BLANCO = 0.55;

    private ColorPrincipal() {
    }

    public static Optional<String> de(Path foto) {
        try {
            BufferedImage imagen = ImageIO.read(foto.toFile());
            return imagen == null ? Optional.empty() : Optional.of(de(imagen));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /**
     * el promedio del grupo de colores con mas pixeles, en #rrggbb
     */
    public static String de(BufferedImage imagen) {
        int grupos = 256 / PASO;
        long[][] sumas = new long[grupos * grupos * grupos][4];
        boolean contoAlguno = false;
        for (int x = 0; x < LADO; x++) {
            for (int y = 0; y < LADO; y++) {
                int argb = imagen.getRGB(x * imagen.getWidth() / LADO, y * imagen.getHeight() / LADO);
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                if (((argb >>> 24) < 128) || esFondo(r, g, b)) {
                    continue;
                }
                long[] grupo = sumas[(r / PASO * grupos + g / PASO) * grupos + b / PASO];
                grupo[0] += r;
                grupo[1] += g;
                grupo[2] += b;
                grupo[3]++;
                contoAlguno = true;
            }
        }
        if (!contoAlguno) {
            return "#808080";
        }
        long[] mayor = sumas[0];
        for (long[] grupo : sumas) {
            if (grupo[3] > mayor[3]) {
                mayor = grupo;
            }
        }
        return hex((int) (mayor[0] / mayor[3]), (int) (mayor[1] / mayor[3]), (int) (mayor[2] / mayor[3]));
    }

    /**
     * el mismo color mezclado con blanco, para la franja
     */
    public static String paraFranja(String color) {
        int rgb = Integer.parseInt(color.substring(1), 16);
        return hex(aclarar(rgb >> 16 & 0xFF), aclarar(rgb >> 8 & 0xFF), aclarar(rgb & 0xFF));
    }

    private static boolean esFondo(int r, int g, int b) {
        return (r > CLARO && g > CLARO && b > CLARO) || (r < OSCURO && g < OSCURO && b < OSCURO);
    }

    private static int aclarar(int canal) {
        return (int) Math.round(canal + (255 - canal) * BLANCO);
    }

    private static String hex(int r, int g, int b) {
        return String.format("#%02x%02x%02x", r, g, b);
    }
}
