package com.kolab.demo;

import com.kolab.calificacion.Calificacion;
import com.kolab.calificacion.CalificacionRepository;
import com.kolab.categoria.Categoria;
import com.kolab.categoria.CategoriaRepository;
import com.kolab.mensaje.Mensaje;
import com.kolab.mensaje.MensajeRepository;
import com.kolab.oferta.EstadoOferta;
import com.kolab.oferta.Oferta;
import com.kolab.oferta.OfertaRepository;
import com.kolab.perfil.Perfil;
import com.kolab.perfil.PerfilCategoriaRepository;
import com.kolab.perfil.PerfilRepository;
import com.kolab.servicio.Servicio;
import com.kolab.servicio.ServicioRepository;
import com.kolab.solicitud.EstadoSolicitud;
import com.kolab.solicitud.Solicitud;
import com.kolab.solicitud.SolicitudRepository;
import com.kolab.usuario.EstadoUsuario;
import com.kolab.usuario.Usuario;
import com.kolab.usuario.UsuarioRepository;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Copia los datos de ejemplo de {@code resources/datos} a la base, una sola vez y solo si las
 * tablas estan vacias.
 *
 * <p>Es un puente mientras las pantallas siguen leyendo el JSON: sirve para que los reportes, que
 * si consultan la base, tengan de donde sacar cifras. Se apaga con {@code kolab.datos-demo=false}
 * y se puede borrar entero sin tocar nada mas.
 */
