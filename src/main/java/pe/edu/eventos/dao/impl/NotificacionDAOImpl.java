package pe.edu.eventos.dao.impl;

import pe.edu.eventos.dao.NotificacionDAO;
import pe.edu.eventos.model.Notificacion;
import pe.edu.eventos.util.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class NotificacionDAOImpl implements NotificacionDAO {

    private static final ZoneId ZONA = ZoneId.of("America/Lima");

    // Texto del aviso con {evento} y {cliente} ya reemplazados (los parámetros son: tipo, plantilla, idEvento)
    private static final String MENSAJE_SQL =
            "REPLACE(REPLACE(?, '{evento}', COALESCE(NULLIF(e.nombre, ''), 'Evento #' || e.id_evento)), " +
                    "'{cliente}', CONCAT(cu.nombre, ' ', cu.apellido))";

    private static final String DESDE_EVENTO =
            "FROM evento e " +
                    "JOIN cliente cl ON cl.id_cliente = e.id_cliente " +
                    "JOIN usuario cu ON cu.id = cl.id_usuario ";

    static final String SQL_AVISAR_CLIENTE =
            "INSERT INTO notificacion (id_usuario, id_evento, tipo, mensaje) " +
                    "SELECT cl.id_usuario, e.id_evento, ?, " + MENSAJE_SQL + " " +
                    DESDE_EVENTO +
                    "WHERE e.id_evento = ?";

    static final String SQL_AVISAR_PERSONAL =
            "INSERT INTO notificacion (id_usuario, id_evento, tipo, mensaje) " +
                    "SELECT DISTINCT u.id, e.id_evento, ?, " + MENSAJE_SQL + " " +
                    DESDE_EVENTO +
                    "JOIN usuario u ON (u.rol = 'ADMIN' OR u.id IN " +
                    "(SELECT emp.id_usuario FROM empleado emp WHERE emp.id_empleado = e.id_empleado)) " +
                    "AND COALESCE(u.estado, 'ACTIVO') <> 'INACTIVO' " +
                    "WHERE e.id_evento = ?";

    private static final String COLUMNAS =
            "n.id_notificacion, n.id_usuario, n.id_evento, n.tipo, n.mensaje, n.leida, n.fecha";

    @Override
    public boolean avisarCliente(int idEvento, String tipo, String plantilla) {
        return insertar(SQL_AVISAR_CLIENTE, idEvento, tipo, plantilla, "avisarCliente") > 0;
    }

    @Override
    public int avisarPersonal(int idEvento, String tipo, String plantilla) {
        return insertar(SQL_AVISAR_PERSONAL, idEvento, tipo, plantilla, "avisarPersonal");
    }

    private int insertar(String sql, int idEvento, String tipo, String plantilla, String operacion) {
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tipo);
            ps.setString(2, plantilla);
            ps.setInt(3, idEvento);
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error en NotificacionDAOImpl." + operacion + ": " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public List<Notificacion> listarPorUsuario(int idUsuario, boolean soloNoLeidas, int limite) {
        List<Notificacion> lista = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM notificacion n WHERE n.id_usuario = ? "
                + (soloNoLeidas ? "AND COALESCE(n.leida, FALSE) = FALSE " : "")
                + "ORDER BY n.fecha DESC, n.id_notificacion DESC LIMIT ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, Math.max(1, Math.min(limite, 200)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en NotificacionDAOImpl.listarPorUsuario: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public Notificacion buscarPorId(int idNotificacion, int idUsuario) {
        String sql = "SELECT " + COLUMNAS + " FROM notificacion n WHERE n.id_notificacion = ? AND n.id_usuario = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idNotificacion);
            ps.setInt(2, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            System.err.println("Error en NotificacionDAOImpl.buscarPorId: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int contarNoLeidas(int idUsuario) {
        String sql = "SELECT COUNT(*) FROM notificacion WHERE id_usuario = ? AND COALESCE(leida, FALSE) = FALSE";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            System.err.println("Error en NotificacionDAOImpl.contarNoLeidas: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public boolean marcarLeida(int idNotificacion, int idUsuario) {
        String sql = "UPDATE notificacion SET leida = TRUE WHERE id_notificacion = ? AND id_usuario = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idNotificacion);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("Error en NotificacionDAOImpl.marcarLeida: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int marcarTodasLeidas(int idUsuario) {
        String sql = "UPDATE notificacion SET leida = TRUE WHERE id_usuario = ? AND COALESCE(leida, FALSE) = FALSE";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error en NotificacionDAOImpl.marcarTodasLeidas: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    private Notificacion mapear(ResultSet rs) throws SQLException {
        Notificacion n = new Notificacion();
        n.setIdNotificacion(rs.getInt("id_notificacion"));
        n.setIdUsuario(rs.getInt("id_usuario"));
        int idEvento = rs.getInt("id_evento");
        n.setIdEvento(rs.wasNull() ? null : idEvento);
        n.setTipo(rs.getString("tipo"));
        n.setMensaje(rs.getString("mensaje"));
        n.setLeida(rs.getBoolean("leida"));
        Timestamp ts = rs.getTimestamp("fecha");
        if (ts != null) n.setFecha(ts.toInstant().atZone(ZONA).toLocalDateTime());
        return n;
    }
}
