package com.kolab.servicio;

import java.util.List;

/**
 * Revisa el estado de KOLAB y lo anota en el log. La corre sola, cada hora, {@code TareasProgramadas}.
 */
public interface RevisionDeSaludService {

    /**
     * Anota el estado en el log y devuelve los avisos: memoria o disco por encima del 80 % o la base sin
     * responder. Si todo está bien, la lista viene vacía.
     */
    List<String> revisar();
}
