package com.kolab.desarrollo;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Lee los datos de ejemplo de {@code resources/ejemplos}. Cada archivo apunta a los demás por
 * correo, por nombre de categoría o por una clave propia, nunca por id: los ids los pone la base.
 */
@Component
class Ejemplos {

    record Persona(String nombre, String apellidos, String email, String telefono, String distrito,
                   BigDecimal latitud, BigDecimal longitud, int mesesEnKolab, String descripcion,
                   String experiencia, List<String> categorias) {
    }

    record Solicitud(String clave, String autor, String para, String categoria, String titulo,
                     String descripcion, String modalidad, String distrito, BigDecimal latitud,
                     BigDecimal longitud, BigDecimal precio, int dentroDeDias, int haceHoras) {
    }

    record Oferta(String clave, String solicitud, String autor, BigDecimal monto, String mensaje,
                  int haceHoras, boolean rechazada) {
    }

    record Nota(int puntaje, String comentario) {
    }

    record Trato(String oferta, int haceHoras, Integer cerradoHaceHoras, Nota calificaQuienPide,
                 Nota calificaQuienOfrece) {
    }

    record Mensaje(String oferta, boolean deQuienPide, String contenido, int haceMinutos, boolean leido) {
    }

    private final ObjectMapper json;

    Ejemplos(ObjectMapper json) {
        this.json = json;
    }

    List<Persona> personas() {
        return leer("personas.json", Persona.class);
    }

    List<Solicitud> solicitudes() {
        return leer("solicitudes.json", Solicitud.class);
    }

    List<Oferta> ofertas() {
        return leer("ofertas.json", Oferta.class);
    }

    List<Trato> tratos() {
        return leer("tratos.json", Trato.class);
    }

    List<Mensaje> mensajes() {
        return leer("mensajes.json", Mensaje.class);
    }

    private <T> List<T> leer(String archivo, Class<T> tipo) {
        try (InputStream entrada = new ClassPathResource("ejemplos/" + archivo).getInputStream()) {
            return json.readValue(entrada, json.getTypeFactory().constructCollectionType(List.class, tipo));
        } catch (IOException e) {
            throw new UncheckedIOException("No pude leer ejemplos/" + archivo, e);
        }
    }
}
