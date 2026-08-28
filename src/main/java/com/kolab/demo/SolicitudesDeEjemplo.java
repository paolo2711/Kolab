package com.kolab.demo;

import com.kolab.common.Distancia;
import com.kolab.solicitud.EstadoSolicitud;
import com.kolab.solicitud.MiSolicitud;
import com.kolab.solicitud.SolicitudDetalle;
import com.kolab.solicitud.SolicitudResumen;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SolicitudesDeEjemplo {

    private final ArchivosDeDatos archivos;
    private final CatalogoDeCategorias categorias;
    private final OfertasDeEjemplo ofertas;
    private final DirectorioDePersonas directorio;

    public SolicitudesDeEjemplo(ArchivosDeDatos archivos, CatalogoDeCategorias categorias,
                                OfertasDeEjemplo ofertas, DirectorioDePersonas directorio) {
        this.archivos = archivos;
        this.categorias = categorias;
        this.ofertas = ofertas;
        this.directorio = directorio;
    }

    // las abiertas que puedo ofertar: las mías no cuentan
    public List<SolicitudResumen> catalogo() {
        return abiertas().map(this::aResumen).toList();
    }

    public List<SolicitudResumen> abiertasDe(Long idCategoria) {
        return catalogo().stream()
                .filter(s -> s.idCategoria().equals(idCategoria))
                .toList();
    }

    public List<MiSolicitud> misSolicitudes() {
        return archivos.solicitudes().stream()
                .filter(SolicitudJson::mia)
                .map(this::aMiSolicitud)
                .toList();
    }

    public SolicitudDetalle solicitud(Long id) {
        SolicitudJson s = archivos.solicitud(id);
        return new SolicitudDetalle(s.id(), s.titulo(), s.descripcion(), categorias.nombreDe(s.categoria()),
                s.modalidad(), s.distrito(), s.fechaDeseada(), s.precio(), s.publicada(), s.estado(),
                ofertas.ofertasDe(s.id()));
    }

    // la distancia se calcula una sola vez y viaja dentro de la solicitud, para que todas las
    // pantallas puedan mostrarla sin repetir lógica
    public List<SolicitudResumen> conDistancia(List<SolicitudResumen> lista, Double lat, Double lon) {
        if (lat == null || lon == null) {
            return lista;
        }
        return lista.stream()
                .map(s -> s.enElMapa() ? s.a(Distancia.entre(lat, lon, s.latitud(), s.longitud())) : s)
                .toList();
    }

    public List<SolicitudResumen> cercaDe(Double lat, Double lon, int cuantas) {
        return conDistancia(catalogo(), lat, lon).stream()
                .filter(SolicitudResumen::sabemosLaDistancia)
                .sorted(Comparator.comparingDouble(SolicitudResumen::kilometros))
                .limit(cuantas)
                .toList();
    }

    public List<String> distritos() {
        return abiertas()
                .map(SolicitudJson::distrito)
                .filter(d -> d != null && !d.isBlank())
                .distinct()
                .sorted()
                .toList();
    }

    private java.util.stream.Stream<SolicitudJson> abiertas() {
        return archivos.solicitudes().stream()
                .filter(s -> !s.mia() && s.estado() == EstadoSolicitud.ABIERTA);
    }

    private SolicitudResumen aResumen(SolicitudJson s) {
        List<OfertaJson> suyas = ofertas.crudasDe(s.id());
        CategoriaJson c = archivos.categoria(s.categoria());
        return new SolicitudResumen(s.id(), s.titulo(), s.categoria(), c.nombre(), c.icono(), c.tema(),
                s.modalidad(), s.distrito(), s.precio(), ofertas.montoMenor(suyas),
                suyas.stream().map(o -> directorio.persona(o.persona())).toList(), s.publicada(),
                s.latitud(), s.longitud(), null);
    }

    private MiSolicitud aMiSolicitud(SolicitudJson s) {
        List<OfertaJson> suyas = ofertas.crudasDe(s.id());
        int sinLeer = suyas.stream().mapToInt(OfertaJson::sinLeer).sum();
        CategoriaJson c = archivos.categoria(s.categoria());
        return new MiSolicitud(s.id(), s.titulo(), c.nombre(), c.icono(), c.tema(), s.modalidad(),
                s.distrito(), s.precio(), suyas.size(), ofertas.montoMenor(suyas), sinLeer,
                s.estado(), s.publicada());
    }
}
