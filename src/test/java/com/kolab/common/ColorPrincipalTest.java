package com.kolab.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

class ColorPrincipalTest {

    @Test
    void ganaElColorQueMasOcupa() {
        BufferedImage foto = lienzo(new Color(200, 30, 30));
        pintar(foto, new Color(20, 60, 200), 0, 0, 40, 100);

        assertThat(ColorPrincipal.de(foto)).isEqualTo("#c81e1e");
    }

    @Test
    void elFondoBlancoONegroNoCuenta() {
        BufferedImage foto = lienzo(Color.WHITE);
        pintar(foto, Color.BLACK, 0, 0, 100, 30);
        pintar(foto, new Color(30, 140, 70), 40, 40, 20, 20);

        assertThat(ColorPrincipal.de(foto)).isEqualTo("#1e8c46");
    }

    @Test
    void unaFotoSoloDeFondoDaUnGrisNeutro() {
        assertThat(ColorPrincipal.de(lienzo(Color.WHITE))).isEqualTo("#808080");
    }

    @Test
    void paraLaFranjaSeAclaraConBlanco() {
        assertThat(ColorPrincipal.paraFranja("#000000")).isEqualTo("#8c8c8c");
        assertThat(ColorPrincipal.paraFranja("#ffffff")).isEqualTo("#ffffff");
        assertThat(ColorPrincipal.paraFranja("#c81e1e")).isEqualTo("#e69a9a");
    }

    private static BufferedImage lienzo(Color fondo) {
        BufferedImage imagen = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        pintar(imagen, fondo, 0, 0, 100, 100);
        return imagen;
    }

    private static void pintar(BufferedImage imagen, Color color, int x, int y, int ancho, int alto) {
        Graphics2D g = imagen.createGraphics();
        g.setColor(color);
        g.fillRect(x, y, ancho, alto);
        g.dispose();
    }
}
