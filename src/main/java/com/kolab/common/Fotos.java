package com.kolab.common;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * Encuentra la foto de una categoría o de una persona entre los archivos de {@code static/img}.
 *
 * <p>La foto se busca por nombre: la de una categoría se llama como la categoría
 * ({@code hogar-y-reparaciones.jpg}) y la de una persona como su correo antes de la arroba
 * ({@code luis.mendoza.jpg}). Así agregar una foto es copiar un archivo, sin tocar código ni tablas.
 * Si no hay archivo se devuelve {@code null} y la vista pinta el ícono o las iniciales.
 */
@Component
public class Fotos {

    private static final String CATEGORIAS = "/img/categoria/";
    private static final String PERSONAS = "/img/persona/";
    private static final String SUELTAS = "/img/";

    private final Set<String> deCategorias;
    private final Set<String> dePersonas;
    private final Set<String> sueltas;

    public Fotos(ResourcePatternResolver recursos) {
        this.deCategorias = nombresEn(recursos, CATEGORIAS);
        this.dePersonas = nombresEn(recursos, PERSONAS);
        this.sueltas = nombresEn(recursos, SUELTAS);
    }

    /**
     * Una foto que no es de nadie, como la de las pantallas de acceso ({@code img/acceso.jpg}).
     */
    public String suelta(String nombre) {
        String archivo = nombre + ".jpg";
        return sueltas.contains(archivo) ? SUELTAS + archivo : null;
    }

    public String deCategoria(String nombre) {
        String archivo = aNombreDeArchivo(nombre) + ".jpg";
        return deCategorias.contains(archivo) ? CATEGORIAS + archivo : null;
    }

    public String dePersona(String email) {
        String archivo = email.substring(0, email.indexOf('@')) + ".jpg";
        return dePersonas.contains(archivo) ? PERSONAS + archivo : null;
    }

    // "Hogar y reparaciones" pasa a "hogar-y-reparaciones"
    static String aNombreDeArchivo(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

    // se leen una vez al arrancar; con classpath* una carpeta que falta es solo una carpeta sin fotos
    private static Set<String> nombresEn(ResourcePatternResolver recursos, String carpeta) {
        try {
            Resource[] encontrados = recursos.getResources("classpath*:static" + carpeta + "*.jpg");
            return Arrays.stream(encontrados)
                    .map(Resource::getFilename)
                    .collect(Collectors.toUnmodifiableSet());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
