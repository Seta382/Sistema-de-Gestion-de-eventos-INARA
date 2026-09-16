package pe.edu.eventos.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

    private static final String URL =
            System.getenv("SUPABASE_DB_URL");

    private static final String USER =
            System.getenv("SUPABASE_DB_USER");

    private static final String PASSWORD =
            System.getenv("SUPABASE_DB_PASSWORD");

    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(
                URL,
                USER,
                PASSWORD
        );
    }

}
