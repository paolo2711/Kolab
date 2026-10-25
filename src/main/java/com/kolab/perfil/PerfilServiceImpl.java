package com.kolab.perfil;

import com.kolab.calificacion.CalificacionService;
import com.kolab.categoria.CategoriaRepository;
import com.kolab.common.FotosSubidas;
import com.kolab.common.RecursoNoEncontradoException;
import com.kolab.common.Tiempo;
import com.kolab.solicitud.SolicitudRepository;
import com.kolab.usuario.Usuario;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de {@link PerfilService}.
 */
@Service
public class PerfilServiceImpl implements PerfilService {

    private static final int RESENAS = 5;

    private final PerfilRepository perfilRepository;
    private final PerfilCategoriaRepository perfilCategoriaRepository;
    private final CategoriaRepository categoriaRepository;
    private final SolicitudRepository solicitudRepository;
    private final ReputacionService reputacionService;
    private final CalificacionService calificacionService;
    private final FotosDePerfil fotos;

    public PerfilServiceImpl(PerfilRepository perfilRepository,
                             PerfilCategoriaRepository perfilCategoriaRepository,
                             CategoriaRepository categoriaRepository, SolicitudRepository solicitudRepository,
                             ReputacionService reputacionService, CalificacionService calificacionService,
                             FotosDePerfil fotos) {
        this.perfilRepository = perfilRepository;
        this.perfilCategoriaRepository = perfilCategoriaRepository;
        this.categoriaRepository = categoriaRepository;
        this.solicitudRepository = solicitudRepository;
        this.reputacionService = reputacionService;
        this.calificacionService = calificacionService;
        this.fotos = fotos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> categoriasDe(Long idUsuario) {
        return perfilCategoriaRepository.findByPerfilId(perfilDe(idUsuario).getId()).stream()
                .map(PerfilCategoria::getIdCategoria)
                .toList();
    }

    @Override
    @Transactional
    public void guardarCategorias(Long idUsuario, List<Long> idsCategoria) {
        Perfil perfil = perfilDe(idUsuario);
        perfilCategoriaRepository.deleteByPerfilId(perfil.getId());
        // sin el flush, JPA inserta antes de borrar y revienta el unique de (perfil, categoría)
        perfilCategoriaRepository.flush();
        idsCategoria.stream()
                .distinct()
                .map(categoriaRepository::getReferenceById)
                .map(perfil::declararCategoria)
                .forEach(perfilCategoriaRepository::save);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean ofreceServicios(Long idUsuario) {
        return perfilCategoriaRepository.countByPerfilUsuarioId(idUsuario) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public PerfilResumen miPerfil(Long idUsuario) {
        Perfil perfil = perfilDe(idUsuario);
        Usuario u = perfil.getUsuario();
        return new PerfilResumen(perfil.getDescripcion(), perfil.getExperiencia(), u.getDistrito(), u.getTelefono(),
                Tiempo.mesYAnio(u.getFechaRegistro()), reputacionService.de(idUsuario),
                solicitudRepository.countByAutorId(idUsuario),
                calificacionService.recibidas(idUsuario, PageRequest.of(0, RESENAS)).getContent(),
                CabeceraDePerfil.de(perfil));
    }

    @Override
    @Transactional(readOnly = true)
    public PerfilForm formularioDe(Long idUsuario) {
        Perfil perfil = perfilDe(idUsuario);
        Usuario u = perfil.getUsuario();
        PerfilForm form = new PerfilForm();
        form.setNombre(u.getNombre());
        form.setApellidos(u.getApellidos());
        form.setTelefono(u.getTelefono());
        form.setDistrito(u.getDistrito());
        form.setDescripcion(perfil.getDescripcion());
        form.setExperiencia(perfil.getExperiencia());
        form.setFotoActual(FotosSubidas.url(u.getFoto()));
        form.setPortadaActual(FotosSubidas.url(perfil.getPortada()));
        return form;
    }

    @Override
    @Transactional
    public void actualizar(Long idUsuario, PerfilForm form) {
        Perfil perfil = perfilDe(idUsuario);
        Usuario u = perfil.getUsuario();
        u.setNombre(form.getNombre().trim());
        u.setApellidos(form.getApellidos().trim());
        u.setTelefono(limpio(form.getTelefono()));
        String distrito = limpio(form.getDistrito());
        // las coordenadas eran del distrito anterior: si cambia, ya no sirven
        if (distrito == null || !distrito.equals(u.getDistrito())) {
            u.setLatitud(null);
            u.setLongitud(null);
        }
        u.setDistrito(distrito);
        perfil.setDescripcion(limpio(form.getDescripcion()));
        perfil.setExperiencia(limpio(form.getExperiencia()));
        fotos.aplicar(form, u, perfil);
    }

    // cada cuenta tiene el suyo desde que se registra: si no aparece, la persona no existe
    private Perfil perfilDe(Long idUsuario) {
        return perfilRepository.findByUsuarioId(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("persona", idUsuario));
    }

    private static String limpio(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
