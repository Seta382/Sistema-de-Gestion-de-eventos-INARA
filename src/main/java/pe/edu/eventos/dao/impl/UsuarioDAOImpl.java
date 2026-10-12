package pe.edu.eventos.dao.impl;

import pe.edu.eventos.dao.UsuarioDAO;
import pe.edu.eventos.model.Usuario;
import pe.edu.eventos.util.ConexionDB;
import pe.edu.eventos.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación JDBC de UsuarioDAO para PostgreSQL en Supabase.
 */
public class UsuarioDAOImpl implements UsuarioDAO {

    @Override
    public Usuario autenticar(String correo, String password) {
        Usuario usuario = buscarPorCorreo(correo);
        if (usuario != null && PasswordUtil.verificarPassword(password, usuario.getPassword())) {
            return usuario;
        }
        return null;
    }

    @Override
    public Usuario buscarPorCorreo(String correo) {
        String sql = "SELECT id, nombre, apellido, correo, password, rol, telefono, estado, creado_en "
                + "FROM usuario WHERE LOWER(correo) = LOWER(?)";
        try (Connection conn = ConexionDB.obtenerConexion(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, correo != null ? correo.trim() : "");

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en UsuarioDAOImpl.buscarPorCorreo: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Usuario buscarPorId(int id) {
        String sql = "SELECT id, nombre, apellido, correo, password, rol, telefono, estado, creado_en "
                + "FROM usuario WHERE id = ?";
        try (Connection conn = ConexionDB.obtenerConexion(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en UsuarioDAOImpl.buscarPorId: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean registrar(Usuario usuario) {
        String sql = "INSERT INTO usuario (nombre, apellido, correo, password, rol, telefono) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionDB.obtenerConexion(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getApellido());
            ps.setString(3, usuario.getCorreo().trim().toLowerCase());
            ps.setString(4, usuario.getPassword());
            ps.setString(5, usuario.getRol() != null ? usuario.getRol() : "CLIENTE");
            ps.setString(6, usuario.getTelefono());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        usuario.setId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error en UsuarioDAOImpl.registrar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, apellido, correo, password, rol, telefono, estado, creado_en "
                + "FROM usuario ORDER BY id";
        try (Connection conn = ConexionDB.obtenerConexion(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearUsuario(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en UsuarioDAOImpl.listarTodos: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("creado_en");
        Usuario u = new Usuario(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("correo"),
                rs.getString("password"),
                rs.getString("rol"),
                rs.getString("telefono"),
                timestamp != null ? timestamp.toLocalDateTime() : null
        );
        u.setEstado(rs.getString("estado"));
        return u;
    }

    @Override
    public boolean verificarDatosRecuperacion(
            String nombreCompleto,
            String dni,
            String telefono,
            String correo) {

        String sql = "SELECT COUNT(*) "
                + "FROM public.usuario u "
                + "INNER JOIN public.cliente c ON c.id_usuario = u.id "
                + "WHERE LOWER(TRIM(u.nombre || ' ' || u.apellido)) = LOWER(TRIM(?)) "
                + "AND TRIM(c.dni) = TRIM(?) "
                + "AND TRIM(u.telefono) = TRIM(?) "
                + "AND LOWER(TRIM(u.correo)) = LOWER(TRIM(?))";

        try (Connection conn = ConexionDB.obtenerConexion(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombreCompleto);
            ps.setString(2, dni);
            ps.setString(3, telefono);
            ps.setString(4, correo);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error verificando datos de recuperación: "
                    + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean actualizarPassword(int idUsuario, String password) {

        String sql = "UPDATE public.usuario SET password = ? WHERE id = ?";

        try (Connection conn = ConexionDB.obtenerConexion(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, PasswordUtil.hashPassword(password));
            ps.setInt(2, idUsuario);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error actualizando contraseña: "
                    + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
