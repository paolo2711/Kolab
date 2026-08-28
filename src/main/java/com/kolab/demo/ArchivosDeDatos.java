package com.kolab.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kolab.perfil.PerfilResumen;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

// los datos viven en resources/datos/*.json y se leen una vez al arrancar. no hay nada quemado en
// el código: para cambiar un precio o agregar una solicitud se edita el json y ya.
// esta clase solo carga y busca por id. lo que cada pantalla necesita lo arman los lectores de al
// lado, uno por dominio. cuando entre la base de verdad, cada lector cambia de fuente y nada más.
@Component
public class ArchivosDeDatos {

    private final ObjectMapper json;

    private List<CategoriaJson> categorias;
    private List<SolicitudJson> solicitudes;
    private List<OfertaJson> ofertas;
    private List<MensajeJson> mensajes;
    private List<ServicioJson> servicios;
    private List<PersonaJson> personas;
    private Map<String, PerfilResumen> perfiles;
    private PlataformaJson plataforma;

    public ArchivosDeDatos(ObjectMapper json) {
        this.json = json;
    }

    @PostConstruct
    void cargar() throws IOException {
        categorias = leer("categorias.json", CategoriaJson.class);
        solicitudes = leer("solicitudes.json", SolicitudJson.class);
        ofertas = leer("ofertas.json", OfertaJson.class);
        mensajes = leer("mensajes.json", MensajeJson.class);
        servicios = leer("servicios.json", ServicioJson.class);
        personas = leer("personas.json", PersonaJson.class);
        perfiles = leerMapa("perfiles.json");
        plataforma = leerUno("plataforma.json", PlataformaJson.class);
    }

    List<CategoriaJson> categorias() {
        return categorias;
    }

    List<SolicitudJson> solicitudes() {
        return solicitudes;
    }

    List<OfertaJson> ofertas() {
        return ofertas;
    }

    List<MensajeJson> mensajes() {
        return mensajes;
    }

    List<ServicioJson> servicios() {
        return servicios;
    }

    List<PersonaJson> personas() {
        return personas;
    }

    public PlataformaJson plataforma() {
        return plataforma;
    }

    public PerfilResumen perfilDeCliente() {
        return perfiles.get("cliente");
    }

    public PerfilResumen perfilDeExperto() {
        return perfiles.get("experto");
    }

    public PerfilResumen perfilDe(boolean experto) {
        return experto ? perfilDeExperto() : perfilDeCliente();
    }

    CategoriaJson categoria(Long id) {
        return uno(categorias, c -> c.id().equals(id), "categoría", id);
    }

    SolicitudJson solicitud(Long id) {
        return uno(solicitudes, s -> s.id().equals(id), "solicitud", id);
    }

    OfertaJson oferta(Long id) {
        return uno(ofertas, o -> o.id().equals(id), "oferta", id);
    }

    PersonaJson persona(Long id) {
        return uno(personas, p -> p.id().equals(id), "persona", id);
    }

    private <T> T uno(List<T> lista, java.util.function.Predicate<T> comoEs, String que, Long id) {
        return lista.stream()
                .filter(comoEs)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No hay " + que + " con id " + id));
    }

    private <T> List<T> leer(String archivo, Class<T> tipo) throws IOException {
        try (InputStream entrada = abrir(archivo)) {
            return json.readValue(entrada, json.getTypeFactory().constructCollectionType(List.class, tipo));
        }
    }

    private <T> T leerUno(String archivo, Class<T> tipo) throws IOException {
        try (InputStream entrada = abrir(archivo)) {
            return json.readValue(entrada, tipo);
        }
    }

    private Map<String, PerfilResumen> leerMapa(String archivo) throws IOException {
        try (InputStream entrada = abrir(archivo)) {
            return json.readValue(entrada,
                    json.getTypeFactory().constructMapType(Map.class, String.class, PerfilResumen.class));
        }
    }

    private InputStream abrir(String archivo) throws IOException {
        return new ClassPathResource("datos/" + archivo).getInputStream();
    }
}
