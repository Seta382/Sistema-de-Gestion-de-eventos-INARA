package pe.edu.eventos.dao.impl;

import pe.edu.eventos.dao.PagoDAO;
import pe.edu.eventos.model.Pago;
import pe.edu.eventos.util.BusquedaUtil;
import pe.edu.eventos.util.ConexionDB;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class PagoDAOImpl implements PagoDAO {

    private static final ZoneId ZONA = ZoneId.of("America/Lima");

    private static final String SELECT_BASE =
            "SELECT p.id_pago, p.id_evento, p.monto, p.fecha_pago, p.metodo_pago, p.estado, p.comprobante_url, " +
                    "e.nombre AS evento_nombre, e.id_cliente, e.estado AS evento_estado, " +
                    "CONCAT(u.nombre, ' ', u.apellido) AS cliente_nombre " +
                    "FROM pago p " +
                    "JOIN evento e ON e.id_evento = p.id_evento " +
                    "JOIN cliente cl ON cl.id_cliente = e.id_cliente " +
                    "JOIN usuario u ON u.id = cl.id_usuario ";

    private static final String ORDEN = "ORDER BY p.fecha_pago DESC, p.id_pago DESC";

    @Override
    public Integer registrar(Pago pago) {
        Connection conn = null;
        try {
            conn = ConexionDB.obtenerConexion();
            conn.setAutoCommit(false);

            // Bloquea el evento: dos pagos a la vez se ordenan uno detrás del otro y no se sobrepasa el total
            if (!bloquearEvento(conn, pago.getIdEvento())) {
                conn.rollback();
                return null;
            }
            BigDecimal total = totalAcordado(conn, pago.getIdEvento());
            if (total == null) {
                conn.rollback();
                return null;
            }
            if (Pago.COMPLETADO.equals(pago.getEstado())
                    && pagado(conn, pago.getIdEvento()).add(pago.getMonto()).compareTo(total) > 0) {
                conn.rollback();
                return null;
            }

            int idNuevo;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO pago (id_evento, monto, fecha_pago, metodo_pago, estado, comprobante_url) " +
                            "VALUES (?, ?, COALESCE(?, CURRENT_TIMESTAMP), ?, ?, ?) RETURNING id_pago")) {
                ps.setInt(1, pago.getIdEvento());
                ps.setBigDecimal(2, pago.getMonto());
                if (pago.getFechaPago() != null) {
                    ps.setTimestamp(3, Timestamp.from(pago.getFechaPago().atZone(ZONA).toInstant()));
                } else {
                    ps.setNull(3, Types.TIMESTAMP);
                }
                ps.setString(4, pago.getMetodoPago());
                ps.setString(5, pago.getEstado());
                if (pago.getComprobanteUrl() != null && !pago.getComprobanteUrl().isEmpty()) {
                    ps.setString(6, pago.getComprobanteUrl());
                } else {
                    ps.setNull(6, Types.VARCHAR);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    idNuevo = rs.getInt(1);
                }
            }
            conn.commit();
            return idNuevo;
        } catch (SQLException e) {
            System.err.println("Error en PagoDAOImpl.registrar: " + e.getMessage());
            e.printStackTrace();
            rollback(conn);
            return null;
        } finally {
            cerrar(conn);
        }
    }

    @Override
    public boolean confirmar(int idPago) {
        Connection conn = null;
        try {
            conn = ConexionDB.obtenerConexion();
            conn.setAutoCommit(false);

            Integer idEvento = null;
            BigDecimal monto = null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id_evento, monto FROM pago WHERE id_pago = ? AND estado = 'PENDIENTE'")) {
                ps.setInt(1, idPago);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        idEvento = rs.getInt("id_evento");
                        monto = rs.getBigDecimal("monto");
                    }
                }
            }
            if (idEvento == null || !bloquearEvento(conn, idEvento)) {
                conn.rollback();
                return false;
            }
            BigDecimal total = totalAcordado(conn, idEvento);
            if (total == null || pagado(conn, idEvento).add(monto).compareTo(total) > 0) {
                conn.rollback();
                return false;
            }
            int filas;
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE pago SET estado = 'COMPLETADO' WHERE id_pago = ? AND estado = 'PENDIENTE'")) {
                ps.setInt(1, idPago);
                filas = ps.executeUpdate();
            }
            if (filas != 1) {
                conn.rollback();
                return false;
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error en PagoDAOImpl.confirmar: " + e.getMessage());
            e.printStackTrace();
            rollback(conn);
            return false;
        } finally {
            cerrar(conn);
        }
    }

    private boolean bloquearEvento(Connection conn, int idEvento) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM evento WHERE id_evento = ? FOR UPDATE")) {
            ps.setInt(1, idEvento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private BigDecimal totalAcordado(Connection conn, int idEvento) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT total FROM cotizacion WHERE id_evento = ? AND estado = 'ACEPTADA' ORDER BY id_cotizacion DESC LIMIT 1")) {
            ps.setInt(1, idEvento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : null;
            }
        }
    }

    private BigDecimal pagado(Connection conn, int idEvento) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COALESCE(SUM(monto), 0) FROM pago WHERE id_evento = ? AND estado = 'COMPLETADO'")) {
            ps.setInt(1, idEvento);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    @Override
    public BigDecimal totalAcordado(int idEvento) {
        try (Connection conn = ConexionDB.obtenerConexion()) {
            return totalAcordado(conn, idEvento);
        } catch (SQLException e) {
            System.err.println("Error en PagoDAOImpl.totalAcordado: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Pago buscarPorId(int idPago) {
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE p.id_pago = ?")) {
            ps.setInt(1, idPago);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            System.err.println("Error en PagoDAOImpl.buscarPorId: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Pago> listarPorEvento(int idEvento) {
        List<Pago> lista = new ArrayList<>();
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE p.id_evento = ? " + ORDEN)) {
            ps.setInt(1, idEvento);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en PagoDAOImpl.listarPorEvento: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public List<Pago> buscar(String texto, String filtro) {
        List<Pago> lista = new ArrayList<>();
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
            System.err.println("Error en PagoDAOImpl.buscar: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    /** Arma el SQL del listado y llena params en el orden de los "?" (separado para poder probarlo). */
    static String construirConsultaBusqueda(String texto, String filtro, List<String> params) {
        StringBuilder sql = new StringBuilder(SELECT_BASE + "WHERE 1=1 ");

        String f = filtro == null ? "" : filtro.trim().toUpperCase();
        switch (f) {
            case Pago.PENDIENTE:
            case Pago.COMPLETADO:
            case Pago.RECHAZADO:
                sql.append("AND p.estado = '").append(f).append("' ");
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
    public boolean cambiarEstado(int idPago, String estadoActual, String estadoNuevo) {
        String sql = "UPDATE pago SET estado = ? WHERE id_pago = ? AND estado = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, estadoNuevo);
            ps.setInt(2, idPago);
            ps.setString(3, estadoActual);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("Error en PagoDAOImpl.cambiarEstado: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int contarPorEstado(String estado) {
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM pago WHERE estado = ?")) {
            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            System.err.println("Error en PagoDAOImpl.contarPorEstado: " + e.getMessage());
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

    private Pago mapear(ResultSet rs) throws SQLException {
        Pago p = new Pago();
        p.setIdPago(rs.getInt("id_pago"));
        p.setIdEvento(rs.getInt("id_evento"));
        p.setMonto(rs.getBigDecimal("monto"));
        Timestamp ts = rs.getTimestamp("fecha_pago");
        if (ts != null) p.setFechaPago(ts.toInstant().atZone(ZONA).toLocalDateTime());
        p.setMetodoPago(rs.getString("metodo_pago"));
        p.setEstado(rs.getString("estado"));
        p.setComprobanteUrl(rs.getString("comprobante_url"));
        p.setIdCliente(rs.getInt("id_cliente"));
        p.setNombreEvento(rs.getString("evento_nombre"));
        p.setNombreCliente(rs.getString("cliente_nombre"));
        p.setEstadoEvento(rs.getString("evento_estado"));
        return p;
    }
}
