package pe.edu.eventos.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Conexión a Supabase (PostgreSQL) con pool de conexiones (HikariCP).
 * Las credenciales viven en resources/application.properties.
 */
public class ConexionDB {

    private static final HikariDataSource dataSource;

    static {
        Properties props = new Properties();
        try (InputStream input = ConexionDB.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("No se encontró application.properties en resources/");
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error al leer application.properties", e);
        }

        String url = String.format(
                "jdbc:postgresql://%s:%s/%s?sslmode=require",
                props.getProperty("db.host"),
                props.getProperty("db.port"),
                props.getProperty("db.database"));

        HikariConfig cfg = new HikariConfig();
        cfg.setDriverClassName("org.postgresql.Driver");
        cfg.setJdbcUrl(url);
        cfg.setUsername(props.getProperty("db.user"));
        cfg.setPassword(props.getProperty("db.password"));

        cfg.setMaximumPoolSize(8);          // el plan gratis de Supabase limita conexiones
        cfg.setMinimumIdle(2);              // mantiene 2 conexiones listas
        cfg.setConnectionTimeout(10_000);   // falla en 10 s en vez de colgarse
        cfg.setIdleTimeout(300_000);        // 5 min
        cfg.setMaxLifetime(1_500_000);      // 25 min (menor al límite del pooler)
        cfg.setInitializationFailTimeout(-1); // no tumba la app si la BD tarda al arrancar
        cfg.setPoolName("inara-pool");

        dataSource = new HikariDataSource(cfg);
    }

    private ConexionDB() {
    }

    public static Connection obtenerConexion() throws SQLException {
        return dataSource.getConnection();
    }

    /** Cierra el pool (se llama al detener la aplicación). */
    public static void cerrar() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
