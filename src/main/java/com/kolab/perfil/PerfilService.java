package com.kolab.perfil;

import java.util.List;

/**
 * El perfil propio: lo que la persona cuenta de sí y lo que declara saber hacer, de donde sale qué
 * solicitudes le aparecen para ofertar.
 */
public interface PerfilService {

    /**
     * Los ids de las categorías que esa persona declaró.
     *
     * @return lista vacía si todavía no declaró ninguna, nunca {@code null}
     */
    List<Long> categoriasDe(Long idUsuario);

    /**
     * Reemplaza las categorías declaradas por las que se pasan.
     *
     * @param idsCategoria las categorías que quedan; una lista vacía borra todas
     */
    void guardarCategorias(Long idUsuario, List<Long> idsCategoria);

    /**
     * Si la persona ofrece algo, que es lo mismo que decir si declaró al menos una categoría.
     */
    boolean ofreceServicios(Long idUsuario);

    PerfilResumen miPerfil(Long idUsuario);

    PerfilForm formularioDe(Long idUsuario);

    /**
     * Guarda los datos de la cuenta y lo que la persona cuenta de sí.
     */
    void actualizar(Long idUsuario, PerfilForm form);
}
