package com.kolab.demo;

import com.kolab.solicitud.EstadoSolicitud;
import com.kolab.solicitud.Modalidad;
import java.math.BigDecimal;
import java.time.LocalDate;

record SolicitudJson(Long id, String titulo, Long categoria, Modalidad modalidad, String distrito,
                     String descripcion, LocalDate fechaDeseada, BigDecimal precio,
                     EstadoSolicitud estado, String publicada, boolean mia, Long persona,
                     Double latitud, Double longitud) {
}
