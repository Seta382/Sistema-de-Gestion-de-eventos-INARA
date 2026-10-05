package pe.edu.eventos.dao.impl;

import pe.edu.eventos.dao.EmpleadoDAO;
import pe.edu.eventos.model.Empleado;
import pe.edu.eventos.model.Usuario;
import pe.edu.eventos.util.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoDAOImpl implements EmpleadoDAO {

    private static final String SELECT_BASE =
            "SELECT e.id_empleado, e.id_usuario, e.dni, e.cargo, e.area, " +
                    "u.nombre, u.apellido, u.correo, u.rol, u.telefono, u.estado " +
                    "FROM empleado e " +
                    "JOIN usuario u ON e.id_usuario = u.id ";

    @Override
    public boolean registrarEmpleado(Usuario usuario, Empleado empleado) {
        String sqlUsuario = "INSERT INTO usuario (nombre, apellido, correo, password, rol, telefono, estado) " +
                "VALUES (?, ?, ?, ?, 'EMPLEADO', ?, 'ACTIVO')";
        String sqlEmpleado = "INSERT INTO empleado (id_usuario, dni, cargo, area) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = ConexionDB.obtenerConexion();
            conn.setAutoCommit(false);

            int idUsuarioGenerado;
            try (PreparedStatement psUser = conn.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                psUser.setString(1, usuario.getNombre());
                psUser.setString(2, usuario.getApellido());
                psUser.setString(3, usuario.getCorreo().trim().toLowerCase());
                psUser.setString(4, usuario.getPassword());
                psUser.setString(5, usuario.getTelefono());

                int filasUser = psUser.executeUpdate();
                if (filasUser == 0) {
                    throw new SQLException("Error al insertar el usuario: no se afectaron filas.");
                }

                try (ResultSet rsKeys = psUser.getGeneratedKeys()) {
                    if (rsKeys.next()) {
                        idUsuarioGenerado = rsKeys.getInt(1);
                    } else {
                        throw new SQLException("No se pudo obtener el ID generado automáticamente.");
                    }
                }
            }

            try (PreparedStatement psEmp = conn.prepareStatement(sqlEmpleado)) {
                psEmp.setInt(1, idUsuarioGenerado);
                psEmp.setString(2, empleado.getDni() != null ? empleado.getDni().trim() : null);
                psEmp.setString(3, empleado.getCargo() != null ? empleado.getCargo().trim() : null);
                psEmp.setString(4, empleado.getArea() != null ? empleado.getArea().trim() : null);

                int filasEmp = psEmp.executeUpdate();
                if (filasEmp == 0) {
                    throw new SQLException("Error al insertar empleado.");
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) {
                    System.err.println("Error al ejecutar rollback: " + ex.getMessage());
                }
            }
            System.err.println("Error en EmpleadoDAOImpl.registrarEmpleado: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) {
                    System.err.println("Error al cerrar conexión: " + ex.getMessage());
                }
            }
        }
    }

    // FIX: nuevo. Misma estructura transaccional que registrarEmpleado, pero con UPDATE.
    // El correo NO se toca aquí a propósito (se deja de solo lectura en el formulario).
    @Override
    public boolean actualizarEmpleado(Usuario usuario, Empleado empleado) {
        String sqlUsuario = "UPDATE usuario SET nombre = ?, apellido = ?, telefono = ? WHERE id = ?";
        String sqlEmpleado = "UPDATE empleado SET dni = ?, cargo = ?, area = ? WHERE id_usuario = ?";

        Connection conn = null;
        try {
            conn = ConexionDB.obtenerConexion();
            conn.setAutoCommit(false);

            try (PreparedStatement psUser = conn.prepareStatement(sqlUsuario)) {
                psUser.setString(1, usuario.getNombre());
                psUser.setString(2, usuario.getApellido());
                psUser.setString(3, usuario.getTelefono());
                psUser.setInt(4, usuario.getId());
                psUser.executeUpdate();
            }

            try (PreparedStatement psEmp = conn.prepareStatement(sqlEmpleado)) {
                psEmp.setString(1, empleado.getDni());
                psEmp.setString(2, empleado.getCargo());
                psEmp.setString(3, empleado.getArea());
                psEmp.setInt(4, usuario.getId());
                psEmp.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) {
                    System.err.println("Error al ejecutar rollback: " + ex.getMessage());
                }
            }
            System.err.println("Error en EmpleadoDAOImpl.actualizarEmpleado: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) {
                    System.err.println("Error al cerrar conexión: " + ex.getMessage());
                }
            }
        }
    }

    // FIX: nuevo. Baja lógica / reactivación: solo toca usuario.estado.
    @Override
    public boolean cambiarEstado(int idUsuario, String nuevoEstado) {
        String sql = "UPDATE usuario SET estado = ? WHERE id = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error en EmpleadoDAOImpl.cambiarEstado: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Empleado buscarPorIdUsuario(int idUsuario) {
        String sql = SELECT_BASE + "WHERE e.id_usuario = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearEmpleadoCompleto(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en EmpleadoDAOImpl.buscarPorIdUsuario: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Empleado buscarPorId(int idEmpleado) {
        String sql = SELECT_BASE + "WHERE e.id_empleado = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearEmpleadoCompleto(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en EmpleadoDAOImpl.buscarPorId: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Empleado> listarTodos() {
        List<Empleado> lista = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY e.id_empleado ASC";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearEmpleadoCompleto(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en EmpleadoDAOImpl.listarTodos: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public List<Empleado> listarPersonalOperativo() {
        List<Empleado> lista = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE UPPER(u.rol) != 'CLIENTE' ORDER BY e.id_empleado ASC";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearEmpleadoCompleto(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en EmpleadoDAOImpl.listarPersonalOperativo: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    private Empleado mapearEmpleadoCompleto(ResultSet rs) throws SQLException {
        Empleado emp = new Empleado(
                rs.getInt("id_empleado"),
                rs.getInt("id_usuario"),
                rs.getString("dni"),
                rs.getString("cargo"),
                rs.getString("area")
        );
        emp.setNombre(rs.getString("nombre"));
        emp.setApellido(rs.getString("apellido"));
        emp.setCorreo(rs.getString("correo"));
        emp.setRol(rs.getString("rol"));
        emp.setTelefono(rs.getString("telefono"));
        emp.setEstado(rs.getString("estado"));
        return emp;
    }
}
