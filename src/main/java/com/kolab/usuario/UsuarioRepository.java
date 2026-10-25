package com.kolab.usuario;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Usuario> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    boolean existsByIdAndEstado(Long id, EstadoUsuario estado);
}
