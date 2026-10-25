package com.kolab.solicitud;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesRegex;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kolab.common.FotosSubidas;
import com.kolab.usuario.DetallesUsuarioService;
import com.kolab.usuario.RegistroForm;
import com.kolab.usuario.Rol;
import com.kolab.usuario.UsuarioAutenticado;
import com.kolab.usuario.UsuarioService;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

// la foto propia de una solicitud: se revisa por lo que trae adentro, se guarda con nombre propio y
// sin ella se sigue usando la de la categoria
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FotoDeSolicitudTest {

    private static final String EMAIL = "foto.solicitud@kolab.pe";

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioService usuarioService;
    @Autowired private DetallesUsuarioService detalles;
    @Autowired private SolicitudRepository solicitudRepository;
    @Autowired private FotosSubidas fotos;

    private UsuarioAutenticado yo;

    @BeforeEach
    void unaCuenta() {
        RegistroForm form = new RegistroForm();
        form.setNombre("Persona");
        form.setApellidos("Prueba");
        form.setEmail(EMAIL);
        form.setPassword("secreto123");
        form.setConfirmacion("secreto123");
        usuarioService.registrar(form);
        yo = detalles.cargar(EMAIL, Rol.USUARIO);
    }

    @Test
    void unaFotoPngSeGuardaConNombreGeneradoYSeSirve() throws Exception {
        byte[] png = imagen("png");

        Long id = publicar(new MockMultipartFile("foto", "mi foto.png", "image/png", png));

        String nombre = solicitudRepository.findById(id).orElseThrow().getFoto();
        assertThat(nombre).matches("[0-9a-f]{32}\\.png").doesNotContain("mi foto");
        mockMvc.perform(get(FotosSubidas.RUTA + nombre))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"))
                .andExpect(content().bytes(png));
        mockMvc.perform(get("/mis-solicitudes/{id}", id).with(user(yo)))
                .andExpect(content().string(containsString(FotosSubidas.RUTA + nombre)));
    }

    @Test
    void unJpgTambienSirve() throws Exception {
        Long id = publicar(new MockMultipartFile("foto", "foto.jpeg", "image/jpeg", imagen("jpg")));

        assertThat(solicitudRepository.findById(id).orElseThrow().getFoto()).endsWith(".jpg");
    }

    @Test
    void sinFotoSeUsaLaDeLaCategoria() throws Exception {
        Long id = publicar(null);

        assertThat(solicitudRepository.findById(id).orElseThrow().getFoto()).isNull();
        mockMvc.perform(get("/mis-solicitudes/{id}", id).with(user(yo)))
                .andExpect(content().string(containsString("/img/categoria/musica.jpg")));
    }

    @Test
    void unArchivoQueNoEsImagenNoPasaAunqueDigaPng() throws Exception {
        MockMultipartFile texto = new MockMultipartFile("foto", "foto.png", "image/png",
                "esto es un archivo de texto".getBytes(StandardCharsets.UTF_8));

        rechazada(texto, "La foto tiene que ser una imagen JPG o PNG.");
    }

    @Test
    void laFirmaDePngSolaNoAlcanza() throws Exception {
        byte[] disfrazado = Arrays.copyOf(new byte[] {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'}, 200);

        rechazada(new MockMultipartFile("foto", "foto.png", "image/png", disfrazado),
                "La foto tiene que ser una imagen JPG o PNG.");
    }

    @Test
    void unaImagenDeOtroFormatoNoPasa() throws Exception {
        rechazada(new MockMultipartFile("foto", "foto.gif", "image/gif", imagen("gif")),
                "La foto tiene que ser una imagen JPG o PNG.");
    }

    @Test
    void masDeDosMegasNoPasa() throws Exception {
        byte[] pesada = Arrays.copyOf(imagen("png"), (int) FotosSubidas.MAXIMO + 1);

        rechazada(new MockMultipartFile("foto", "grande.png", "image/png", pesada), "La foto pesa más de 2 MB.");
    }

    @Test
    void alEditarUnaFotoNuevaReemplazaALaAnterior() throws Exception {
        Long id = publicar(new MockMultipartFile("foto", "a.png", "image/png", imagen("png")));
        String anterior = solicitudRepository.findById(id).orElseThrow().getFoto();

        mockMvc.perform(conDatos(multipart("/mis-solicitudes/{id}/editar", id)
                        .file(new MockMultipartFile("foto", "b.jpg", "image/jpeg", imagen("jpg")))))
                .andExpect(status().is3xxRedirection());

        String nueva = solicitudRepository.findById(id).orElseThrow().getFoto();
        assertThat(nueva).endsWith(".jpg").isNotEqualTo(anterior);
        assertThat(fotos.archivo(anterior)).isEmpty();
        assertThat(fotos.archivo(nueva)).isPresent();
    }

    @Test
    void alEditarSinSubirOtraSeQuedaLaQueTenia() throws Exception {
        Long id = publicar(new MockMultipartFile("foto", "a.png", "image/png", imagen("png")));
        String antes = solicitudRepository.findById(id).orElseThrow().getFoto();

        mockMvc.perform(conDatos(multipart("/mis-solicitudes/{id}/editar", id)))
                .andExpect(status().is3xxRedirection());

        assertThat(solicitudRepository.findById(id).orElseThrow().getFoto()).isEqualTo(antes);
    }

    @Test
    void alEditarLaVistaPreviaArrancaConLaFotoQueYaTiene() throws Exception {
        Long id = publicar(new MockMultipartFile("foto", "a.png", "image/png", imagen("png")));
        String propia = FotosSubidas.RUTA + solicitudRepository.findById(id).orElseThrow().getFoto();

        mockMvc.perform(get("/mis-solicitudes/{id}/editar", id).with(user(yo)))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("data-foto-propia=\"" + propia + "\""),
                        matchesRegex("(?s).*id=\"vpFoto\"[^>]*src=\"" + propia + "\".*"))));
    }

    @Test
    void unNombreQueNoGeneroLaAplicacionNoLlegaAlDisco() throws Exception {
        mockMvc.perform(get(FotosSubidas.RUTA + "pom.xml")).andExpect(status().isNotFound());
        mockMvc.perform(get(FotosSubidas.RUTA + "no-existe.png")).andExpect(status().isNotFound());
        assertThat(fotos.archivo("../../pom.xml")).isEmpty();
    }

    private void rechazada(MockMultipartFile archivo, String mensaje) throws Exception {
        long antes = solicitudRepository.count();

        mockMvc.perform(conDatos(multipart("/mis-solicitudes").file(archivo)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(mensaje)));

        assertThat(solicitudRepository.count()).isEqualTo(antes);
    }

    private Long publicar(MockMultipartFile foto) throws Exception {
        MockMultipartHttpServletRequestBuilder pedido = multipart("/mis-solicitudes");
        if (foto != null) {
            pedido.file(foto);
        }
        String destino = mockMvc.perform(conDatos(pedido))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();
        return Long.valueOf(destino.substring(destino.lastIndexOf('/') + 1));
    }

    private MockMultipartHttpServletRequestBuilder conDatos(MockMultipartHttpServletRequestBuilder pedido) {
        pedido.param("titulo", "Clases de cajón peruano")
                .param("idCategoria", "1")
                .param("modalidad", "PRESENCIAL")
                .param("distrito", "Jesús María")
                .param("descripcion", "Quiero aprender lo básico del cajón, dos veces por semana.")
                .param("fechaDeseada", LocalDate.now().plusDays(7).toString())
                .param("precioPropuesto", "80")
                .with(csrf()).with(user(yo));
        return pedido;
    }

    private static byte[] imagen(String formato) throws IOException {
        BufferedImage imagen = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        imagen.setRGB(3, 3, 0x1a56a8);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        ImageIO.write(imagen, formato, salida);
        return salida.toByteArray();
    }
}
