package com.kolab.servicio.impl;

import com.kolab.modelo.dto.PerfilForm;
import com.kolab.modelo.entidad.Perfil;
import com.kolab.modelo.entidad.Usuario;
import com.kolab.util.ColorPrincipal;
import com.kolab.util.FotosSubidas;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * la foto y la portada que la persona sube o quita en editar perfil. al subir la foto se guarda su
 * color principal para la franja
 */
@Component
class FotosDePerfil {

    private final FotosSubidas subidas;

    FotosDePerfil(FotosSubidas subidas) {
        this.subidas = subidas;
    }

    void aplicar(PerfilForm form, Usuario usuario, Perfil perfil) {
        if (FotosSubidas.subio(form.getFoto())) {
            String nueva = reemplazar(usuario.getFoto(), form.getFoto());
            usuario.setFoto(nueva);
            perfil.setColorFoto(subidas.archivo(nueva).flatMap(ColorPrincipal::de).orElse(null));
        } else if (form.isQuitarFoto()) {
            subidas.borrar(usuario.getFoto());
            usuario.setFoto(null);
            perfil.setColorFoto(null);
        }

        if (FotosSubidas.subio(form.getPortada())) {
            perfil.setPortada(reemplazar(perfil.getPortada(), form.getPortada()));
        } else if (form.isQuitarPortada()) {
            subidas.borrar(perfil.getPortada());
            perfil.setPortada(null);
        }
    }

    private String reemplazar(String anterior, MultipartFile archivo) {
        String nueva = subidas.guardar(archivo);
        subidas.borrar(anterior);
        return nueva;
    }
}
