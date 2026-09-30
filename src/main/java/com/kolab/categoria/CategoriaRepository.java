package com.kolab.categoria;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Acceso a {@link Categoria}. Es la capa DAO del catálogo.
 */
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByNombre(String nombre);

    List<Categoria> findByEstadoOrderByNombre(EstadoCategoria estado);

    // precio bajo, típico y alto, y cuántas solicitudes entraron en el cálculo
    @Query(value = """
            select precio_bajo, precio_tipico, precio_alto, solicitudes
            from v_precios_categoria where id_categoria = :idCategoria""", nativeQuery = true)
    List<Object[]> precios(@Param("idCategoria") Long idCategoria);
}
