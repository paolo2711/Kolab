package com.kolab.demo;

import com.kolab.mensaje.ConversacionDetalle;
import com.kolab.mensaje.ConversacionResumen;
import com.kolab.mensaje.MensajeResumen;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class MensajesDeEjemplo {

    private final ArchivosDeDatos archivos;
    private final DirectorioDePersonas directorio;

    public MensajesDeEjemplo(ArchivosDeDatos archivos, DirectorioDePersonas directorio) {
        this.archivos = archivos;
        this.directorio = directorio;
    }

    public List<MensajeResumen> mensajesCon(Long idOferta) {
        return archivos.mensajes().stream()
                .filter(m -> m.oferta().equals(idOferta))
                .map(m -> new MensajeResumen(m.contenido(), m.hora(), m.mio()))
                .toList();
    }

    public List<ConversacionResumen> conversaciones() {
        Map<Long, List<MensajeJson>> porOferta = archivos.mensajes().stream()
                .collect(Collectors.groupingBy(MensajeJson::oferta));

        return porOferta.keySet().stream()
                .sorted()
                .map(idOferta -> {
                    OfertaJson o = archivos.oferta(idOferta);
                    SolicitudJson s = archivos.solicitud(o.solicitud());
                    List<MensajeJson> hilo = porOferta.get(idOferta);
                    MensajeJson ultimo = hilo.get(hilo.size() - 1);
                    return new ConversacionResumen(s.titulo(), directorio.contraparte(s, o),
                            ultimo.contenido(), ultimo.hora(), o.sinLeer() > 0, "/mensajes/" + o.id());
                })
                .toList();
    }

    public ConversacionDetalle conversacion(Long idOferta) {
        OfertaJson o = archivos.oferta(idOferta);
        SolicitudJson s = archivos.solicitud(o.solicitud());
        boolean cerrado = servicioDe(idOferta) != null;
        return new ConversacionDetalle(o.id(), s.titulo(), directorio.contraparte(s, o), enlaceDe(s, o),
                cerrado ? "Ver el servicio" : "Ver la solicitud", mensajesCon(idOferta));
    }

    // a dónde lleva la conversación: al servicio si ya se cerró el trato, si no a la solicitud
    private String enlaceDe(SolicitudJson s, OfertaJson o) {
        ServicioJson v = servicioDe(o.id());
        if (v != null) {
            return "/servicios/" + v.id();
        }
        return s.mia() ? "/mis-solicitudes/" + s.id() : "/solicitudes/" + s.id();
    }

    private ServicioJson servicioDe(Long idOferta) {
        return archivos.servicios().stream()
                .filter(v -> v.oferta().equals(idOferta))
                .findFirst()
                .orElse(null);
    }
}
