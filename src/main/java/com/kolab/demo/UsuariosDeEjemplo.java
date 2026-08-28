package com.kolab.demo;

import com.kolab.usuario.RegistroForm;
import com.kolab.usuario.TipoPerfil;
import com.kolab.perfil.PerfilService;
import com.kolab.usuario.UsuarioService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import java.util.List;
import org.springframework.stereotype.Component;

// la base es en memoria, se vacía en cada arranque, así que las cuentas de la demo se
// vuelven a crear aquí. se borra cuando entre la base de verdad.
@Component
public class UsuariosDeEjemplo implements ApplicationRunner {

    private static final String CLAVE = "kolab1234";

    private final UsuarioService usuarioService;
    private final PerfilService perfilService;

    public UsuariosDeEjemplo(UsuarioService usuarioService, PerfilService perfilService) {
        this.usuarioService = usuarioService;
        this.perfilService = perfilService;
    }

    @Override
    public void run(ApplicationArguments args) {
        crear("Paolo", "Rodríguez Paredes", "cliente@kolab.pe", "987654321", TipoPerfil.CLIENTE);
        crear("Luis", "Mendoza Quispe", "experto@kolab.pe", "912345678", TipoPerfil.EXPERTO);

        usuarioService.buscarPorEmail("experto@kolab.pe")
                .ifPresent(luis -> perfilService.guardarCategorias(luis.getId(), List.of(1L, 3L)));
    }

    private void crear(String nombre, String apellidos, String email, String telefono, TipoPerfil tipoPerfil) {
        if (!usuarioService.emailDisponible(email)) {
            return;
        }
        RegistroForm form = new RegistroForm();
        form.setNombre(nombre);
        form.setApellidos(apellidos);
        form.setEmail(email);
        form.setTelefono(telefono);
        form.setPassword(CLAVE);
        form.setConfirmacion(CLAVE);
        form.setTipoPerfil(tipoPerfil);
        usuarioService.registrar(form);
    }
}
