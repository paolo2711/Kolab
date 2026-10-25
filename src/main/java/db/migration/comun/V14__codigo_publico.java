package db.migration.comun;

import com.kolab.usuario.CodigoPublico;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/**
 * Cada cuenta recibe su código público. Va en Java y no en SQL porque el código sale del mismo
 * generador seguro que usa el registro, y así corre igual en PostgreSQL y en H2.
 */
public class V14__codigo_publico extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws SQLException {
        Connection conexion = context.getConnection();
        try (Statement sql = conexion.createStatement()) {
            sql.execute("alter table usuario add column codigo varchar(" + CodigoPublico.LARGO + ")");
        }

        Set<String> usados = new HashSet<>();
        try (PreparedStatement poner = conexion.prepareStatement(
                "update usuario set codigo = ? where id_usuario = ?")) {
            for (Long id : cuentas(conexion)) {
                String codigo = CodigoPublico.nuevo();
                while (!usados.add(codigo)) {
                    codigo = CodigoPublico.nuevo();
                }
                poner.setString(1, codigo);
                poner.setLong(2, id);
                poner.executeUpdate();
            }
        }

        try (Statement sql = conexion.createStatement()) {
            sql.execute("alter table usuario alter column codigo set not null");
            sql.execute("alter table usuario add constraint uk_usuario_codigo unique (codigo)");
        }
    }

    private static List<Long> cuentas(Connection conexion) throws SQLException {
        List<Long> ids = new ArrayList<>();
        try (Statement sql = conexion.createStatement();
             ResultSet filas = sql.executeQuery("select id_usuario from usuario")) {
            while (filas.next()) {
                ids.add(filas.getLong(1));
            }
        }
        return ids;
    }
}
