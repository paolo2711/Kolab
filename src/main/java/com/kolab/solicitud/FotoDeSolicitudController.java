package com.kolab.solicitud;

import java.time.Duration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// las fotos subidas viven fuera del jar, asi que no las sirve el manejo de estaticos sino esto
@Controller
public class FotoDeSolicitudController {

    private final FotosDeSolicitud fotos;

    public FotoDeSolicitudController(FotosDeSolicitud fotos) {
        this.fotos = fotos;
    }

    // el nombre es unico y una foto no cambia: el navegador la puede guardar sin preguntar de nuevo
    @GetMapping(FotosDeSolicitud.RUTA + "{nombre}")
    public ResponseEntity<Resource> foto(@PathVariable String nombre) {
        return fotos.archivo(nombre)
                .map(archivo -> ResponseEntity.ok()
                        .contentType(nombre.endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG)
                        .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic().immutable())
                        .<Resource>body(new FileSystemResource(archivo)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
