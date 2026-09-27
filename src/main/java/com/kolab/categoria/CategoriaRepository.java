package com.kolab.categoria;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a {@link Categoria}. Es la capa DAO del catálogo.
 */
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByNombre(String nombre);

    List<Categoria> findByEstadoOrderByNombre(EstadoCategoria estado);
}
