package com.kolab.configuracion;

import com.kolab.dao.CategoriaRepository;
import com.kolab.dao.SolicitudRepository;
import com.kolab.dao.UsuarioRepository;
import com.kolab.modelo.dto.PerfilForm;
import com.kolab.modelo.dto.Persona;
import com.kolab.modelo.dto.RegistroForm;
import com.kolab.modelo.dto.SolicitudForm;
import com.kolab.modelo.entidad.Categoria;
import com.kolab.modelo.entidad.Modalidad;
import com.kolab.modelo.entidad.Rol;
import com.kolab.modelo.entidad.Solicitud;
import com.kolab.modelo.entidad.Usuario;
import com.kolab.servicio.PerfilService;
import com.kolab.servicio.SolicitudService;
import com.kolab.servicio.UsuarioService;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * llena una base vacia con los ejemplos, por los mismos servicios que usa la app
 */
@Component
@ConditionalOnProperty(name = "kolab.desarrollo", havingValue = "true")
public class CargaDeEjemplos {

    private static final Logger log = LoggerFactory.getLogger(CargaDeEjemplos.class);

    private final Ejemplos ejemplos;
    private final UsuarioService usuarioService;
    private final PerfilService perfilService;
    private final SolicitudService solicitudService;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final SolicitudRepository solicitudRepository;
    private final CargaDeTratos tratos;
    private final Fechado fechado;
    private final String clave;

    CargaDeEjemplos(Ejemplos ejemplos, UsuarioService usuarioService, PerfilService perfilService,
                    SolicitudService solicitudService, UsuarioRepository usuarioRepository,
                    CategoriaRepository categoriaRepository, SolicitudRepository solicitudRepository,
                    CargaDeTratos tratos, Fechado fechado,
                    @Value("${kolab.clave-de-ejemplo}") String clave) {
        this.ejemplos = ejemplos;
        this.usuarioService = usuarioService;
        this.perfilService = perfilService;
        this.solicitudService = solicitudService;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.solicitudRepository = solicitudRepository;
        this.tratos = tratos;
        this.fechado = fechado;
        this.clave = clave;
    }

    /**
     * @return false si la base ya tenia solicitudes y no se toco nada
     */
    @Transactional
    public boolean cargarSiVacia() {
        if (solicitudRepository.count() > 0) {
            return false;
        }
        CargaEnCurso carga = new CargaEnCurso();
        ejemplos.personas().forEach(p -> registrar(p, carga));
        ejemplos.solicitudes().forEach(s -> publicar(s, carga));
        tratos.cargar(carga);
        fechado.aplicar(carga.fechas, carga.mensajesLeidos);
        log.info("ejemplos cargados: {} personas, {} solicitudes, {} ofertas",
                carga.personas.size(), carga.solicitudes.size(), carga.ofertas.size());
        return true;
    }

    private void registrar(Ejemplos.Persona p, CargaEnCurso carga) {
        Long id = usuarioService.buscarPorEmail(p.email())
                .map(Usuario::getId)
                .orElseGet(() -> usuarioService.registrar(registro(p)).getId());
        carga.personas.put(p.email(), id);

        if (!p.categorias().isEmpty()) {
            perfilService.guardarCategorias(id, p.categorias().stream().map(n -> categoria(n).getId()).toList());
        }
        perfilService.actualizar(id, perfil(p));
        Usuario u = usuarioRepository.getReferenceById(id);
        u.setLatitud(p.latitud());
        u.setLongitud(p.longitud());
        if (p.administrador()) {
            u.setRol(Rol.ADMINISTRADOR);
        }
        carga.fechas.add(Fechado.registro(id, p.mesesEnKolab()));
    }

    private void publicar(Ejemplos.Solicitud s, CargaEnCurso carga) {
        SolicitudForm form = new SolicitudForm();
        form.setTitulo(s.titulo());
        form.setIdCategoria(categoria(s.categoria()).getId());
        form.setModalidad(Modalidad.valueOf(s.modalidad()));
        form.setDistrito(s.distrito());
        form.setDescripcion(s.descripcion());
        form.setFechaDeseada(LocalDate.now().plusDays(s.dentroDeDias()));
        form.setPrecioPropuesto(s.precio());
        form.setCodigoDestinatario(s.para() == null ? null
                : usuarioService.buscarPorEmail(s.para()).map(Usuario::getCodigo).orElseThrow());

        Long id = solicitudService.publicar(form, carga.persona(s.autor()));
        Solicitud publicada = solicitudRepository.getReferenceById(id);
        publicada.setLatitud(s.latitud());
        publicada.setLongitud(s.longitud());
        carga.solicitudes.put(s.clave(), id);
        carga.autores.put(s.clave(), s.autor());
        carga.fechas.add(Fechado.solicitud(id, s.haceHoras()));
    }

    /**
     * deja creadas las cuentas de admin de los ejemplos aunque la base ya tenga datos
     */
    @Transactional
    public void asegurarAdministradores() {
        ejemplos.personas().stream().filter(Ejemplos.Persona::administrador).forEach(p -> {
            Long id = usuarioService.buscarPorEmail(p.email())
                    .map(Usuario::getId)
                    .orElseGet(() -> usuarioService.registrar(registro(p)).getId());
            usuarioRepository.getReferenceById(id).setRol(Rol.ADMINISTRADOR);
        });
    }

    private RegistroForm registro(Ejemplos.Persona p) {
        RegistroForm form = new RegistroForm();
        form.setNombre(p.nombre());
        form.setApellidos(p.apellidos());
        form.setEmail(p.email());
        form.setTelefono(p.telefono());
        form.setPassword(clave);
        form.setConfirmacion(clave);
        return form;
    }

    private PerfilForm perfil(Ejemplos.Persona p) {
        PerfilForm form = new PerfilForm();
        form.setNombre(p.nombre());
        form.setApellidos(p.apellidos());
        form.setTelefono(p.telefono());
        form.setDistrito(p.distrito());
        form.setDescripcion(p.descripcion());
        form.setExperiencia(p.experiencia());
        return form;
    }

    private Categoria categoria(String nombre) {
        return categoriaRepository.findByNombre(nombre)
                .orElseThrow(() -> new IllegalStateException("No hay categoría «" + nombre + "» en la base"));
    }
}
