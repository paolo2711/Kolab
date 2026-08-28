package com.kolab.demo;

import com.kolab.categoria.CategoriaResumen;
import com.kolab.categoria.PreciosDeCategoria;
import com.kolab.solicitud.EstadoSolicitud;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CatalogoDeCategorias {

    private final ArchivosDeDatos archivos;

    public CatalogoDeCategorias(ArchivosDeDatos archivos) {
        this.archivos = archivos;
    }

    public List<CategoriaResumen> categorias() {
        return archivos.categorias().stream()
                .map(c -> new CategoriaResumen(c.id(), c.nombre(), c.icono(), c.tema(),
                        c.precioMinimo(), c.precioMaximo(), abiertasEn(c.id())))
                .toList();
    }

    public CategoriaResumen categoria(Long id) {
        CategoriaJson c = archivos.categoria(id);
        return new CategoriaResumen(c.id(), c.nombre(), c.icono(), c.tema(),
                c.precioMinimo(), c.precioMaximo(), abiertasEn(c.id()));
    }

    // lo que se está pagando de verdad sale de lo que la gente publicó, no de una tabla de tarifas
    public PreciosDeCategoria preciosDe(Long idCategoria) {
        CategoriaJson c = archivos.categoria(idCategoria);
        List<BigDecimal> montos = archivos.solicitudes().stream()
                .filter(s -> s.categoria().equals(idCategoria))
                .map(SolicitudJson::precio)
                .toList();
        return PreciosDeCategoria.de(montos, c.precioMinimo(), c.precioMaximo());
    }

    public long abiertasEn(Long idCategoria) {
        return archivos.solicitudes().stream()
                .filter(s -> !s.mia() && s.estado() == EstadoSolicitud.ABIERTA)
                .filter(s -> s.categoria().equals(idCategoria))
                .count();
    }

    String nombreDe(Long id) {
        return archivos.categoria(id).nombre();
    }
}
