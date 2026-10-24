package com.kolab.solicitud;

import com.kolab.perfil.PerfilService;
import com.kolab.usuario.Usuario;
import com.kolab.usuario.UsuarioRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de {@link CatalogoService}.
 */
@Service
@Transactional(readOnly = true)
public class CatalogoServiceImpl implements CatalogoService {

    private static final Sort RECIENTES = Sort.by(Sort.Direction.DESC, "fechaPublicacion");
    // entre cuántas presenciales recientes se busca la más cercana: acota la consulta
    private static final int CANDIDATAS_CERCANAS = 50;

    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final PerfilService perfilService;
    private final ResumenesDeSolicitud resumenes;

    public CatalogoServiceImpl(SolicitudRepository solicitudRepository,
                               UsuarioRepository usuarioRepository,
                               PerfilService perfilService,
                               ResumenesDeSolicitud resumenes) {
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
        this.perfilService = perfilService;
        this.resumenes = resumenes;
    }

    @Override
    public Page<SolicitudResumen> explorar(FiltroSolicitudes filtro, Long idUsuario, Pageable pagina) {
        List<Long> misCategorias = filtro.isSoloLoQueSe() ? perfilService.categoriasDe(idUsuario) : List.of();
        Specification<Solicitud> spec = SolicitudesVisibles.paraOfertar(idUsuario)
                .and(SolicitudesVisibles.segun(filtro, misCategorias));
        Pageable ordenada = PageRequest.of(pagina.getPageNumber(), pagina.getPageSize(), RECIENTES);
        Page<Solicitud> encontradas = solicitudRepository.findAll(spec, ordenada);
        return new PageImpl<>(resumenes.de(encontradas.getContent(), usuario(idUsuario)),
                ordenada, encontradas.getTotalElements());
    }

    @Override
    public List<SolicitudResumen> enMisCategorias(Long idUsuario, int cuantas) {
        List<Long> mias = perfilService.categoriasDe(idUsuario);
        if (mias.isEmpty()) {
            return List.of();
        }
        return buscar(SolicitudesVisibles.paraOfertar(idUsuario)
                .and(SolicitudesVisibles.enCategorias(mias)), idUsuario, cuantas);
    }

    @Override
    public List<SolicitudResumen> recientes(Long idUsuario, int cuantas) {
        return buscar(SolicitudesVisibles.paraOfertar(idUsuario), idUsuario, cuantas);
    }

    @Override
    public List<SolicitudResumen> cercanas(Long idUsuario, int cuantas) {
        Usuario yo = usuario(idUsuario);
        if (yo.getLatitud() == null || yo.getLongitud() == null) {
            return List.of();
        }
        Specification<Solicitud> spec = SolicitudesVisibles.paraOfertar(idUsuario)
                .and(SolicitudesVisibles.presencialesConUbicacion());
        List<Solicitud> candidatas = solicitudRepository
                .findAll(spec, PageRequest.of(0, CANDIDATAS_CERCANAS, RECIENTES)).getContent();
        return resumenes.de(candidatas, yo).stream()
                .sorted(Comparator.comparingDouble(SolicitudResumen::kilometros))
                .limit(cuantas)
                .toList();
    }

    @Override
    public List<SolicitudResumen> abiertasEn(Long idCategoria, Long idUsuario, int cuantas) {
        return buscar(SolicitudesVisibles.paraOfertar(idUsuario)
                .and(SolicitudesVisibles.deCategoria(idCategoria)), idUsuario, cuantas);
    }

    @Override
    public List<SolicitudResumen> recientesPublicas(int cuantas) {
        return resumenes.de(solicitudRepository
                .findAll(SolicitudesVisibles.publicas(), PageRequest.of(0, cuantas, RECIENTES))
                .getContent(), null);
    }

    @Override
    public List<String> distritos() {
        return solicitudRepository.distritosConAbiertas();
    }

    private List<SolicitudResumen> buscar(Specification<Solicitud> spec, Long idUsuario, int cuantas) {
        List<Solicitud> encontradas = solicitudRepository
                .findAll(spec, PageRequest.of(0, cuantas, RECIENTES)).getContent();
        return resumenes.de(encontradas, usuario(idUsuario));
    }

    private Usuario usuario(Long idUsuario) {
        return usuarioRepository.getReferenceById(idUsuario);
    }
}
