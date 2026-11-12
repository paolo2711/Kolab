package com.kolab.servicio;

import com.kolab.modelo.dto.ExpertoResumen;
import com.kolab.modelo.dto.PerfilPublico;
import java.util.List;

/**
 * las personas que ofrecen algo, como las ve el resto
 */
public interface DirectorioService {

    /**
     * los que ofrecen algo con mejor promedio, solo si ya tienen alguna calificacion
     */
    List<ExpertoResumen> mejorCalificados(int cuantos);

    List<ExpertoResumen> queOfrecen(Long idCategoria, int cuantos);

    /**
     * el perfil que ven los demas, por codigo publico y nunca por id
     */
    PerfilPublico publico(String codigo);
}
