package com.kolab.solicitud;

import com.kolab.categoria.Categoria;
import com.kolab.categoria.CategoriaRepository;
import com.kolab.common.OperacionNoPermitidaException;
import com.kolab.common.RecursoNoEncontradoException;
import com.kolab.usuario.Usuario;
import com.kolab.usuario.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de {@link SolicitudService}.
 */
@Service
public class SolicitudServiceImpl implements SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final DetallesDeSolicitud detalles;
    private final MisSolicitudes misSolicitudes;

    public SolicitudServiceImpl(SolicitudRepository solicitudRepository, CategoriaRepository categoriaRepository,
                                UsuarioRepository usuarioRepository, DetallesDeSolicitud detalles,
                                MisSolicitudes misSolicitudes) {
        this.solicitudRepository = solicitudRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.detalles = detalles;
        this.misSolicitudes = misSolicitudes;
    }

    @Override
    @Transactional
    public Long publicar(SolicitudForm form, Long idAutor) {
        Usuario autor = usuarioRepository.getReferenceById(idAutor);
        Solicitud nueva = new Solicitud(categoriaActiva(form.getIdCategoria()), autor, form.getTitulo().trim(),
                form.getDescripcion().trim(), form.getModalidad(), form.getPrecioPropuesto());
        copiarDetalles(form, nueva);
        if (form.getIdDestinatario() != null) {
            if (form.getIdDestinatario().equals(idAutor)) {
                throw new OperacionNoPermitidaException("No puedes proponerte un servicio a ti mismo.");
            }
            nueva.setDestinatario(usuarioRepository.findById(form.getIdDestinatario())
                    .orElseThrow(() -> new RecursoNoEncontradoException("persona", form.getIdDestinatario())));
        }
        return solicitudRepository.save(nueva).getId();
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudDetalle paraOfertar(Long idSolicitud, Long idUsuario) {
        return detalles.paraOfertar(idSolicitud, idUsuario);
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudDetalle propia(Long idSolicitud, Long idAutor) {
        return detalles.propia(idSolicitud, idAutor);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean esSuya(Long idSolicitud, Long idUsuario) {
        return detalles.cargar(idSolicitud).esDe(idUsuario);
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudForm formularioDe(Long idSolicitud, Long idAutor) {
        Solicitud s = editable(idSolicitud, idAutor);
        SolicitudForm form = new SolicitudForm();
        form.setTitulo(s.getTitulo());
        form.setIdCategoria(s.getCategoria().getId());
        form.setModalidad(s.getModalidad());
        form.setDistrito(s.getDistrito());
        form.setDescripcion(s.getDescripcion());
        form.setFechaDeseada(s.getFechaDeseada());
        form.setPrecioPropuesto(s.getPrecioPropuesto());
        return form;
    }

    @Override
    @Transactional
    public void editar(Long idSolicitud, SolicitudForm form, Long idAutor) {
        Solicitud s = editable(idSolicitud, idAutor);
        s.setTitulo(form.getTitulo().trim());
        s.setCategoria(categoriaActiva(form.getIdCategoria()));
        s.setModalidad(form.getModalidad());
        s.setDescripcion(form.getDescripcion().trim());
        s.setPrecioPropuesto(form.getPrecioPropuesto());
        copiarDetalles(form, s);
    }

    @Override
    @Transactional
    public void cancelar(Long idSolicitud, Long idAutor) {
        editable(idSolicitud, idAutor).cancelar();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MiSolicitud> mias(Long idAutor, Pageable pagina) {
        return misSolicitudes.de(idAutor, pagina);
    }

    @Override
    @Transactional(readOnly = true)
    public long cuantasMias(Long idAutor) {
        return solicitudRepository.countByAutorId(idAutor);
    }

    private Solicitud editable(Long idSolicitud, Long idAutor) {
        Solicitud s = detalles.cargar(idSolicitud);
        if (!s.esDe(idAutor)) {
            throw new RecursoNoEncontradoException("solicitud", idSolicitud);
        }
        if (!s.puedeEditarse()) {
            throw new OperacionNoPermitidaException("Esta solicitud ya no se puede cambiar: "
                    + s.getEstado().getEtiqueta().toLowerCase() + ".");
        }
        return s;
    }

    private Categoria categoriaActiva(Long idCategoria) {
        Categoria c = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new RecursoNoEncontradoException("categoría", idCategoria));
        if (!c.estaActiva()) {
            throw new OperacionNoPermitidaException("La categoría " + c.getNombre() + " ya no recibe solicitudes.");
        }
        return c;
    }

    // en una virtual el distrito no dice nada, así que no se guarda
    private void copiarDetalles(SolicitudForm form, Solicitud s) {
        boolean presencial = form.getModalidad() == Modalidad.PRESENCIAL;
        String distrito = form.getDistrito() == null ? null : form.getDistrito().trim();
        s.setDistrito(presencial && distrito != null && !distrito.isEmpty() ? distrito : null);
        s.setFechaDeseada(form.getFechaDeseada());
    }
}
