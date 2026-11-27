package com.kolab.servicio.impl;

import com.kolab.modelo.dto.Monitoreo;
import com.kolab.servicio.MonitoreoService;
import com.kolab.servicio.RevisionDeSaludService;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementación de {@link RevisionDeSaludService}: usa la misma lectura de la pantalla Monitoreo y deja una
 * línea INFO con el estado, más un WARN por cada aviso. Las dos se ven en el Historial.
 */
@Service
public class RevisionDeSaludServiceImpl implements RevisionDeSaludService {

    private static final Logger log = LoggerFactory.getLogger("kolab.mantenimiento");
    private static final int LIMITE = 80;

    private final MonitoreoService monitoreo;

    public RevisionDeSaludServiceImpl(MonitoreoService monitoreo) {
        this.monitoreo = monitoreo;
    }

    @Override
    public List<String> revisar() {
        Monitoreo lectura = monitoreo.leer();
        Monitoreo.Sistema sistema = lectura.sistema();
        log.info("revision programada · peticiones={} · memoria={}% · disco={}% · base={}",
                lectura.aplicacion().peticiones(), sistema.porcentajeMemoria(), sistema.porcentajeDisco(),
                lectura.baseDeDatos().estado());

        List<String> avisos = new ArrayList<>();
        if (sistema.porcentajeMemoria() > LIMITE) {
            avisos.add("la memoria del servidor esta al " + sistema.porcentajeMemoria() + "%");
        }
        if (sistema.porcentajeDisco() > LIMITE) {
            avisos.add("el disco esta al " + sistema.porcentajeDisco() + "%");
        }
        if (!"UP".equals(lectura.baseDeDatos().estado())) {
            avisos.add("la base de datos no responde");
        }
        avisos.forEach(aviso -> log.warn("revision programada · {}", aviso));
        return avisos;
    }
}
