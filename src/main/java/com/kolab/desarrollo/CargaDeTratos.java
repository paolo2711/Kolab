package com.kolab.desarrollo;

import com.kolab.calificacion.CalificacionForm;
import com.kolab.mensaje.MensajeService;
import com.kolab.oferta.OfertaForm;
import com.kolab.oferta.OfertaService;
import com.kolab.servicio.ServicioService;
import org.springframework.stereotype.Component;

/**
 * La segunda mitad de los ejemplos: ofertas, tratos cerrados con sus calificaciones y mensajes.
 */
@Component
class CargaDeTratos {

    private final Ejemplos ejemplos;
    private final OfertaService ofertaService;
    private final ServicioService servicioService;
    private final MensajeService mensajeService;

    CargaDeTratos(Ejemplos ejemplos, OfertaService ofertaService, ServicioService servicioService,
                  MensajeService mensajeService) {
        this.ejemplos = ejemplos;
        this.ofertaService = ofertaService;
        this.servicioService = servicioService;
        this.mensajeService = mensajeService;
    }

    void cargar(CargaEnCurso carga) {
        ejemplos.ofertas().forEach(o -> ofertar(o, carga));
        ejemplos.tratos().forEach(t -> cerrarTrato(t, carga));
        ejemplos.mensajes().forEach(m -> escribir(m, carga));
    }

    private void ofertar(Ejemplos.Oferta o, CargaEnCurso carga) {
        OfertaForm form = new OfertaForm();
        form.setMonto(o.monto());
        form.setMensaje(o.mensaje());
        Long id = ofertaService.ofertar(carga.solicitud(o.solicitud()), form, carga.persona(o.autor()));
        carga.ofertas.put(o.clave(), id);
        carga.ofertasPorClave.put(o.clave(), o);
        carga.fechas.add(Fechado.oferta(id, o.haceHoras()));
        if (o.rechazada()) {
            ofertaService.rechazar(id, quienPide(o.clave(), carga));
        }
    }

    private void cerrarTrato(Ejemplos.Trato t, CargaEnCurso carga) {
        Long pide = quienPide(t.oferta(), carga);
        Long ofrece = carga.persona(carga.ofertasPorClave.get(t.oferta()).autor());
        Long idServicio = ofertaService.aceptar(carga.oferta(t.oferta()), pide);
        carga.fechas.add(Fechado.inicio(idServicio, t.haceHoras()));
        if (t.cerradoHaceHoras() == null) {
            return;
        }
        servicioService.cerrarYCalificar(idServicio, nota(t.calificaQuienPide()), pide);
        if (t.calificaQuienOfrece() != null) {
            servicioService.cerrarYCalificar(idServicio, nota(t.calificaQuienOfrece()), ofrece);
        }
        carga.fechas.add(Fechado.cierre(idServicio, t.cerradoHaceHoras()));
        carga.fechas.add(Fechado.calificaciones(idServicio, t.cerradoHaceHoras()));
    }

    private void escribir(Ejemplos.Mensaje m, CargaEnCurso carga) {
        Ejemplos.Oferta oferta = carga.ofertasPorClave.get(m.oferta());
        Long emisor = m.deQuienPide() ? quienPide(m.oferta(), carga) : carga.persona(oferta.autor());
        Long id = mensajeService.enviar(carga.oferta(m.oferta()), m.contenido(), emisor);
        carga.fechas.add(Fechado.mensaje(id, m.haceMinutos()));
        if (m.leido()) {
            carga.mensajesLeidos.add(id);
        }
    }

    private Long quienPide(String claveOferta, CargaEnCurso carga) {
        Ejemplos.Oferta oferta = carga.ofertasPorClave.get(claveOferta);
        if (oferta == null) {
            throw new IllegalStateException("No hay «" + claveOferta + "» en ofertas.json");
        }
        return carga.persona(carga.autores.get(oferta.solicitud()));
    }

    private CalificacionForm nota(Ejemplos.Nota n) {
        CalificacionForm form = new CalificacionForm();
        form.setPuntaje(n.puntaje());
        form.setComentario(n.comentario());
        return form;
    }
}
