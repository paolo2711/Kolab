package com.kolab.perfil;

import java.util.List;

public interface PerfilService {

    List<Long> categoriasDe(Long idUsuario);

    void guardarCategorias(Long idUsuario, List<Long> idsCategoria);

    boolean ofreceServicios(Long idUsuario);
}
