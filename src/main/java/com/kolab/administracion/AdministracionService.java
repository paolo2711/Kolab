package com.kolab.administracion;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Lo que ve y hace quien administra la plataforma.
 */
public interface AdministracionService {

    /**
     * Todas las cuentas, las más nuevas primero.
     */
    Page<CuentaResumen> cuentas(Pageable pagina);

    /**
     * Las cifras del panel. Todo se calcula al consultar: no hay tabla de reportes.
     */
    Tablero tablero();
}
