package com.kolab.usuario;

import com.kolab.common.EmailYaRegistradoException;
import java.util.Optional;

/**
 * Altas y consultas de cuentas. Es lo único del proyecto que escribe en la tabla {@code usuario}.
 */
public interface UsuarioService {

    /**
     * Crea la cuenta con la contraseña cifrada en BCrypt. El correo se guarda en minúsculas y sin
     * espacios, si no el unique deja pasar el mismo correo dos veces. El perfil se crea aquí mismo,
     * vacío: es el único lugar donde nace.
     *
     * @throws EmailYaRegistradoException si ese correo ya tiene cuenta
     */
    Usuario registrar(RegistroForm form);

    /**
     * Busca por correo, sin distinguir mayúsculas.
     */
    Optional<Usuario> buscarPorEmail(String email);

    boolean emailDisponible(String email);

    /**
     * Si la cuenta está suspendida en este momento. Se pregunta en cada pedido con sesión, así que
     * es una sola consulta por id.
     */
    boolean estaSuspendida(Long idUsuario);
}
