package com.kolab.demo;

import com.kolab.perfil.ExpertoResumen;
import com.kolab.perfil.Persona;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

// la gente de la plataforma es una sola lista: clientes y expertos son los mismos. quien sale como
// experto es quien declaró categorías en las que trabaja.
@Component
public class DirectorioDePersonas {

    private final ArchivosDeDatos archivos;

    public DirectorioDePersonas(ArchivosDeDatos archivos) {
        this.archivos = archivos;
    }

    public Persona persona(Long id) {
        return aPersona(archivos.persona(id));
    }

    // si la solicitud es mía yo soy el cliente y del otro lado está quien ofertó; si no, al revés
    Persona contraparte(SolicitudJson s, OfertaJson o) {
        return persona(s.mia() ? o.persona() : s.persona());
    }

    // los mejor calificados de la categoría, que es lo que pide el RF-09 del informe
    public List<ExpertoResumen> expertosDe(Long idCategoria) {
        return ordenados(archivos.personas().stream()
                .filter(p -> p.categorias().contains(idCategoria)));
    }

    public List<ExpertoResumen> mejoresExpertos(int cuantos) {
        return ordenados(archivos.personas().stream()).stream().limit(cuantos).toList();
    }

    private List<ExpertoResumen> ordenados(Stream<PersonaJson> flujo) {
        return flujo
                .sorted(Comparator.comparing(PersonaJson::calificacion).reversed()
                        .thenComparing(Comparator.comparingInt(PersonaJson::servicios).reversed()))
                .map(p -> new ExpertoResumen(aPersona(p), p.distrito(), p.calificacion(),
                        p.servicios(), p.titular(), p.ultimaResena(), p.verificado()))
                .toList();
    }

    private Persona aPersona(PersonaJson p) {
        return new Persona(p.id(), p.nombre(), p.iniciales(), p.cara());
    }
}
