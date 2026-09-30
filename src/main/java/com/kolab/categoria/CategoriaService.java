package com.kolab.categoria;

import java.util.List;

/**
 * El catálogo de categorías con lo que se está pidiendo y pagando en cada una.
 */
public interface CategoriaService {

    /**
     * Las categorías activas, en orden alfabético, cada una con sus solicitudes abiertas. Es un
     * catálogo corto por naturaleza, por eso no va paginado.
     */
    List<CategoriaResumen> activas();

    CategoriaResumen resumen(Long idCategoria);

    /**
     * Lo que se está pagando en la categoría, según lo que la gente publicó.
     */
    PreciosDeCategoria precios(Long idCategoria);
}
