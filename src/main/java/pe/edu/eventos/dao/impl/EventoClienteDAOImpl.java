package pe.edu.eventos.dao.impl;

import pe.edu.eventos.dao.EventoClienteDAO;
import pe.edu.eventos.model.Evento;
import pe.edu.eventos.util.ConexionDB;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class EventoClienteDAOImpl implements EventoClienteDAO {

    @Override
    public List<Evento> listarPorCliente(int idCliente) {
        List<Evento> lista = new ArrayList<>();
        String sql = "SELECT e.id_evento, e.nombre, e.id_cliente, e.fecha_evento, e.hora_evento, e.lugar, " +
                "e.num_invitados, e.presupuesto, e.descripcion, e.estado, " +
                "te.nombre AS tipo_celebracion, " +
                "CONCAT(ue.nombre, ' ', ue.apellido) AS coordinador_nombre " +
                "FROM evento e " +
                "LEFT JOIN tipo_evento te ON te.id_tipo_evento = e.id_tipo_evento " +
                "LEFT JOIN empleado emp ON emp.id_empleado = e.id_empleado " +
                "LEFT JOIN usuario ue ON ue.id = emp.id_usuario " +
                "WHERE e.id_cliente = ? " +
                "ORDER BY CASE WHEN e.estado IN ('FINALIZADO', 'CANCELADO') THEN 1 ELSE 0 END, " +
                "e.fecha_evento ASC, e.id_evento DESC";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Evento ev = new Evento();
                    ev.setIdEvento(rs.getInt("id_evento"));
                    ev.setNombre(rs.getString("nombre"));
                    ev.setIdCliente(rs.getInt("id_cliente"));
                    Date d = rs.getDate("fecha_evento");
                    if (d != null) ev.setFechaEvento(d.toLocalDate());
                    Time t = rs.getTime("hora_evento");
                    if (t != null) ev.setHoraEvento(t.toLocalTime());
                    ev.setLugar(rs.getString("lugar"));
                    ev.setNumInvitados(rs.getInt("num_invitados"));
                    ev.setPresupuesto(rs.getBigDecimal("presupuesto"));
                    ev.setDescripcion(rs.getString("descripcion"));
                    ev.setEstado(rs.getString("estado"));
                    ev.setTipoCelebracion(rs.getString("tipo_celebracion"));
                    ev.setNombreCoordinador(rs.getString("coordinador_nombre"));
                    lista.add(ev);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en EventoClienteDAOImpl.listarPorCliente: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }
}
