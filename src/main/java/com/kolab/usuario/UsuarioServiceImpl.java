package com.kolab.usuario;

import com.kolab.common.EmailYaRegistradoException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de {@link UsuarioService}.
 */
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioServiceImpl.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Usuario registrar(RegistroForm form) {
        String email = normalizar(form.getEmail());
        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailYaRegistradoException(email);
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(form.getNombre().trim());
        usuario.setApellidos(form.getApellidos().trim());
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        usuario.setTelefono(vacioComoNulo(form.getTelefono()));
        usuario.setFechaRegistro(LocalDateTime.now());
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuarioRepository.save(usuario);

        log.info("usuario registrado id={}", usuario.getId());
        return usuario;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(normalizar(email));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean emailDisponible(String email) {
        return !usuarioRepository.existsByEmail(normalizar(email));
    }

    // el correo se guarda siempre igual, si no el unique deja pasar Pepe@x.com y pepe@x.com
    private String normalizar(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String vacioComoNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
