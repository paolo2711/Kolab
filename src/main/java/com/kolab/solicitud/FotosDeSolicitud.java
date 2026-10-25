package com.kolab.solicitud;

import com.kolab.common.Fotos;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Las fotos que sube quien publica una solicitud. Se guardan en una carpeta fuera del jar con un
 * nombre generado, nunca con el que traía el archivo, y se revisan por su contenido: un .png que por
 * dentro no es una imagen no pasa. Sin foto propia, la solicitud usa la de su categoría.
 */
@Component
public class FotosDeSolicitud {

    public static final String RUTA = "/fotos/solicitudes/";
    static final long MAXIMO = 2L * 1024 * 1024;

    private static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
    private static final Pattern NOMBRE = Pattern.compile("[0-9a-f]{32}\\.(jpg|png)");

    private final Path carpeta;
    private final Fotos fotos;

    public FotosDeSolicitud(@Value("${kolab.fotos-subidas}") String carpeta, Fotos fotos) {
        this.carpeta = Path.of(carpeta).toAbsolutePath().normalize();
        this.fotos = fotos;
    }

    /**
     * Por qué no sirve el archivo, o vacío si sirve. Si no se subió nada tampoco hay problema: la
     * foto es opcional.
     */
    public Optional<String> problemaCon(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            return Optional.empty();
        }
        if (archivo.getSize() > MAXIMO) {
            return Optional.of("La foto pesa más de 2 MB.");
        }
        byte[] contenido = leer(archivo);
        if (extensionDe(contenido) == null || !esImagen(contenido)) {
            return Optional.of("La foto tiene que ser una imagen JPG o PNG.");
        }
        return Optional.empty();
    }

    /**
     * Guarda una foto ya revisada con {@link #problemaCon} y devuelve el nombre con que quedó.
     */
    public String guardar(MultipartFile archivo) {
        byte[] contenido = leer(archivo);
        String nombre = UUID.randomUUID().toString().replace("-", "") + "." + extensionDe(contenido);
        try {
            Files.createDirectories(carpeta);
            Files.write(carpeta.resolve(nombre), contenido);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return nombre;
    }

    public void borrar(String nombre) {
        archivo(nombre).ifPresent(a -> {
            try {
                Files.deleteIfExists(a);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    // solo nombres generados aqui: un ../ o cualquier otra cosa no llega al disco
    public Optional<Path> archivo(String nombre) {
        if (nombre == null || !NOMBRE.matcher(nombre).matches()) {
            return Optional.empty();
        }
        Path ruta = carpeta.resolve(nombre);
        return Files.isRegularFile(ruta) ? Optional.of(ruta) : Optional.empty();
    }

    /**
     * La dirección de la foto que se muestra: la propia si la tiene, si no la de su categoría.
     */
    public String de(Solicitud s) {
        String propia = propia(s);
        return propia != null ? propia : fotos.deCategoria(s.getCategoria().getNombre());
    }

    // null si usa la de su categoria
    public String propia(Solicitud s) {
        return s.getFoto() == null ? null : RUTA + s.getFoto();
    }

    private static String extensionDe(byte[] contenido) {
        if (empiezaCon(contenido, JPG)) {
            return "jpg";
        }
        return empiezaCon(contenido, PNG) ? "png" : null;
    }

    private static boolean empiezaCon(byte[] contenido, byte[] firma) {
        return contenido.length >= firma.length && Arrays.equals(contenido, 0, firma.length, firma, 0, firma.length);
    }

    // la firma se puede copiar al inicio de cualquier archivo: que se deje leer como imagen es la prueba
    private static boolean esImagen(byte[] contenido) {
        try {
            return ImageIO.read(new ByteArrayInputStream(contenido)) != null;
        } catch (IOException | RuntimeException e) {
            return false;
        }
    }

    private static byte[] leer(MultipartFile archivo) {
        try {
            return archivo.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
