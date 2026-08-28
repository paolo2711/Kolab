package com.kolab.perfil;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfilCategoriaRepository extends JpaRepository<PerfilCategoria, Long> {

    List<PerfilCategoria> findByPerfilId(Long idPerfil);

    long countByPerfilUsuarioId(Long idUsuario);

    void deleteByPerfilId(Long idPerfil);
}
