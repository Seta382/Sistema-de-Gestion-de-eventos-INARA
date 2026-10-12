package pe.edu.eventos.dao.impl;

import pe.edu.eventos.dao.ArticuloDAO;
import pe.edu.eventos.model.Articulo;
import pe.edu.eventos.util.BusquedaUtil;
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

    @Override
    public List<Articulo> buscar(String texto, String filtro) {
        List<Articulo> lista = new ArrayList<>();
        List<String> params = new ArrayList<>();
        String sql = construirConsultaBusqueda(texto, filtro, params);
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Articulo art = mapearArticulo(rs);
                    art.setNombreProveedor(rs.getString("proveedor_nombre"));
                    lista.add(art);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en ArticuloDAOImpl.buscar: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    /** Arma el SQL de búsqueda y llena params en el orden de los "?" (separado para poder probarlo). */
    static String construirConsultaBusqueda(String texto, String filtro, List<String> params) {
        StringBuilder sql = new StringBuilder(
                "SELECT a.id_articulo, a.nombre, a.descripcion, a.precio, a.stock, a.id_proveedor, a.estado, a.creado_en, " +
                        "COALESCE(p.razon_social, 'Sin proveedor') AS proveedor_nombre " +
                        "FROM articulo a " +
                        "LEFT JOIN proveedor p ON a.id_proveedor = p.id_proveedor " +
                        "WHERE 1=1 ");

        String f = filtro == null ? "" : filtro.trim().toUpperCase();
        switch (f) {
            case "ACTIVO":
                sql.append("AND a.estado = 'ACTIVO' ");
                break;
            case "INACTIVO":
                sql.append("AND a.estado = 'INACTIVO' ");
                break;
            case "STOCK_BAJO":
                sql.append("AND a.estado = 'ACTIVO' AND a.stock <= ").append(UMBRAL_STOCK_BAJO).append(" ");
                break;
            default:
                break; // todos
        }

        // Cada palabra debe coincidir en al menos uno de estos campos (AND entre palabras)
        for (String palabra : BusquedaUtil.tokenizar(texto)) {
            sql.append("AND (a.nombre ILIKE ? OR a.descripcion ILIKE ? OR p.razon_social ILIKE ? OR p.ruc ILIKE ?) ");
            String patron = BusquedaUtil.patronContiene(palabra);
            for (int i = 0; i < 4; i++) {
                params.add(patron);
            }
        }

        sql.append("ORDER BY a.nombre ASC");
        return sql.toString();
    }

    @Override
    public boolean cambiarEstado(int idArticulo, String nuevoEstado) {
        String sql = "UPDATE articulo SET estado = ? WHERE id_articulo = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idArticulo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error en ArticuloDAOImpl.cambiarEstado: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean actualizar(Articulo articulo, Integer stockEsperado) {
        boolean cambiaStock = stockEsperado != null;
        String sql = construirSqlActualizacion(cambiaStock);
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int i = 1;
            ps.setString(i++, articulo.getNombre() != null ? articulo.getNombre().trim() : "");
            if (articulo.getDescripcion() != null && !articulo.getDescripcion().trim().isEmpty()) {
                ps.setString(i++, articulo.getDescripcion().trim());
            } else {
                ps.setNull(i++, Types.VARCHAR);
            }
            ps.setBigDecimal(i++, articulo.getPrecio());
            if (articulo.getIdProveedor() != null && articulo.getIdProveedor() > 0) {
                ps.setInt(i++, articulo.getIdProveedor());
            } else {
                ps.setNull(i++, Types.INTEGER);
            }
            if (cambiaStock) {
                ps.setInt(i++, articulo.getStock());
            }
            ps.setInt(i++, articulo.getIdArticulo());
            if (cambiaStock) {
                ps.setInt(i++, stockEsperado);
            }

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error en ArticuloDAOImpl.actualizar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /** SQL del UPDATE (separado para poder probarlo). Con cambiaStock agrega la condición de stock esperado. */
    static String construirSqlActualizacion(boolean cambiaStock) {
        return "UPDATE articulo SET nombre = ?, descripcion = ?, precio = ?, id_proveedor = ?"
                + (cambiaStock ? ", stock = ?" : "")
                + " WHERE id_articulo = ?"
                + (cambiaStock ? " AND stock = ?" : "");
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
