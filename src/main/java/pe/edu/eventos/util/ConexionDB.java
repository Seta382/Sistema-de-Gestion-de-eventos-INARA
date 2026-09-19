package pe.edu.eventos.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Conexión a Supabase (PostgreSQL) usando el Session pooler.
 * Las credenciales viven en resources/application.properties, NO en el
 * código, para no subir la contraseña al repositorio por accidente.
 */
public class ConexionDB {

    private static final Properties props = new Properties();

    static {
        try (InputStream input = ConexionDB.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("No se encontró application.properties en resources/");
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error al leer application.properties", e);
        }
    }

    private ConexionDB() {
    }

    public static Connection obtenerConexion() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontró el driver JDBC de PostgreSQL en el classpath", e);
        }

        String host = props.getProperty("db.host");
        String port = props.getProperty("db.port");
        String database = props.getProperty("db.database");
        String user = props.getProperty("db.user");
        String password = props.getProperty("db.password");

        // sslmode=require porque Supabase exige conexiones cifradas
        String url = String.format(
                "jdbc:postgresql://%s:%s/%s?sslmode=require",
                host, port, database
        );

        return DriverManager.getConnection(url, user, password);
    }
}