@Component
@Order(20)
@ConditionalOnProperty(name = "kolab.datos-demo", havingValue = "true", matchIfMissing = true)
public class CargadorDeDatosDemo implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CargadorDeDatosDemo.class);
    private static final String CLAVE = "kolab1234";
    private static final BigDecimal COMISION = BigDecimal.valueOf(5);

    private final ArchivosDeDatos archivos;
    private final UsuarioRepository usuarios;
    private final PerfilRepository perfiles;
    private final PerfilCategoriaRepository perfilCategorias;
    private final CategoriaRepository categorias;
    private final SolicitudRepository solicitudes;
    private final OfertaRepository ofertas;
    private final ServicioRepository servicios;
    private final MensajeRepository mensajes;
    private final CalificacionRepository calificaciones;
    private final PasswordEncoder cifrador;

    public CargadorDeDatosDemo(ArchivosDeDatos archivos, UsuarioRepository usuarios,
                               PerfilRepository perfiles, PerfilCategoriaRepository perfilCategorias,
                               CategoriaRepository categorias, SolicitudRepository solicitudes,
                               OfertaRepository ofertas, ServicioRepository servicios,
                               MensajeRepository mensajes, CalificacionRepository calificaciones,
                               PasswordEncoder cifrador) {
        this.archivos = archivos;
        this.usuarios = usuarios;
        this.perfiles = perfiles;
        this.perfilCategorias = perfilCategorias;
        this.categorias = categorias;
        this.solicitudes = solicitudes;
        this.ofertas = ofertas;
        this.servicios = servicios;
        this.mensajes = mensajes;
        this.calificaciones = calificaciones;
        this.cifrador = cifrador;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (solicitudes.count() > 0) {
            return;
        }

        Map<Long, Usuario> gente = cargarPersonas();
        Map<Long, Solicitud> publicadas = cargarSolicitudes(gente);
        Map<Long, Oferta> enviadas = cargarOfertas(publicadas, gente);
        cargarServiciosYCalificaciones(enviadas);
        cargarMensajes(enviadas);

        log.info("datos de ejemplo cargados: {} personas, {} solicitudes, {} ofertas",
                gente.size(), publicadas.size(), enviadas.size());
    }

    private Map<Long, Usuario> cargarPersonas() {
        Map<Long, Usuario> gente = new HashMap<>();
        for (PersonaJson p : archivos.personas()) {
            Usuario u = usuarios.findByEmail(correoDe(p.nombre()))
                    .orElseGet(() -> usuarios.save(nuevoUsuario(p)));
            gente.put(p.id(), u);

            Perfil perfil = perfiles.findByUsuarioId(u.getId())
                    .orElseGet(() -> perfiles.save(new Perfil(u)));
            perfil.setDescripcion(p.titular());
            for (int i = 0; i < p.servicios(); i++) {
                perfil.registrarCalificacion(redondeado(p.calificacion()));
            }
            for (Long idCategoria : p.categorias()) {
                Categoria c = categorias.getReferenceById(idCategoria);
                perfilCategorias.save(perfil.declararCategoria(c));
            }
        }
        return gente;
    }

    private Usuario nuevoUsuario(PersonaJson p) {
        String[] partes = p.nombre().split(" ", 2);
        Usuario u = new Usuario();
        u.setNombre(partes[0]);
        u.setApellidos(partes.length > 1 ? partes[1] : partes[0]);
        u.setEmail(correoDe(p.nombre()));
        u.setPasswordHash(cifrador.encode(CLAVE));
        u.setDistrito(p.distrito());
        u.setFechaRegistro(LocalDateTime.now().minusMonths(6));
        u.setEstado(EstadoUsuario.ACTIVO);
        return u;
    }

    private Map<Long, Solicitud> cargarSolicitudes(Map<Long, Usuario> gente) {
        Usuario cliente = usuarios.findByEmail("cliente@kolab.pe").orElseThrow();
        Map<Long, Solicitud> publicadas = new HashMap<>();

        for (SolicitudJson s : archivos.solicitudes()) {
            Usuario autor = s.mia() ? cliente : gente.get(s.persona());
            if (autor == null) {
                continue;
            }
            Solicitud nueva = new Solicitud(categorias.getReferenceById(s.categoria()), autor,
                    s.titulo(), s.descripcion(), s.modalidad(), s.precio());
            nueva.setDistrito(s.distrito());
            nueva.setLatitud(decimal(s.latitud()));
            nueva.setLongitud(decimal(s.longitud()));
            nueva.setFechaDeseada(s.fechaDeseada());
            publicadas.put(s.id(), solicitudes.save(nueva));
        }
        return publicadas;
    }

    private Map<Long, Oferta> cargarOfertas(Map<Long, Solicitud> publicadas, Map<Long, Usuario> gente) {
        Usuario luis = gente.get(1L);
        Map<Long, Oferta> enviadas = new HashMap<>();

        for (OfertaJson o : archivos.ofertas()) {
            Solicitud s = publicadas.get(o.solicitud());
            Usuario quien = o.mia() ? luis : gente.get(o.persona());
            // nadie oferta en su propia solicitud, y solo se admite una por persona y solicitud
            if (s == null || quien == null || quien.getId().equals(s.getAutor().getId())
                    || ofertas.findBySolicitudIdAndUsuarioId(s.getId(), quien.getId()).isPresent()) {
                continue;
            }
            Oferta nueva = new Oferta(s, quien, o.monto(), o.mensaje());
            if (o.estado() == EstadoOferta.RECHAZADA) {
                nueva.rechazar();
            }
            enviadas.put(o.id(), ofertas.save(nueva));
        }
        return enviadas;
    }

    private void cargarServiciosYCalificaciones(Map<Long, Oferta> enviadas) {
        for (ServicioJson v : archivos.servicios()) {
            Oferta o = enviadas.get(v.oferta());
            if (o == null || o.getEstado() != EstadoOferta.ENVIADA) {
                continue;
            }
            Servicio servicio = servicios.save(o.aceptar(COMISION));
            if (v.estado() == com.kolab.servicio.EstadoServicio.CERRADO) {
                servicio.cerrar();
                calificar(servicio);
            }
        }
    }

    // al cerrar, cada parte califica a la otra: eso es lo que alimenta el promedio del perfil
    private void calificar(Servicio servicio) {
        Usuario cliente = servicio.getOferta().getSolicitud().getAutor();
        Usuario experto = servicio.getOferta().getUsuario();
        calificaciones.save(new Calificacion(servicio, cliente, experto, 5,
                "Cumplio con lo acordado y a tiempo."));
        calificaciones.save(new Calificacion(servicio, experto, cliente, 5,
                "Todo claro desde el principio."));
    }

    private void cargarMensajes(Map<Long, Oferta> enviadas) {
        for (MensajeJson m : archivos.mensajes()) {
            Oferta o = enviadas.get(m.oferta());
            if (o == null) {
                continue;
            }
            Usuario emisor = m.mio() ? o.getSolicitud().getAutor() : o.getUsuario();
            Mensaje mensaje = new Mensaje(o, emisor, m.contenido());
            if (m.mio()) {
                mensaje.marcarLeido();
            }
            mensajes.save(mensaje);
        }
    }

    private String correoDe(String nombre) {
        String plano = Normalizer.normalize(nombre, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("[^a-z]+", ".");
        return plano + "@kolab.pe";
    }

    private int redondeado(BigDecimal calificacion) {
        return calificacion == null ? 5 : Math.max(1, Math.min(5, calificacion.setScale(0,
                java.math.RoundingMode.HALF_UP).intValue()));
    }

    private BigDecimal decimal(Double valor) {
        return valor == null ? null : BigDecimal.valueOf(valor);
    }
}
