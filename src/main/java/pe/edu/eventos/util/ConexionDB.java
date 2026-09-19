package pe.edu.eventos.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
/**
 * pruebas con supabase. actualizar conexiones
 */
public class TestConexion {

    public static void main(String[] args) {
        try (Connection con = ConexionDB.obtenerConexion()) {

            System.out.println("Conexión a Supabase exitosa.");

            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) AS total FROM usuario")) {
                if (rs.next()) {
                    System.out.println("Filas en la tabla usuario: " + rs.getInt("total"));
                }
            }

        } catch (SQLException e) {
            System.out.println("Fallo la conexión o la consulta. Revisa lo siguiente:");
            System.out.println("- application.properties (host/usuario/password correctos)");
            System.out.println("- Que ya hayas corrido 01_create_usuario.sql en el SQL Editor de Supabase");
            e.printStackTrace();
        }
    }
}
