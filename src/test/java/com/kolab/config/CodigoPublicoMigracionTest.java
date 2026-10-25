package com.kolab.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kolab.usuario.CodigoPublico;
import java.sql.SQLException;
import java.util.List;
import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

// la V14 sobre una base con cuentas: H2 aparte hasta la V13, se cargan cuentas y recien ahi migra
class CodigoPublicoMigracionTest {

    @Test
    void lasCuentasQueYaExistianRecibenCadaUnaSuCodigo() {
        JdbcDataSource base = new JdbcDataSource();
        base.setURL("jdbc:h2:mem:migracion-codigo;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        base.setUser("sa");
        JdbcTemplate jdbc = new JdbcTemplate(base);

        flyway(base, "13").migrate();
        for (int i = 1; i <= 3; i++) {
            jdbc.update("insert into usuario (nombre, apellidos, email, password_hash, fecha_registro, estado) "
                    + "values ('Persona', 'Antigua', ?, 'hash', current_timestamp, 'ACTIVO')", "antigua" + i + "@kolab.pe");
        }

        flyway(base, "14").migrate();

        List<String> codigos = jdbc.queryForList("select codigo from usuario", String.class);
        assertThat(codigos).hasSize(3).doesNotHaveDuplicates().allMatch(c -> CodigoPublico.FORMA.matcher(c).matches());
        assertThatThrownBy(() -> jdbc.update("update usuario set codigo = null"))
                .hasRootCauseInstanceOf(SQLException.class);
        assertThatThrownBy(() -> jdbc.update("update usuario set codigo = ?", codigos.get(0)))
                .hasRootCauseInstanceOf(SQLException.class);
    }

    private static Flyway flyway(JdbcDataSource base, String hasta) {
        return Flyway.configure()
                .dataSource(base)
                .locations("classpath:db/migration/comun")
                .target(hasta)
                .load();
    }
}
