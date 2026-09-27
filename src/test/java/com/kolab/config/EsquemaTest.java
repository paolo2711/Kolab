package com.kolab.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// arranca el contexto completo: Flyway corre las ocho migraciones y, como ddl-auto esta en
// validate, Hibernate compara cada entidad contra el esquema. si una migracion y una entidad
// dejan de coincidir, esta prueba se cae antes de que lo haga la aplicacion.
@SpringBootTest
class EsquemaTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void lasMigracionesCreanLasNueveTablasYLaVista() throws Exception {
        assertThat(unEntero("select count(*) from information_schema.tables "
                + "where table_schema = 'PUBLIC' and table_type = 'BASE TABLE' "
                + "and table_name in ('USUARIO','PERFIL','CATEGORIA','PERFIL_CATEGORIA','SOLICITUD',"
                + "'OFERTA','SERVICIO','MENSAJE','CALIFICACION')")).isEqualTo(9);

        assertThat(unEntero("select count(*) from information_schema.views "
                + "where table_name = 'V_PRECIOS_CATEGORIA'")).isEqualTo(1);
    }

    @Test
    void elCatalogoDeCategoriasQuedaSembradoYActivo() throws Exception {
        assertThat(unEntero("select count(*) from categoria")).isEqualTo(8);
        assertThat(unEntero("select count(*) from categoria where estado = 'ACTIVA'")).isEqualTo(8);
    }

    // sin solicitudes la vista tiene que caer al rango de referencia de la categoria
    @Test
    void laVistaDePreciosUsaElRangoDeReferenciaCuandoNoHaySolicitudes() throws Exception {
        assertThat(unEntero("select solicitudes from v_precios_categoria where id_categoria = 1"))
                .isZero();
        assertThat(unDecimal("select precio_bajo from v_precios_categoria where id_categoria = 1"))
                .isEqualByComparingTo("50");
        assertThat(unDecimal("select precio_tipico from v_precios_categoria where id_categoria = 1"))
                .isEqualByComparingTo("85");
        assertThat(unDecimal("select precio_alto from v_precios_categoria where id_categoria = 1"))
                .isEqualByComparingTo("120");
    }

    private int unEntero(String sql) throws Exception {
        return unaCelda(sql).intValue();
    }

    private BigDecimal unDecimal(String sql) throws Exception {
        return unaCelda(sql);
    }

    private BigDecimal unaCelda(String sql) throws Exception {
        try (Connection c = dataSource.getConnection();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(sql)) {
            r.next();
            return r.getBigDecimal(1);
        }
    }
}
