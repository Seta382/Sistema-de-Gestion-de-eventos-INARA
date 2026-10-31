package pe.edu.eventos.dao.impl;

import pe.edu.eventos.dao.SeguimientoDAO;
import pe.edu.eventos.model.Seguimiento;
import pe.edu.eventos.util.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class SeguimientoDAOImpl implements SeguimientoDAO {

    private static final ZoneId ZONA = ZoneId.of("America/Lima");
    private static final String COLUMNAS = "id_seguimiento, id_evento, titulo, descripcion, fecha_registro";

    @Override
    public Integer registrar(Seguimiento s) {
        String sql = "INSERT INTO seguimiento (id_evento, titulo, descripcion) VALUES (?, ?, ?) RETURNING id_seguimiento";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, s.getIdEvento());
            ps.setString(2, s.getTitulo());
            if (s.getDescripcion() != null && !s.getDescripcion().isEmpty()) {
                ps.setString(3, s.getDescripcion());
            } else {
                ps.setNull(3, Types.VARCHAR);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        } catch (SQLException e) {
            System.err.println("Error en SeguimientoDAOImpl.registrar: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Seguimiento buscarPorId(int idSeguimiento) {
        String sql = "SELECT " + COLUMNAS + " FROM seguimiento WHERE id_seguimiento = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSeguimiento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            System.err.println("Error en SeguimientoDAOImpl.buscarPorId: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Seguimiento> listarPorEvento(int idEvento) {
        List<Seguimiento> lista = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM seguimiento WHERE id_evento = ? " +
                "ORDER BY fecha_registro DESC, id_seguimiento DESC";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEvento);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en SeguimientoDAOImpl.listarPorEvento: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public boolean actualizar(int idSeguimiento, String titulo, String descripcion) {
        String sql = "UPDATE seguimiento SET titulo = ?, descripcion = ? WHERE id_seguimiento = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, titulo);
            if (descripcion != null && !descripcion.isEmpty()) {
                ps.setString(2, descripcion);
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setInt(3, idSeguimiento);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("Error en SeguimientoDAOImpl.actualizar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean eliminar(int idSeguimiento) {
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM seguimiento WHERE id_seguimiento = ?")) {
            ps.setInt(1, idSeguimiento);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("Error en SeguimientoDAOImpl.eliminar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private Seguimiento mapear(ResultSet rs) throws SQLException {
        Seguimiento s = new Seguimiento();
        s.setIdSeguimiento(rs.getInt("id_seguimiento"));
        s.setIdEvento(rs.getInt("id_evento"));
        s.setTitulo(rs.getString("titulo"));
        s.setDescripcion(rs.getString("descripcion"));
        Timestamp ts = rs.getTimestamp("fecha_registro");
        if (ts != null) s.setFechaRegistro(ts.toInstant().atZone(ZONA).toLocalDateTime());
        return s;
    }
}
