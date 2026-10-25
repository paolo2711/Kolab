package com.kolab.common;

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
 * las fotos que sube la gente: solicitudes, perfil y portada. van fuera del jar con nombre generado
 * y se revisan por contenido, un .png que por dentro no es imagen no pasa
 */
@Component
public class FotosSubidas {

    public static final String RUTA = "/fotos/subidas/";
    public static final long MAXIMO = 2L * 1024 * 1024;

    private static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
    private static final Pattern NOMBRE = Pattern.compile("[0-9a-f]{32}\\.(jpg|png)");

    private final Path carpeta;

    public FotosSubidas(@Value("${kolab.fotos-subidas}") String carpeta) {
        this.carpeta = Path.of(carpeta).toAbsolutePath().normalize();
    }

    /**
     * por que no sirve el archivo, vacio si sirve o si no se subio nada
     */
    public Optional<String> problemaCon(MultipartFile archivo) {
        if (!subio(archivo)) {
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

    public static boolean subio(MultipartFile archivo) {
        return archivo != null && !archivo.isEmpty();
    }

    /**
     * guarda una foto ya revisada con problemaCon y devuelve el nombre con que quedo
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

    // solo nombres generados aqui, un ../ o cualquier otra cosa no llega al disco
    public Optional<Path> archivo(String nombre) {
        if (nombre == null || !NOMBRE.matcher(nombre).matches()) {
            return Optional.empty();
        }
        Path ruta = carpeta.resolve(nombre);
        return Files.isRegularFile(ruta) ? Optional.of(ruta) : Optional.empty();
    }

    public static String url(String nombre) {
        return nombre == null ? null : RUTA + nombre;
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

    // la firma se copia al inicio de cualquier archivo, que se deje leer como imagen es la prueba
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
