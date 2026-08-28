package com.kolab.demo;

import com.kolab.perfil.Persona;
import com.kolab.servicio.ServicioDetalle;
import com.kolab.servicio.ServicioResumen;
import java.util.List;
import java.util.function.Predicate;
import org.springframework.stereotype.Component;

@Component
public class ServiciosDeEjemplo {

    private final ArchivosDeDatos archivos;
    private final CatalogoDeCategorias categorias;
    private final DirectorioDePersonas directorio;
    private final MensajesDeEjemplo mensajes;

    public ServiciosDeEjemplo(ArchivosDeDatos archivos, CatalogoDeCategorias categorias,
                              DirectorioDePersonas directorio, MensajesDeEjemplo mensajes) {
        this.archivos = archivos;
        this.categorias = categorias;
        this.directorio = directorio;
        this.mensajes = mensajes;
    }

    public List<ServicioResumen> serviciosQuePedi() {
        return listar(ServicioJson::loPedi);
    }

    public List<ServicioResumen> serviciosQueDoy() {
        return listar(ServicioJson::loDoy);
    }

    public ServicioDetalle servicio(Long id) {
        ServicioJson v = archivos.servicios().stream()
                .filter(x -> x.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No hay servicio con id " + id));

        OfertaJson o = archivos.oferta(v.oferta());
        SolicitudJson s = archivos.solicitud(v.solicitud());
        Persona quien = directorio.contraparte(s, o);
        PersonaJson p = archivos.persona(quien.id());

        return new ServicioDetalle(v.id(), s.id(), s.titulo(), categorias.nombreDe(s.categoria()),
                quien, p.calificacion(), p.servicios(), v.miembroDesde(),
                o.monto(), v.comision(), v.estado(), v.fecha(), mensajes.mensajesCon(o.id()));
    }

    private List<ServicioResumen> listar(Predicate<ServicioJson> filtro) {
        return archivos.servicios().stream()
                .filter(filtro)
                .map(v -> {
                    OfertaJson o = archivos.oferta(v.oferta());
                    SolicitudJson s = archivos.solicitud(v.solicitud());
                    return new ServicioResumen(v.id(), s.titulo(), directorio.contraparte(s, o),
                            o.monto(), v.comision(), v.estado(), v.fecha(), v.loDoy());
                })
                .toList();
    }
}
