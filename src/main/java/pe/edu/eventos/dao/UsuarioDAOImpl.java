package pe.edu.eventos.dao.impl;

import pe.edu.eventos.dao.UsuarioDAO;
import pe.edu.eventos.model.Usuario;
import pe.edu.eventos.util.ConexionDB;
import pe.edu.eventos.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        String sql = "SELECT id, nombre, apellido, correo, password, rol, telefono, creado_en " +
                     "FROM usuario WHERE LOWER(correo) = LOWER(?)";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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
        String sql = "SELECT id, nombre, apellido, correo, password, rol, telefono, creado_en " +
                     "FROM usuario WHERE id = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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
        String sql = "INSERT INTO usuario (nombre, apellido, correo, password, rol, telefono) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getApellido());
            ps.setString(3, usuario.getCorreo().trim().toLowerCase());
            ps.setString(4, usuario.getPassword());
            ps.setString(5, usuario.getRol() != null ? usuario.getRol() : "CLIENTE");
            ps.setString(6, usuario.getTelefono());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error en UsuarioDAOImpl.registrar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, apellido, correo, password, rol, telefono, creado_en " +
                     "FROM usuario ORDER BY id";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

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
        return new Usuario(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("correo"),
                rs.getString("password"),
                rs.getString("rol"),
                rs.getString("telefono"),
                timestamp != null ? timestamp.toLocalDateTime() : null
        );
    }
}
