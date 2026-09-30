package com.kolab.usuario;

import com.kolab.common.OperacionNoPermitidaException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de {@link RecuperacionService} con HMAC-SHA256. El código lleva el id, el
 * vencimiento y una firma que incluye el hash actual de la contraseña: al cambiarla, la firma deja
 * de coincidir y el enlace muere solo.
 */
@Service
public class RecuperacionServiceImpl implements RecuperacionService {

    private static final Logger log = LoggerFactory.getLogger(RecuperacionServiceImpl.class);
    private static final Duration VIGENCIA = Duration.ofMinutes(30);
    private static final String ALGORITMO = "HmacSHA256";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final byte[] clave;

    public RecuperacionServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                                   @Value("${kolab.clave-enlaces:}") String claveEnlaces) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.clave = claveEnlaces.isBlank() ? claveAlAzar() : claveEnlaces.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> generarCodigo(String email) {
        return usuarioRepository.findByEmail(email.trim().toLowerCase())
                .filter(Usuario::estaActivo)
                .map(u -> {
                    long vence = Instant.now().plus(VIGENCIA).getEpochSecond();
                    String datos = u.getId() + "." + vence;
                    return base64(datos.getBytes(StandardCharsets.UTF_8)) + "." + base64(firma(datos, u));
                });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean esValido(String codigo) {
        return dueno(codigo).isPresent();
    }

    @Override
    @Transactional
    public void cambiarClave(String codigo, String nuevaClave) {
        Usuario u = dueno(codigo).orElseThrow(() -> new OperacionNoPermitidaException(
                "El enlace venció o ya se usó. Pide uno nuevo."));
        u.setPasswordHash(passwordEncoder.encode(nuevaClave));
        log.info("contraseña cambiada por enlace, usuario id={}", u.getId());
    }

    private Optional<Usuario> dueno(String codigo) {
        try {
            String[] partes = codigo.split("\\.");
            if (partes.length != 2) {
                return Optional.empty();
            }
            String datos = new String(Base64.getUrlDecoder().decode(partes[0]), StandardCharsets.UTF_8);
            String[] campos = datos.split("\\.");
            if (Instant.now().getEpochSecond() > Long.parseLong(campos[1])) {
                return Optional.empty();
            }
            byte[] recibida = Base64.getUrlDecoder().decode(partes[1]);
            return usuarioRepository.findById(Long.parseLong(campos[0]))
                    .filter(u -> MessageDigest.isEqual(recibida, firma(datos, u)));
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }

    private byte[] firma(String datos, Usuario u) {
        try {
            Mac mac = Mac.getInstance(ALGORITMO);
            mac.init(new SecretKeySpec(clave, ALGORITMO));
            return mac.doFinal((datos + "." + u.getPasswordHash()).getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo firmar el enlace", e);
        }
    }

    // sin clave configurada los enlaces valen hasta que se reinicie la aplicación
    private static byte[] claveAlAzar() {
        log.warn("kolab.clave-enlaces no está definida: los enlaces de recuperación vencen al reiniciar");
        byte[] azar = new byte[32];
        new SecureRandom().nextBytes(azar);
        return azar;
    }

    private static String base64(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
