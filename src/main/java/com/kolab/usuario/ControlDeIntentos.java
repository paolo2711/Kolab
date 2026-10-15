package com.kolab.usuario;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Cuenta los intentos fallidos de ingreso de cada correo y bloquea el que insiste.
 *
 * <p>Se apoya en una caché de Guava que vence por escritura: la cuenta se desbloquea sola al pasar
 * los minutos, sin tarea que limpie ni columna en la base. Es a propósito que esto no se guarde: un
 * bloqueo que sobrevive al reinicio deja a alguien fuera sin que nadie pueda levantarlo.
 */
@Component
public class ControlDeIntentos {

    private final Cache<String, Integer> fallidos;
    private final int permitidos;
    private final int minutosDeBloqueo;

    public ControlDeIntentos(@Value("${kolab.ingreso.intentos:5}") int permitidos,
                             @Value("${kolab.ingreso.bloqueo-minutos:15}") int minutosDeBloqueo) {
        this.permitidos = permitidos;
        this.minutosDeBloqueo = minutosDeBloqueo;
        this.fallidos = CacheBuilder.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(minutosDeBloqueo))
                .maximumSize(10_000)
                .build();
    }

    /**
     * Suma un fallo y dice si con ese ya quedó bloqueada. Cada fallo vuelve a escribir la entrada a
     * propósito: así el reloj del vencimiento arranca en el último intento y no en el primero, que
     * es lo que hace que el bloqueo dure los minutos completos desde que se bloqueó.
     */
    public boolean fallo(String email) {
        String clave = clave(email);
        Integer anteriores = fallidos.getIfPresent(clave);
        int cuenta = (anteriores == null ? 0 : anteriores) + 1;
        fallidos.put(clave, cuenta);
        return cuenta >= permitidos;
    }

    public boolean estaBloqueado(String email) {
        Integer cuenta = fallidos.getIfPresent(clave(email));
        return cuenta != null && cuenta >= permitidos;
    }

    /** Un ingreso correcto borra lo anterior: los fallos cuentan solo si son seguidos. */
    public void entro(String email) {
        fallidos.invalidate(clave(email));
    }

    public int minutosDeBloqueo() {
        return minutosDeBloqueo;
    }

    private String clave(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
