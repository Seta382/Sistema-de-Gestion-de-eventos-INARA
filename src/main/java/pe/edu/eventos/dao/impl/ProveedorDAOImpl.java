package pe.edu.eventos.dao.impl;

import pe.edu.eventos.dao.ProveedorDAO;
import pe.edu.eventos.model.Proveedor;
import pe.edu.eventos.util.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProveedorDAOImpl implements ProveedorDAO {

    @Override
    public boolean registrar(Proveedor proveedor) {
        String sql = "INSERT INTO proveedor (razon_social, ruc, correo, telefono, direccion, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, proveedor.getRazonSocial() != null ? proveedor.getRazonSocial().trim() : "");
            ps.setString(2, proveedor.getRuc() != null ? proveedor.getRuc().trim() : "");
            if (proveedor.getCorreo() != null && !proveedor.getCorreo().trim().isEmpty()) {
                ps.setString(3, proveedor.getCorreo().trim());
            } else {
                ps.setNull(3, Types.VARCHAR);
            }
            if (proveedor.getTelefono() != null && !proveedor.getTelefono().trim().isEmpty()) {
                ps.setString(4, proveedor.getTelefono().trim());
            } else {
                ps.setNull(4, Types.VARCHAR);
            }
            if (proveedor.getDireccion() != null && !proveedor.getDireccion().trim().isEmpty()) {
                ps.setString(5, proveedor.getDireccion().trim());
            } else {
                ps.setNull(5, Types.VARCHAR);
            }
            ps.setString(6, proveedor.getEstado() != null ? proveedor.getEstado() : "ACTIVO");

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error en ProveedorDAOImpl.registrar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Proveedor buscarPorRuc(String ruc) {
        String sql = "SELECT id_proveedor, razon_social, ruc, correo, telefono, direccion, estado " +
                "FROM proveedor WHERE ruc = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ruc != null ? ruc.trim() : "");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearProveedor(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en ProveedorDAOImpl.buscarPorRuc: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Proveedor buscarPorId(int idProveedor) {
        String sql = "SELECT id_proveedor, razon_social, ruc, correo, telefono, direccion, estado " +
                "FROM proveedor WHERE id_proveedor = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idProveedor);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearProveedor(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en ProveedorDAOImpl.buscarPorId: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Proveedor> listarTodos() {
        List<Proveedor> lista = new ArrayList<>();
        String sql = "SELECT id_proveedor, razon_social, ruc, correo, telefono, direccion, estado " +
                "FROM proveedor ORDER BY razon_social ASC";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearProveedor(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en ProveedorDAOImpl.listarTodos: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public List<Proveedor> listarPorIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<Proveedor> lista = new ArrayList<>();
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) placeholders.append(",");
            placeholders.append("?");
        }
        String sql = "SELECT id_proveedor, razon_social, ruc, correo, telefono, direccion, estado " +
                "FROM proveedor WHERE id_proveedor IN (" + placeholders + ") ORDER BY razon_social ASC";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < ids.size(); i++) {
                ps.setInt(i + 1, ids.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearProveedor(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en ProveedorDAOImpl.listarPorIds: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    private Proveedor mapearProveedor(ResultSet rs) throws SQLException {
        return new Proveedor(
                rs.getInt("id_proveedor"),
                rs.getString("razon_social"),
                rs.getString("ruc"),
                rs.getString("correo"),
                rs.getString("telefono"),
                rs.getString("direccion"),
                rs.getString("estado")
        );
    }
}
