package com.kolab.perfil;

import java.util.List;

/**
 * Lo que la persona declara saber hacer. De aquí sale qué solicitudes le aparecen para ofertar.
 */
public interface PerfilService {

    /**
     * Los ids de las categorías que esa persona declaró.
     *
     * @return lista vacía si todavía no declaró ninguna, nunca {@code null}
     */
    List<Long> categoriasDe(Long idUsuario);

    /**
     * Reemplaza las categorías declaradas por las que se pasan. Si la persona todavía no tenía
     * perfil, se le crea en esta misma operación.
     *
     * @param idsCategoria las categorías que quedan; una lista vacía borra todas
     */
    void guardarCategorias(Long idUsuario, List<Long> idsCategoria);

    /**
     * Si la persona aparece como experta, que es lo mismo que decir si declaró al menos una
     * categoría. No depende de cómo se registró.
     */
    boolean ofreceServicios(Long idUsuario);
}
