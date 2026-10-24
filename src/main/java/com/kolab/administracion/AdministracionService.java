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
     * Deja la cuenta sin poder ingresar y corta su sesión en el siguiente pedido. Nadie se suspende
     * a sí mismo. Devuelve el correo de la cuenta, para la bitácora.
     */
    String suspender(Long idCuenta, Long idAdministrador);

    /**
     * Devuelve el correo de la cuenta, para la bitácora.
     */
    String reactivar(Long idCuenta);

    /**
     * Las cifras del panel. Todo se calcula al consultar: no hay tabla de reportes.
     */
    Tablero tablero();
}
