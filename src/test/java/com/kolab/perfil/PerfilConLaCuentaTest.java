package com.kolab.perfil;

import static com.kolab.CuentasDePrueba.CLAVE;
import static org.assertj.core.api.Assertions.assertThat;

import com.kolab.usuario.RegistroForm;
import com.kolab.usuario.UsuarioService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

// el perfil nace una sola vez, al registrarse; lo demas solo lo usa
@SpringBootTest
@Transactional
class PerfilConLaCuentaTest {

    private static final long MUSICA = 1L;

    @Autowired private UsuarioService usuarioService;
    @Autowired private PerfilService perfilService;
    @Autowired private ReputacionService reputacionService;
    @Autowired private DirectorioService directorioService;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void alRegistrarseLaCuentaTieneUnPerfilVacio() {
        Long id = registrar("nueva@kolab.pe");

        assertThat(perfilesDe(id)).isEqualTo(1);
        assertThat(perfilService.categoriasDe(id)).isEmpty();
        assertThat(reputacionService.de(id).sinCalificar()).isTrue();
    }

    @Test
    void categoriasDescripcionYCalificacionUsanElMismoPerfil() {
        Long id = registrar("ofrece@kolab.pe");
        PerfilForm form = perfilService.formularioDe(id);
        form.setDescripcion("Profesora de guitarra hace diez años.");

        perfilService.guardarCategorias(id, List.of(MUSICA));
        perfilService.actualizar(id, form);
        reputacionService.sumarCalificacion(id, 4);

        assertThat(perfilesDe(id)).isEqualTo(1);
        assertThat(perfilService.miPerfil(id).descripcion()).isEqualTo("Profesora de guitarra hace diez años.");
        assertThat(reputacionService.de(id).calificaciones()).isEqualTo(1);
    }

    @Test
    void elDirectorioSoloMuestraAQuienDeclaroAlgunaCategoria() {
        Long ofrece = registrar("con.categoria@kolab.pe");
        Long soloPide = registrar("sin.categoria@kolab.pe");
        perfilService.guardarCategorias(ofrece, List.of(MUSICA));
        reputacionService.sumarCalificacion(ofrece, 5);
        reputacionService.sumarCalificacion(soloPide, 5);

        assertThat(ids(directorioService.mejorCalificados(100))).contains(ofrece).doesNotContain(soloPide);
        assertThat(ids(directorioService.queOfrecen(MUSICA, 100))).contains(ofrece).doesNotContain(soloPide);
    }

    @Test
    void laMigracionLeDaPerfilALasCuentasQueNoTenian() throws IOException {
        Long sinPerfil = registrar("antigua@kolab.pe");
        Long conPerfil = registrar("con.perfil@kolab.pe");
        jdbc.update("delete from perfil where id_usuario = ?", sinPerfil);

        jdbc.update(migracion("V12__perfil_para_cada_cuenta.sql"));

        assertThat(perfilesDe(sinPerfil)).isEqualTo(1);
        assertThat(perfilesDe(conPerfil)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select total_calificaciones from perfil where id_usuario = ?",
                Integer.class, sinPerfil)).isZero();
    }

    private int perfilesDe(Long idUsuario) {
        return jdbc.queryForObject("select count(*) from perfil where id_usuario = ?", Integer.class, idUsuario);
    }

    private static List<Long> ids(List<ExpertoResumen> tarjetas) {
        return tarjetas.stream().map(t -> t.persona().id()).toList();
    }

    private static String migracion(String archivo) throws IOException {
        String sql = new ClassPathResource("db/migration/comun/" + archivo).getContentAsString(StandardCharsets.UTF_8);
        return sql.strip().replaceAll(";$", "");
    }

    private Long registrar(String email) {
        RegistroForm form = new RegistroForm();
        form.setNombre("Persona");
        form.setApellidos("Prueba");
        form.setEmail(email);
        form.setPassword(CLAVE);
        form.setConfirmacion(CLAVE);
        return usuarioService.registrar(form).getId();
    }
}
