package com.kolab.desarrollo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lo que se va sabiendo mientras se cargan los ejemplos: qué id le tocó a cada correo y a cada clave,
 * y qué fechas hay que ajustar al final. Vive lo que dura una carga.
 */
final class CargaEnCurso {

    final Map<String, Long> personas = new HashMap<>();
    final Map<String, Long> solicitudes = new HashMap<>();
    final Map<String, Long> ofertas = new HashMap<>();
    final Map<String, String> autores = new HashMap<>();
    final Map<String, Ejemplos.Oferta> ofertasPorClave = new HashMap<>();
    final List<Fechado.Cambio> fechas = new ArrayList<>();
    final List<Long> mensajesLeidos = new ArrayList<>();

    Long persona(String email) {
        Long id = personas.get(email);
        if (id == null) {
            throw new IllegalStateException("El ejemplo nombra a " + email + ", que no está en personas.json");
        }
        return id;
    }

    Long solicitud(String clave) {
        return buscar(solicitudes, clave, "solicitudes.json");
    }

    Long oferta(String clave) {
        return buscar(ofertas, clave, "ofertas.json");
    }

    private static Long buscar(Map<String, Long> ids, String clave, String archivo) {
        Long id = ids.get(clave);
        if (id == null) {
            throw new IllegalStateException("No hay «" + clave + "» en " + archivo);
        }
        return id;
    }
}
