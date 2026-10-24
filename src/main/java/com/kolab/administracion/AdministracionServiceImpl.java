package com.kolab.administracion;

import com.kolab.common.Tiempo;
import com.kolab.usuario.EstadoUsuario;
import com.kolab.usuario.Usuario;
import com.kolab.usuario.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdministracionServiceImpl implements AdministracionService {

    private final UsuarioRepository usuarioRepository;

    public AdministracionServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Page<CuentaResumen> cuentas(Pageable pagina) {
        Pageable nuevasPrimero = PageRequest.of(pagina.getPageNumber(), pagina.getPageSize(),
                Sort.by(Sort.Direction.DESC, "fechaRegistro"));
        return usuarioRepository.findAll(nuevasPrimero).map(this::aResumen);
    }

    @Override
    public long cuentasRegistradas() {
        return usuarioRepository.count();
    }

    private CuentaResumen aResumen(Usuario u) {
        return new CuentaResumen(u.getNombreCompleto(), u.getEmail(),
                Tiempo.fecha(u.getFechaRegistro()), u.getEstado() == EstadoUsuario.ACTIVO);
    }
}
