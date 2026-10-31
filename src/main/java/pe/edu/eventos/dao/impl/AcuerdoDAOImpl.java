package pe.edu.eventos.dao.impl;

import pe.edu.eventos.dao.AcuerdoDAO;
import pe.edu.eventos.model.Acuerdo;
import pe.edu.eventos.util.BusquedaUtil;
import pe.edu.eventos.util.ConexionDB;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AcuerdoDAOImpl implements AcuerdoDAO {

    private static final String SELECT_BASE =
            "SELECT a.id_acuerdo, a.id_evento, a.condiciones, a.estado, a.fecha_registro, a.fecha_confirmacion, " +
                    "e.nombre AS evento_nombre, e.id_cliente, e.fecha_evento, e.estado AS evento_estado, " +
                    "CONCAT(u.nombre, ' ', u.apellido) AS cliente_nombre " +
                    "FROM acuerdo a " +
                    "JOIN evento e ON e.id_evento = a.id_evento " +
                    "JOIN cliente cl ON cl.id_cliente = e.id_cliente " +
                    "JOIN usuario u ON u.id = cl.id_usuario ";

    private static final String ORDEN = "ORDER BY a.fecha_registro DESC, a.id_acuerdo DESC";

    @Override
    public Integer registrar(Acuerdo acuerdo) {
        Connection conn = null;
        try {
            conn = ConexionDB.obtenerConexion();
            conn.setAutoCommit(false);

            // Bloquea el evento: dos personas creando el acuerdo a la vez se ordenan una detrás de otra
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT estado FROM evento WHERE id_evento = ? FOR UPDATE")) {
                ps.setInt(1, acuerdo.getIdEvento());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return null;
                    }
                }
            }

            if (!existe(conn, "SELECT 1 FROM cotizacion WHERE id_evento = ? AND estado = 'ACEPTADA'", acuerdo.getIdEvento())
                    || existe(conn, "SELECT 1 FROM acuerdo WHERE id_evento = ? AND estado IN ('PENDIENTE', 'ACEPTADO')",
                    acuerdo.getIdEvento())) {
                conn.rollback();
                return null;
            }

            int idNuevo;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO acuerdo (id_evento, condiciones, estado) VALUES (?, ?, 'PENDIENTE') RETURNING id_acuerdo")) {
                ps.setInt(1, acuerdo.getIdEvento());
                ps.setString(2, acuerdo.getCondiciones());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    idNuevo = rs.getInt(1);
                }
            }
            conn.commit();
            return idNuevo;
        } catch (SQLException e) {
            System.err.println("Error en AcuerdoDAOImpl.registrar: " + e.getMessage());
            e.printStackTrace();
            rollback(conn);
            return null;
        } finally {
            cerrar(conn);
        }
    }

    private boolean existe(Connection conn, String sql, int idEvento) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEvento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public Acuerdo buscarPorId(int idAcuerdo) {
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE a.id_acuerdo = ?")) {
            ps.setInt(1, idAcuerdo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            System.err.println("Error en AcuerdoDAOImpl.buscarPorId: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Acuerdo> listarPorEvento(int idEvento) {
        return listar("WHERE a.id_evento = ? ", idEvento);
    }

    @Override
    public List<Acuerdo> listarPorCliente(int idCliente) {
        return listar("WHERE e.id_cliente = ? ", idCliente);
    }

    private List<Acuerdo> listar(String where, int parametro) {
        List<Acuerdo> lista = new ArrayList<>();
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + where + ORDEN)) {
            ps.setInt(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en AcuerdoDAOImpl.listar: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public List<Acuerdo> buscar(String texto, String filtro) {
        List<Acuerdo> lista = new ArrayList<>();
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
            System.err.println("Error en AcuerdoDAOImpl.buscar: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    /** Arma el SQL del listado y llena params en el orden de los "?" (separado para poder probarlo). */
    static String construirConsultaBusqueda(String texto, String filtro, List<String> params) {
        StringBuilder sql = new StringBuilder(SELECT_BASE + "WHERE 1=1 ");

        String f = filtro == null ? "" : filtro.trim().toUpperCase();
        switch (f) {
            case Acuerdo.PENDIENTE:
            case Acuerdo.ACEPTADO:
            case Acuerdo.CANCELADO:
                sql.append("AND a.estado = '").append(f).append("' ");
                break;
            default:
                break; // todos
        }

        for (String palabra : BusquedaUtil.tokenizar(texto)) {
            sql.append("AND (").append(BusquedaUtil.sinTildesSql("e.nombre")).append(" ILIKE ? OR ")
                    .append(BusquedaUtil.sinTildesSql("CONCAT(u.nombre, ' ', u.apellido)")).append(" ILIKE ?) ");
            String patron = BusquedaUtil.patronContieneSinTildes(palabra);
            params.add(patron);
            params.add(patron);
        }

        sql.append(ORDEN);
        return sql.toString();
    }

    @Override
    public boolean actualizarCondiciones(int idAcuerdo, String condiciones) {
        String sql = "UPDATE acuerdo SET condiciones = ? WHERE id_acuerdo = ? AND estado = 'PENDIENTE'";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, condiciones);
            ps.setInt(2, idAcuerdo);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("Error en AcuerdoDAOImpl.actualizarCondiciones: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean aceptar(int idAcuerdo) {
        Connection conn = null;
        try {
            conn = ConexionDB.obtenerConexion();
            conn.setAutoCommit(false);

            Integer idEvento = bloquearAcuerdo(conn, idAcuerdo, Acuerdo.PENDIENTE);
            if (idEvento == null) {
                conn.rollback();
                return false;
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE acuerdo SET estado = 'ACEPTADO', fecha_confirmacion = CURRENT_TIMESTAMP WHERE id_acuerdo = ?")) {
                ps.setInt(1, idAcuerdo);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE evento SET estado = 'CONFIRMADO' WHERE id_evento = ? AND estado = 'COTIZADO'")) {
                ps.setInt(1, idEvento);
                ps.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error en AcuerdoDAOImpl.aceptar: " + e.getMessage());
            e.printStackTrace();
            rollback(conn);
            return false;
        } finally {
            cerrar(conn);
        }
    }

    @Override
    public boolean cancelar(int idAcuerdo) {
        Connection conn = null;
        try {
            conn = ConexionDB.obtenerConexion();
            conn.setAutoCommit(false);

            String estadoAnterior = null;
            Integer idEvento = null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id_evento, estado FROM acuerdo WHERE id_acuerdo = ? FOR UPDATE")) {
                ps.setInt(1, idAcuerdo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        idEvento = rs.getInt("id_evento");
                        estadoAnterior = rs.getString("estado");
                    }
                }
            }
            if (idEvento == null || (!Acuerdo.PENDIENTE.equals(estadoAnterior) && !Acuerdo.ACEPTADO.equals(estadoAnterior))) {
                conn.rollback();
                return false;
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE acuerdo SET estado = 'CANCELADO' WHERE id_acuerdo = ?")) {
                ps.setInt(1, idAcuerdo);
                ps.executeUpdate();
            }
            if (Acuerdo.ACEPTADO.equals(estadoAnterior)) {
                // El evento deja de estar confirmado hasta que haya un acuerdo nuevo
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE evento SET estado = 'COTIZADO' WHERE id_evento = ? AND estado = 'CONFIRMADO'")) {
                    ps.setInt(1, idEvento);
                    ps.executeUpdate();
                }
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error en AcuerdoDAOImpl.cancelar: " + e.getMessage());
            e.printStackTrace();
            rollback(conn);
            return false;
        } finally {
            cerrar(conn);
        }
    }

    /** Bloquea el acuerdo y devuelve su evento solo si está en el estado esperado; si no, null. */
    private Integer bloquearAcuerdo(Connection conn, int idAcuerdo, String estadoEsperado) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id_evento FROM acuerdo WHERE id_acuerdo = ? AND estado = ? FOR UPDATE")) {
            ps.setInt(1, idAcuerdo);
            ps.setString(2, estadoEsperado);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    @Override
    public int contarPorEstado(String estado) {
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM acuerdo WHERE estado = ?")) {
            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            System.err.println("Error en AcuerdoDAOImpl.contarPorEstado: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int contarPendientesPorCliente(int idCliente) {
        String sql = "SELECT COUNT(*) FROM acuerdo a JOIN evento e ON e.id_evento = a.id_evento " +
                "WHERE e.id_cliente = ? AND a.estado = 'PENDIENTE'";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            System.err.println("Error en AcuerdoDAOImpl.contarPendientesPorCliente: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    private void rollback(Connection conn) {
        if (conn == null) return;
        try {
            conn.rollback();
        } catch (SQLException ex) {
            System.err.println("No se pudo hacer rollback: " + ex.getMessage());
        }
    }

    private void cerrar(Connection conn) {
        if (conn == null) return;
        try {
            conn.setAutoCommit(true);
            conn.close();
        } catch (SQLException ignored) {
            // la conexión ya no se usa
        }
    }

    private Acuerdo mapear(ResultSet rs) throws SQLException {
        Acuerdo a = new Acuerdo();
        a.setIdAcuerdo(rs.getInt("id_acuerdo"));
        a.setIdEvento(rs.getInt("id_evento"));
        a.setCondiciones(rs.getString("condiciones"));
        a.setEstado(rs.getString("estado"));
        Timestamp reg = rs.getTimestamp("fecha_registro");
        if (reg != null) a.setFechaRegistro(reg.toLocalDateTime());
        Timestamp conf = rs.getTimestamp("fecha_confirmacion");
        if (conf != null) a.setFechaConfirmacion(conf.toLocalDateTime());
        a.setIdCliente(rs.getInt("id_cliente"));
        a.setNombreEvento(rs.getString("evento_nombre"));
        a.setNombreCliente(rs.getString("cliente_nombre"));
        Date fe = rs.getDate("fecha_evento");
        if (fe != null) a.setFechaEvento(fe.toLocalDate());
        a.setEstadoEvento(rs.getString("evento_estado"));
        return a;
    }
}
