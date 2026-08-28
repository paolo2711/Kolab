package com.kolab.demo;

import com.kolab.oferta.OfertaEnviada;
import com.kolab.oferta.OfertaResumen;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class OfertasDeEjemplo {

    private final ArchivosDeDatos archivos;
    private final DirectorioDePersonas directorio;

    public OfertasDeEjemplo(ArchivosDeDatos archivos, DirectorioDePersonas directorio) {
        this.archivos = archivos;
        this.directorio = directorio;
    }

    public List<OfertaResumen> ofertasDe(Long idSolicitud) {
        return crudasDe(idSolicitud).stream()
                .map(this::aResumen)
                .toList();
    }

    // la oferta que yo mandé a esa solicitud, si es que ya oferté
    public Optional<OfertaResumen> miOfertaEn(Long idSolicitud) {
        return archivos.ofertas().stream()
                .filter(o -> o.solicitud().equals(idSolicitud) && o.mia())
                .findFirst()
                .map(this::aResumen);
    }

    public List<OfertaEnviada> misOfertas() {
        return archivos.ofertas().stream()
                .filter(OfertaJson::mia)
                .map(o -> {
                    SolicitudJson s = archivos.solicitud(o.solicitud());
                    CategoriaJson c = archivos.categoria(s.categoria());
                    return new OfertaEnviada(s.id(), s.titulo(), c.nombre(), c.icono(), c.tema(),
                            s.precio(), o.monto(), o.estado(), o.sinLeer(),
                            crudasDe(s.id()).size(), s.publicada());
                })
                .toList();
    }

    public int mensajesSinLeer() {
        return archivos.ofertas().stream().mapToInt(OfertaJson::sinLeer).sum();
    }

    // ordenadas por monto, para que la más barata sea siempre la primera
    List<OfertaJson> crudasDe(Long idSolicitud) {
        return archivos.ofertas().stream()
                .filter(o -> o.solicitud().equals(idSolicitud))
                .sorted(Comparator.comparing(OfertaJson::monto))
                .toList();
    }

    BigDecimal montoMenor(List<OfertaJson> lista) {
        return lista.isEmpty() ? null : lista.get(0).monto();
    }

    private OfertaResumen aResumen(OfertaJson o) {
        PersonaJson p = archivos.persona(o.persona());
        return new OfertaResumen(o.id(), directorio.persona(o.persona()), p.calificacion(),
                p.servicios(), o.monto(), o.mensaje(), o.sinLeer());
    }
}
