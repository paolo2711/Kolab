package com.kolab.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FotosTest {

    @Test
    void elNombreDeLaCategoriaSeVuelveNombreDeArchivo() {
        assertThat(Fotos.aNombreDeArchivo("Hogar y reparaciones")).isEqualTo("hogar-y-reparaciones");
        assertThat(Fotos.aNombreDeArchivo("Diseño")).isEqualTo("diseno");
        assertThat(Fotos.aNombreDeArchivo("Clases escolares")).isEqualTo("clases-escolares");
    }

    @Test
    void losSignosYLosEspaciosDeMasNoDejanGuionesSueltos() {
        assertThat(Fotos.aNombreDeArchivo("  Música  ")).isEqualTo("musica");
        assertThat(Fotos.aNombreDeArchivo("Belleza & cuidado")).isEqualTo("belleza-cuidado");
        assertThat(Fotos.aNombreDeArchivo("¡Tecnología!")).isEqualTo("tecnologia");
    }
}
