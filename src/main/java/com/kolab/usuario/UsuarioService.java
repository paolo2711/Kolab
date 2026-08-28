package com.kolab.usuario;

import java.util.Optional;

public interface UsuarioService {

    Usuario registrar(RegistroForm form);

    Optional<Usuario> buscarPorEmail(String email);

    boolean emailDisponible(String email);
}
