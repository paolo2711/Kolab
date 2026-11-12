package com.kolab.dao;

import com.kolab.modelo.entidad.EstadoUsuario;
import com.kolab.modelo.entidad.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Usuario> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    boolean existsByIdAndEstado(Long id, EstadoUsuario estado);
}
