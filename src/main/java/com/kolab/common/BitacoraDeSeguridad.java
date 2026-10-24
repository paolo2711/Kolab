package com.kolab.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Deja constancia de lo que le pasa a las cuentas: quién entró, quién falló, quién pidió recuperar
 * su contraseña y quién intentó abrir algo que no es suyo.
 *
 * <p>Escribe en el logger {@code kolab.seguridad}, aparte del resto, para poder seguir solo esto en
 * el archivo. Nunca se registra una contraseña ni un enlace de recuperación: solo el correo
 * enmascarado y la dirección desde donde vino el pedido, que es lo que sirve para investigar sin
 * guardar de más.
 */
@Component
public class BitacoraDeSeguridad {

    private static final Logger log = LoggerFactory.getLogger("kolab.seguridad");

    public void ingreso(String email, String ip) {
        log.info("ingreso correcto · cuenta={} · ip={}", enmascarar(email), ip);
    }

    public void ingresoFallido(String email, String ip, String motivo) {
        log.warn("ingreso fallido · cuenta={} · ip={} · motivo={}", enmascarar(email), ip, motivo);
    }

    public void cuentaBloqueada(String email, String ip, int minutos) {
        log.warn("cuenta bloqueada por intentos fallidos · cuenta={} · ip={} · minutos={}",
                enmascarar(email), ip, minutos);
    }

    public void pidioRecuperar(String email, String ip, boolean existia) {
        log.info("pedido de enlace de recuperacion · cuenta={} · ip={} · existia={}",
                enmascarar(email), ip, existia);
    }

    public void cambioDeClave(String email, String ip) {
        log.info("contrasena cambiada con enlace · cuenta={} · ip={}", enmascarar(email), ip);
    }

    public void noEsSuyo(String ruta, String email, String ip) {
        log.warn("pidio algo ajeno o inexistente · ruta={} · cuenta={} · ip={}",
                ruta, enmascarar(email), ip);
    }

    /**
     * Deja la primera letra y el dominio: {@code ana.ramirez@kolab.pe} queda como {@code a***@kolab.pe}.
     * Alcanza para reconocer a quién se refiere una línea sin dejar la lista de correos en el archivo.
     */
    static String enmascarar(String email) {
        if (email == null || email.isBlank()) {
            return "(sin cuenta)";
        }
        int arroba = email.indexOf('@');
        if (arroba < 1) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(arroba);
    }
}
