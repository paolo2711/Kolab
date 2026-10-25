package com.kolab.perfil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesRegex;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kolab.common.ColorPrincipal;
import com.kolab.common.FotosSubidas;
import com.kolab.usuario.DetallesUsuarioService;
import com.kolab.usuario.RegistroForm;
import com.kolab.usuario.Rol;
import com.kolab.usuario.Usuario;
import com.kolab.usuario.UsuarioAutenticado;
import com.kolab.usuario.UsuarioRepository;
import com.kolab.usuario.UsuarioService;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

// foto y portada en editar perfil, con las mismas reglas que la foto de una solicitud
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FotoDePerfilTest {

    private static final String EMAIL = "foto.perfil@kolab.pe";

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioService usuarioService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PerfilRepository perfilRepository;
    @Autowired private DetallesUsuarioService detalles;
    @Autowired private FotosSubidas fotos;

    private Usuario yo;
    private UsuarioAutenticado sesion;
    private UsuarioAutenticado mira;

    @BeforeEach
    void dosCuentas() {
        yo = registrar(EMAIL);
        registrar("mira.foto@kolab.pe");
        sesion = detalles.cargar(EMAIL, Rol.USUARIO);
        mira = detalles.cargar("mira.foto@kolab.pe", Rol.USUARIO);
    }

    @Test
    void laFotoSeGuardaConSuColorYLaFranjaLoUsa() throws Exception {
        guardar(multipart("/perfil/editar").file(png("foto", new Color(200, 30, 30))));

        String foto = usuarioRepository.findById(yo.getId()).orElseThrow().getFoto();
        String color = perfilRepository.findByUsuarioId(yo.getId()).orElseThrow().getColorFoto();
        assertThat(foto).matches("[0-9a-f]{32}\\.png");
        assertThat(color).isEqualTo("#c81e1e");
        mockMvc.perform(get("/personas/{codigo}", yo.getCodigo()).with(user(mira)))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("src=\"" + FotosSubidas.RUTA + foto + "\""),
                        containsString("data-fondo=\"" + ColorPrincipal.paraFranja(color) + "\""),
                        not(containsString("k-banner")))));
    }

    @Test
    void conPortadaArribaVaLaPortadaYNoLaFranja() throws Exception {
        guardar(multipart("/perfil/editar").file(png("portada", new Color(30, 90, 160))));

        String portada = perfilRepository.findByUsuarioId(yo.getId()).orElseThrow().getPortada();
        mockMvc.perform(get("/personas/{codigo}", yo.getCodigo()).with(user(mira)))
                .andExpect(content().string(allOf(
                        containsString("class=\"k-banner\" src=\"" + FotosSubidas.RUTA + portada + "\""),
                        not(containsString("k-franja")))));
    }

    @Test
    void quitarLasDosVuelveALaFranjaPorNombre() throws Exception {
        guardar(multipart("/perfil/editar")
                .file(png("foto", new Color(200, 30, 30)))
                .file(png("portada", new Color(30, 90, 160))));
        String foto = usuarioRepository.findById(yo.getId()).orElseThrow().getFoto();
        String portada = perfilRepository.findByUsuarioId(yo.getId()).orElseThrow().getPortada();

        MockMultipartHttpServletRequestBuilder quitar = multipart("/perfil/editar");
        quitar.param("quitarFoto", "true").param("quitarPortada", "true");
        guardar(quitar);

        assertThat(usuarioRepository.findById(yo.getId()).orElseThrow().getFoto()).isNull();
        Perfil perfil = perfilRepository.findByUsuarioId(yo.getId()).orElseThrow();
        assertThat(perfil.getPortada()).isNull();
        assertThat(perfil.getColorFoto()).isNull();
        assertThat(fotos.archivo(foto)).isEmpty();
        assertThat(fotos.archivo(portada)).isEmpty();
        mockMvc.perform(get("/personas/{codigo}", yo.getCodigo()).with(user(mira)))
                .andExpect(content().string(allOf(
                        containsString("class=\"k-franja f" + Personas.franjaDe("Persona Prueba") + "\""),
                        not(containsString("data-fondo")),
                        not(containsString("k-banner")))));
    }

    @Test
    void unaFotoNuevaReemplazaALaAnterior() throws Exception {
        guardar(multipart("/perfil/editar").file(png("foto", new Color(200, 30, 30))));
        String anterior = usuarioRepository.findById(yo.getId()).orElseThrow().getFoto();

        guardar(multipart("/perfil/editar").file(png("foto", new Color(30, 140, 70))));

        assertThat(fotos.archivo(anterior)).isEmpty();
        assertThat(perfilRepository.findByUsuarioId(yo.getId()).orElseThrow().getColorFoto()).isEqualTo("#1e8c46");
    }

    @Test
    void unArchivoQueNoEsImagenNoPasaNiComoFotoNiComoPortada() throws Exception {
        byte[] texto = "no soy una foto".getBytes(StandardCharsets.UTF_8);

        mockMvc.perform(conDatos(multipart("/perfil/editar")
                        .file(new MockMultipartFile("foto", "foto.png", "image/png", texto))
                        .file(new MockMultipartFile("portada", "portada.jpg", "image/jpeg", texto))))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesRegex(
                        "(?s).*La foto tiene que ser una imagen JPG o PNG.*La foto tiene que ser una imagen JPG o PNG.*")));

        assertThat(usuarioRepository.findById(yo.getId()).orElseThrow().getFoto()).isNull();
        assertThat(perfilRepository.findByUsuarioId(yo.getId()).orElseThrow().getPortada()).isNull();
    }

    @Test
    void masDeDosMegasNoPasa() throws Exception {
        byte[] pesada = Arrays.copyOf(png("foto", Color.RED).getBytes(), (int) FotosSubidas.MAXIMO + 1);

        mockMvc.perform(conDatos(multipart("/perfil/editar")
                        .file(new MockMultipartFile("foto", "grande.png", "image/png", pesada))))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("La foto pesa más de 2 MB.")));
    }

    @Test
    void laVistaPreviaArrancaConLoQueYaTiene() throws Exception {
        guardar(multipart("/perfil/editar")
                .file(png("foto", new Color(200, 30, 30)))
                .file(png("portada", new Color(30, 90, 160))));
        String foto = FotosSubidas.url(usuarioRepository.findById(yo.getId()).orElseThrow().getFoto());
        String portada = FotosSubidas.url(perfilRepository.findByUsuarioId(yo.getId()).orElseThrow().getPortada());

        mockMvc.perform(get("/perfil/editar").with(user(sesion)))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        matchesRegex("(?s).*id=\"vpFoto\"[^>]*src=\"" + foto + "\".*"),
                        matchesRegex("(?s).*id=\"vpPortada\"[^>]*src=\"" + portada + "\".*"),
                        containsString("Quitar mi foto"),
                        containsString("Quitar la portada"))));
    }

    private void guardar(MockMultipartHttpServletRequestBuilder pedido) throws Exception {
        mockMvc.perform(conDatos(pedido)).andExpect(status().is3xxRedirection());
    }

    private MockMultipartHttpServletRequestBuilder conDatos(MockMultipartHttpServletRequestBuilder pedido) {
        pedido.param("nombre", "Persona").param("apellidos", "Prueba").with(csrf()).with(user(sesion));
        return pedido;
    }

    private static MockMultipartFile png(String campo, Color color) throws IOException {
        BufferedImage imagen = new BufferedImage(60, 60, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = imagen.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, 60, 60);
        g.dispose();
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        ImageIO.write(imagen, "png", salida);
        return new MockMultipartFile(campo, campo + ".png", "image/png", salida.toByteArray());
    }

    private Usuario registrar(String email) {
        RegistroForm form = new RegistroForm();
        form.setNombre("Persona");
        form.setApellidos("Prueba");
        form.setEmail(email);
        form.setPassword("secreto123");
        form.setConfirmacion("secreto123");
        return usuarioService.registrar(form);
    }
}
