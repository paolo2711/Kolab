package com.kolab.perfil;

import java.util.List;

/**
 * Las personas que ofrecen algo, tal como las ve el resto: para elegir a quién pedirle un servicio.
 */
public interface DirectorioService {

    /**
     * Quienes ofrecen algo y tienen mejor promedio. Solo cuenta quien ya fue calificado al menos
     * una vez.
     */
    List<ExpertoResumen> mejorCalificados(int cuantos);

    List<ExpertoResumen> queOfrecen(Long idCategoria, int cuantos);

    /**
     * El perfil que ven los demás, buscado por el código público de la persona y nunca por su id.
     */
    PerfilPublico publico(String codigo);
}
