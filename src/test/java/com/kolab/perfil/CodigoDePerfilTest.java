package com.kolab.perfil;

import static com.kolab.CuentasDePrueba.CLAVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kolab.solicitud.SolicitudRepository;
import com.kolab.usuario.CodigoPublico;
import com.kolab.usuario.DetallesUsuarioService;
import com.kolab.usuario.RegistroForm;
import com.kolab.usuario.Rol;
import com.kolab.usuario.Usuario;
import com.kolab.usuario.UsuarioAutenticado;
import com.kolab.usuario.UsuarioRepository;
import com.kolab.usuario.UsuarioService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

// el perfil se ve por codigo publico, por id no hay nada
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CodigoDePerfilTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioService usuarioService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PerfilService perfilService;
    @Autowired private DetallesUsuarioService detalles;
    @Autowired private SolicitudRepository solicitudRepository;

    private Usuario luis;
    private UsuarioAutenticado mira;

    @BeforeEach
    void dosCuentas() {
        luis = registrar("luis.codigo@kolab.pe", "Luis", "Mendoza");
        registrar("mira.codigo@kolab.pe", "Ana", "Torres");
        mira = detalles.cargar("mira.codigo@kolab.pe", Rol.USUARIO);
    }

    @Test
    void alRegistrarseRecibeUnCodigoDeDiezCaracteresQueNoSaleDelNombre() {
        assertThat(luis.getCodigo()).matches(CodigoPublico.FORMA).doesNotContain("luis").doesNotContain("mendoza");
        assertThat(usuarioRepository.findByCodigo(luis.getCodigo())).map(Usuario::getId).contains(luis.getId());
    }

    @Test
    void losCodigosNoSeRepitenNiSiguenUnOrden() {
        Set<String> codigos = new HashSet<>();
        for (int i = 0; i < 20_000; i++) {
            String codigo = CodigoPublico.nuevo();
            assertThat(codigo).matches(CodigoPublico.FORMA);
            codigos.add(codigo);
        }
        assertThat(codigos).hasSize(20_000);
    }

    @Test
    void elPerfilResponde404PorIdYSeVePorCodigo() throws Exception {
        mockMvc.perform(get("/personas/{id}", luis.getId()).with(user(mira)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/personas/{codigo}", luis.getCodigo()).with(user(mira)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Luis Mendoza")));
    }

    @Test
    void elCodigoNoCambiaAunqueLaPersonaCambieSuNombre() {
        String antes = luis.getCodigo();
        PerfilForm form = perfilService.formularioDe(luis.getId());
        form.setNombre("Luis Alberto");
        form.setApellidos("Mendoza Quispe");

        perfilService.actualizar(luis.getId(), form);

        assertThat(usuarioRepository.findById(luis.getId()).orElseThrow().getCodigo()).isEqualTo(antes);
    }

    @Test
    void proponerleUnServicioVaPorElCodigoYNoPorElId() throws Exception {
        mockMvc.perform(get("/mis-solicitudes/nueva").param("persona", luis.getCodigo()).with(user(mira)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Dirigida a Luis Mendoza")))
                .andExpect(content().string(containsString(
                        "name=\"codigoDestinatario\" value=\"" + luis.getCodigo() + "\"")));
        mockMvc.perform(get("/mis-solicitudes/nueva").param("persona", String.valueOf(luis.getId())).with(user(mira)))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/mis-solicitudes").with(csrf()).with(user(mira))
                        .param("codigoDestinatario", luis.getCodigo())
                        .param("titulo", "Clases de guitarra para principiante")
                        .param("idCategoria", "1")
                        .param("modalidad", "PRESENCIAL")
                        .param("descripcion", "Quiero aprender desde cero, los sábados en la mañana.")
                        .param("fechaDeseada", LocalDate.now().plusDays(7).toString())
                        .param("precioPropuesto", "80"))
                .andExpect(status().is3xxRedirection());

        assertThat(solicitudRepository.findAll()).anySatisfy(s ->
                assertThat(s.getDestinatario()).extracting(Usuario::getId).isEqualTo(luis.getId()));
    }

    // un enlace nuevo con id se cuela sin darse cuenta, esto lo atrapa
    @Test
    void ningunaPlantillaArmaEnlacesAPerfilesConElId() throws IOException {
        Pattern conId = Pattern.compile("/personas/\\{id}|idPersona");
        List<String> hallazgos;
        try (Stream<Path> archivos = Files.walk(Path.of("src/main/resources/templates"))) {
            hallazgos = archivos.filter(a -> a.toString().endsWith(".html"))
                    .filter(a -> conId.matcher(leer(a)).find())
                    .map(Path::toString)
                    .toList();
        }
        assertThat(hallazgos).isEmpty();
    }

    private static String leer(Path archivo) {
        try {
            return Files.readString(archivo, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private Usuario registrar(String email, String nombre, String apellidos) {
        RegistroForm form = new RegistroForm();
        form.setNombre(nombre);
        form.setApellidos(apellidos);
        form.setEmail(email);
        form.setPassword(CLAVE);
        form.setConfirmacion(CLAVE);
        return usuarioService.registrar(form);
    }
}
