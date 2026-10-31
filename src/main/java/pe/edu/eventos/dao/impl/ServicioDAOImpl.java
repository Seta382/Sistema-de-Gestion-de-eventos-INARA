package pe.edu.eventos.dao.impl;

import pe.edu.eventos.dao.ServicioDAO;
import pe.edu.eventos.model.Servicio;
import pe.edu.eventos.util.BusquedaUtil;
import pe.edu.eventos.util.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ServicioDAOImpl implements ServicioDAO {

    private static final String COLUMNAS = "s.id_servicio, s.nombre, s.descripcion, s.precio, s.estado";

    @Override
    public boolean registrar(Servicio servicio) {
        String sql = "INSERT INTO servicio (nombre, descripcion, precio, estado) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, servicio.getNombre() != null ? servicio.getNombre().trim() : "");
            if (servicio.getDescripcion() != null && !servicio.getDescripcion().trim().isEmpty()) {
                ps.setString(2, servicio.getDescripcion().trim());
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setBigDecimal(3, servicio.getPrecio());
            ps.setString(4, servicio.getEstado() != null ? servicio.getEstado() : "ACTIVO");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error en ServicioDAOImpl.registrar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Servicio buscarPorId(int idServicio) {
        String sql = "SELECT " + COLUMNAS + " FROM servicio s WHERE s.id_servicio = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idServicio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error en ServicioDAOImpl.buscarPorId: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Servicio buscarPorNombre(String nombre) {
        String sql = "SELECT " + COLUMNAS + " FROM servicio s WHERE LOWER(TRIM(s.nombre)) = LOWER(TRIM(?)) "
                + "ORDER BY s.id_servicio LIMIT 1";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error en ServicioDAOImpl.buscarPorNombre: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Servicio> listarActivos() {
        return buscar("", "ACTIVO");
    }

    @Override
    public List<Servicio> buscar(String texto, String filtro) {
        List<Servicio> lista = new ArrayList<>();
        List<String> params = new ArrayList<>();
        String sql = construirConsultaBusqueda(texto, filtro, params);
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en ServicioDAOImpl.buscar: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    /** Arma el SQL de búsqueda y llena params en el orden de los "?" (separado para poder probarlo). */
    static String construirConsultaBusqueda(String texto, String filtro, List<String> params) {
        StringBuilder sql = new StringBuilder("SELECT " + COLUMNAS + " FROM servicio s WHERE 1=1 ");

        String f = filtro == null ? "" : filtro.trim().toUpperCase();
        switch (f) {
            case "ACTIVO":
                sql.append("AND s.estado = 'ACTIVO' ");
                break;
            case "INACTIVO":
                sql.append("AND s.estado = 'INACTIVO' ");
                break;
            default:
                break; // todos
        }

        // Cada palabra debe coincidir en nombre o descripción, sin importar tildes (AND entre palabras)
        for (String palabra : BusquedaUtil.tokenizar(texto)) {
            sql.append("AND (").append(BusquedaUtil.sinTildesSql("s.nombre")).append(" ILIKE ? OR ")
                    .append(BusquedaUtil.sinTildesSql("COALESCE(s.descripcion, '')")).append(" ILIKE ?) ");
            String patron = BusquedaUtil.patronContieneSinTildes(palabra);
            params.add(patron);
            params.add(patron);
        }

        sql.append("ORDER BY s.nombre ASC");
        return sql.toString();
    }

    @Override
    public boolean cambiarEstado(int idServicio, String nuevoEstado) {
        String sql = "UPDATE servicio SET estado = ? WHERE id_servicio = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idServicio);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error en ServicioDAOImpl.cambiarEstado: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean actualizar(Servicio servicio) {
        String sql = "UPDATE servicio SET nombre = ?, descripcion = ?, precio = ? WHERE id_servicio = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, servicio.getNombre() != null ? servicio.getNombre().trim() : "");
            if (servicio.getDescripcion() != null && !servicio.getDescripcion().trim().isEmpty()) {
                ps.setString(2, servicio.getDescripcion().trim());
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setBigDecimal(3, servicio.getPrecio());
            ps.setInt(4, servicio.getIdServicio());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error en ServicioDAOImpl.actualizar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private Servicio mapear(ResultSet rs) throws SQLException {
        return new Servicio(
                rs.getInt("id_servicio"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getBigDecimal("precio"),
                rs.getString("estado"));
    }
}
