package com.kolab.perfil;

import com.kolab.calificacion.ResenaResumen;
import java.math.BigDecimal;
import java.util.List;

public record PerfilResumen(String descripcion,
                            String ubicacion,
                            String distrito,
                            Double latitud,
                            Double longitud,
                            String miembroDesde,
                            BigDecimal calificacion,
                            int calificaciones,
                            int serviciosCerrados,
                            int solicitudesPublicadas,
                            int tasaRespuesta,
                            List<ResenaResumen> resenas) {

    public boolean sinCalificar() {
        return calificaciones == 0;
    }
}
