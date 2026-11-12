package com.kolab.servicio.impl;

import com.kolab.dao.CategoriaRepository;
import com.kolab.dao.SolicitudRepository;
import com.kolab.dao.UsuarioRepository;
import com.kolab.modelo.dto.MiSolicitud;
import com.kolab.modelo.dto.SolicitudDetalle;
import com.kolab.modelo.dto.SolicitudForm;
import com.kolab.modelo.entidad.Categoria;
import com.kolab.modelo.entidad.Modalidad;
import com.kolab.modelo.entidad.Solicitud;
import com.kolab.modelo.entidad.Usuario;
import com.kolab.servicio.SolicitudService;
import com.kolab.servicio.excepcion.OperacionNoPermitidaException;
import com.kolab.servicio.excepcion.RecursoNoEncontradoException;
import com.kolab.util.FotosSubidas;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SolicitudServiceImpl implements SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final DetallesDeSolicitud detalles;
    private final MisSolicitudes misSolicitudes;
    private final FotosDeSolicitud fotos;
    private final FotosSubidas subidas;

    public SolicitudServiceImpl(SolicitudRepository solicitudRepository, CategoriaRepository categoriaRepository,
                                UsuarioRepository usuarioRepository, DetallesDeSolicitud detalles,
                                MisSolicitudes misSolicitudes, FotosDeSolicitud fotos, FotosSubidas subidas) {
        this.solicitudRepository = solicitudRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.detalles = detalles;
        this.misSolicitudes = misSolicitudes;
        this.fotos = fotos;
        this.subidas = subidas;
    }

    @Override
    @Transactional
    public Long publicar(SolicitudForm form, Long idAutor) {
        Usuario autor = usuarioRepository.getReferenceById(idAutor);
        Solicitud nueva = new Solicitud(categoriaActiva(form.getIdCategoria()), autor, form.getTitulo().trim(),
                form.getDescripcion().trim(), form.getModalidad(), form.getPrecioPropuesto());
        copiarDetalles(form, nueva);
        if (FotosSubidas.subio(form.getFoto())) {
            nueva.setFoto(subidas.guardar(form.getFoto()));
        }
        if (form.getCodigoDestinatario() != null) {
            Usuario destinatario = usuarioRepository.findByCodigo(form.getCodigoDestinatario())
                    .orElseThrow(() -> new RecursoNoEncontradoException("persona " + form.getCodigoDestinatario()));
            if (destinatario.getId().equals(idAutor)) {
                throw new OperacionNoPermitidaException("No puedes proponerte un servicio a ti mismo.");
            }
            nueva.setDestinatario(destinatario);
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
        form.setFotoActual(fotos.propia(s));
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
        if (FotosSubidas.subio(form.getFoto())) {
            String anterior = s.getFoto();
            s.setFoto(subidas.guardar(form.getFoto()));
            subidas.borrar(anterior);
        }
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

    // en una virtual el distrito no dice nada asi que no se guarda
    private void copiarDetalles(SolicitudForm form, Solicitud s) {
        boolean presencial = form.getModalidad() == Modalidad.PRESENCIAL;
        String distrito = form.getDistrito() == null ? null : form.getDistrito().trim();
        s.setDistrito(presencial && distrito != null && !distrito.isEmpty() ? distrito : null);
        s.setFechaDeseada(form.getFechaDeseada());
    }
}
