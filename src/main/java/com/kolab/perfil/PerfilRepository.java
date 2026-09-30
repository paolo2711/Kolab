package com.kolab.perfil;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Acceso a {@link Perfil}. Es la capa DAO del perfil y de la reputación.
 */
public interface PerfilRepository extends JpaRepository<Perfil, Long> {

    Optional<Perfil> findByUsuarioId(Long idUsuario);

    @EntityGraph(attributePaths = "usuario")
    List<Perfil> findByUsuarioIdIn(Collection<Long> idsUsuario);

    // solo quien ofrece algo y ya tiene al menos una calificación
    @Query(value = """
            select p from Perfil p join fetch p.usuario
            where p.totalCalificaciones > 0
              and exists (select 1 from PerfilCategoria pc where pc.perfil = p)
            order by p.califPromedio desc, p.totalCalificaciones desc""",
            countQuery = """
            select count(p) from Perfil p
            where p.totalCalificaciones > 0
              and exists (select 1 from PerfilCategoria pc where pc.perfil = p)""")
    Page<Perfil> mejorCalificados(Pageable pagina);

    @Query(value = """
            select p from Perfil p join fetch p.usuario
            where exists (select 1 from PerfilCategoria pc
                          where pc.perfil = p and pc.categoria.id = :idCategoria)
            order by p.califPromedio desc, p.totalCalificaciones desc""",
            countQuery = """
            select count(p) from Perfil p
            where exists (select 1 from PerfilCategoria pc
                          where pc.perfil = p and pc.categoria.id = :idCategoria)""")
    Page<Perfil> queOfrecen(@Param("idCategoria") Long idCategoria, Pageable pagina);

    @Query("select count(distinct pc.perfil) from PerfilCategoria pc")
    long contarQuienesOfrecen();
}
