package com.kolab.usuario;

import com.kolab.common.EmailYaRegistradoException;
import java.util.Optional;

/**
 * altas y consultas de cuentas, lo unico que escribe en la tabla usuario
 */
public interface UsuarioService {

    /**
     * crea la cuenta con la clave en BCrypt, el correo en minusculas y sin espacios, su perfil
     * vacio y su codigo publico
     *
     * @throws EmailYaRegistradoException si ese correo ya tiene cuenta
     */
    Usuario registrar(RegistroForm form);

    /**
     * busca por correo sin importar mayusculas
     */
    Optional<Usuario> buscarPorEmail(String email);

    boolean emailDisponible(String email);

    /**
     * si la cuenta esta suspendida ahora, se pregunta en cada pedido con sesion
     */
    boolean estaSuspendida(Long idUsuario);
}
