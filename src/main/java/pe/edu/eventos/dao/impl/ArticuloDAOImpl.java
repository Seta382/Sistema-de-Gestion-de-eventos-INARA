package pe.edu.eventos.dao.impl;

import pe.edu.eventos.dao.ArticuloDAO;
import pe.edu.eventos.model.Articulo;
import pe.edu.eventos.util.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ArticuloDAOImpl implements ArticuloDAO {

    @Override
    public boolean registrar(Articulo articulo) {
        String sql = "INSERT INTO articulo (nombre, descripcion, precio, stock, id_proveedor, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, articulo.getNombre() != null ? articulo.getNombre().trim() : "");
            if (articulo.getDescripcion() != null && !articulo.getDescripcion().trim().isEmpty()) {
                ps.setString(2, articulo.getDescripcion().trim());
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setBigDecimal(3, articulo.getPrecio());
            ps.setInt(4, articulo.getStock() != null ? articulo.getStock() : 0);
            if (articulo.getIdProveedor() != null && articulo.getIdProveedor() > 0) {
                ps.setInt(5, articulo.getIdProveedor());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setString(6, articulo.getEstado() != null ? articulo.getEstado() : "ACTIVO");

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error en ArticuloDAOImpl.registrar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Articulo buscarPorId(int idArticulo) {
        String sql = "SELECT a.id_articulo, a.nombre, a.descripcion, a.precio, a.stock, a.id_proveedor, a.estado, a.creado_en, " +
                "p.razon_social AS proveedor_nombre " +
                "FROM articulo a " +
                "LEFT JOIN proveedor p ON a.id_proveedor = p.id_proveedor " +
                "WHERE a.id_articulo = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idArticulo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Articulo art = mapearArticulo(rs);
                    art.setNombreProveedor(rs.getString("proveedor_nombre"));
                    return art;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en ArticuloDAOImpl.buscarPorId: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Articulo> listarTodos() {
        List<Articulo> lista = new ArrayList<>();
        String sql = "SELECT a.id_articulo, a.nombre, a.descripcion, a.precio, a.stock, a.id_proveedor, a.estado, a.creado_en, " +
                "p.razon_social AS proveedor_nombre " +
                "FROM articulo a " +
                "LEFT JOIN proveedor p ON a.id_proveedor = p.id_proveedor " +
                "ORDER BY a.id_articulo ASC";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Articulo art = mapearArticulo(rs);
                art.setNombreProveedor(rs.getString("proveedor_nombre"));
                lista.add(art);
            }
        } catch (SQLException e) {
            System.err.println("Error en ArticuloDAOImpl.listarTodos: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public List<Articulo> listarActivosConProveedor() {
        List<Articulo> lista = new ArrayList<>();
        String sql = "SELECT a.id_articulo, a.nombre, a.descripcion, a.precio, a.stock, a.id_proveedor, a.estado, a.creado_en, " +
                "COALESCE(p.razon_social, 'Sin Proveedor') AS proveedor_nombre " +
                "FROM articulo a " +
                "LEFT JOIN proveedor p ON a.id_proveedor = p.id_proveedor " +
                "WHERE a.estado = 'ACTIVO' AND a.stock > 0 " +
                "ORDER BY a.nombre ASC";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Articulo art = mapearArticulo(rs);
                art.setNombreProveedor(rs.getString("proveedor_nombre"));
                lista.add(art);
            }
        } catch (SQLException e) {
            System.err.println("Error en ArticuloDAOImpl.listarActivosConProveedor: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public List<Articulo> listarPorProveedor(int idProveedor) {
        List<Articulo> lista = new ArrayList<>();
        String sql = "SELECT a.id_articulo, a.nombre, a.descripcion, a.precio, a.stock, a.id_proveedor, a.estado, a.creado_en, " +
                "p.razon_social AS proveedor_nombre " +
                "FROM articulo a " +
                "LEFT JOIN proveedor p ON a.id_proveedor = p.id_proveedor " +
                "WHERE a.id_proveedor = ? ORDER BY a.id_articulo ASC";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idProveedor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Articulo art = mapearArticulo(rs);
                    art.setNombreProveedor(rs.getString("proveedor_nombre"));
                    lista.add(art);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en ArticuloDAOImpl.listarPorProveedor: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    private Articulo mapearArticulo(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("creado_en");
        return new Articulo(
                rs.getInt("id_articulo"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getBigDecimal("precio"),
                rs.getInt("stock"),
                rs.getInt("id_proveedor"),
                rs.getString("estado"),
                ts != null ? ts.toLocalDateTime() : null
        );
    }
}