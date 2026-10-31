package pe.edu.eventos.dao.impl;

import pe.edu.eventos.dao.CotizacionDAO;
import pe.edu.eventos.model.Cotizacion;
import pe.edu.eventos.model.DetalleCotizacion;
import pe.edu.eventos.util.BusquedaUtil;
import pe.edu.eventos.util.ConexionDB;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class CotizacionDAOImpl implements CotizacionDAO {

    private static final String SELECT_BASE =
            "SELECT c.id_cotizacion, c.id_evento, c.fecha, c.subtotal, c.descuento, c.total, c.estado, " +
                    "c.observaciones, c.creado_en, e.nombre AS evento_nombre, e.id_cliente, " +
                    "e.fecha_evento, e.estado AS evento_estado, " +
                    "CONCAT(u.nombre, ' ', u.apellido) AS cliente_nombre " +
                    "FROM cotizacion c " +
                    "JOIN evento e ON e.id_evento = c.id_evento " +
                    "JOIN cliente cl ON cl.id_cliente = e.id_cliente " +
                    "JOIN usuario u ON u.id = cl.id_usuario ";

    private static final String ORDEN = "ORDER BY c.creado_en DESC, c.id_cotizacion DESC";

    @Override
    public Integer registrar(Cotizacion cotizacion, List<DetalleCotizacion> detalles) {
        Connection conn = null;
        try {
            conn = ConexionDB.obtenerConexion();
            conn.setAutoCommit(false);

            // Bloquea el evento: dos personas cotizando a la vez se ordenan una detrás de otra
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT estado FROM evento WHERE id_evento = ? FOR UPDATE")) {
                ps.setInt(1, cotizacion.getIdEvento());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return null;
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT 1 FROM cotizacion WHERE id_evento = ? AND estado = 'ACEPTADA'")) {
                ps.setInt(1, cotizacion.getIdEvento());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        conn.rollback();
                        return null;
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE cotizacion SET estado = 'MODIFICADA' WHERE id_evento = ? AND estado = 'PENDIENTE'")) {
                ps.setInt(1, cotizacion.getIdEvento());
                ps.executeUpdate();
            }

            int idNuevo;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO cotizacion (id_evento, subtotal, descuento, total, estado, observaciones) " +
                            "VALUES (?, ?, ?, ?, 'PENDIENTE', ?) RETURNING id_cotizacion")) {
                ps.setInt(1, cotizacion.getIdEvento());
                ps.setBigDecimal(2, cotizacion.getSubtotal());
                ps.setBigDecimal(3, cotizacion.getDescuento());
                ps.setBigDecimal(4, cotizacion.getTotal());
                if (cotizacion.getObservaciones() != null && !cotizacion.getObservaciones().trim().isEmpty()) {
                    ps.setString(5, cotizacion.getObservaciones().trim());
                } else {
                    ps.setNull(5, Types.VARCHAR);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    idNuevo = rs.getInt(1);
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO detalle_cotizacion (id_cotizacion, id_servicio, cantidad, precio_unitario, subtotal) " +
                            "VALUES (?, ?, ?, ?, ?)")) {
                for (DetalleCotizacion d : detalles) {
                    ps.setInt(1, idNuevo);
                    ps.setInt(2, d.getIdServicio());
                    ps.setInt(3, d.getCantidad());
                    ps.setBigDecimal(4, d.getPrecioUnitario());
                    ps.setBigDecimal(5, d.getSubtotal());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE evento SET estado = 'COTIZADO' WHERE id_evento = ? AND estado = 'SOLICITADO'")) {
                ps.setInt(1, cotizacion.getIdEvento());
                ps.executeUpdate();
            }

            conn.commit();
            return idNuevo;
        } catch (SQLException e) {
            System.err.println("Error en CotizacionDAOImpl.registrar: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("No se pudo hacer rollback: " + ex.getMessage());
                }
            }
            return null;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                    // la conexión ya no se usa
                }
            }
        }
    }

    @Override
    public Cotizacion buscarPorId(int idCotizacion) {
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + "WHERE c.id_cotizacion = ?")) {
            ps.setInt(1, idCotizacion);
            Cotizacion c = null;
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) c = mapear(rs);
            }
            if (c != null) cargarDetalles(conn, List.of(c));
            return c;
        } catch (SQLException e) {
            System.err.println("Error en CotizacionDAOImpl.buscarPorId: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Cotizacion> listarPorEvento(int idEvento) {
        return listarConDetalles("WHERE c.id_evento = ? ", idEvento);
    }

    @Override
    public List<Cotizacion> listarPorCliente(int idCliente) {
        return listarConDetalles("WHERE e.id_cliente = ? ", idCliente);
    }

    private List<Cotizacion> listarConDetalles(String where, int parametro) {
        List<Cotizacion> lista = new ArrayList<>();
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(SELECT_BASE + where + ORDEN)) {
            ps.setInt(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
            cargarDetalles(conn, lista);
        } catch (SQLException e) {
            System.err.println("Error en CotizacionDAOImpl.listar: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public List<Cotizacion> buscar(String texto, String filtro) {
        List<Cotizacion> lista = new ArrayList<>();
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
            System.err.println("Error en CotizacionDAOImpl.buscar: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    /** Arma el SQL del listado y llena params en el orden de los "?" (separado para poder probarlo). */
    static String construirConsultaBusqueda(String texto, String filtro, List<String> params) {
        StringBuilder sql = new StringBuilder(SELECT_BASE + "WHERE 1=1 ");

        String f = filtro == null ? "" : filtro.trim().toUpperCase();
        switch (f) {
            case Cotizacion.PENDIENTE:
            case Cotizacion.ACEPTADA:
            case Cotizacion.RECHAZADA:
            case Cotizacion.MODIFICADA:
                sql.append("AND c.estado = '").append(f).append("' ");
                break;
            default:
                break; // todas
        }

        // Cada palabra debe coincidir con el evento o el cliente, sin importar tildes (AND entre palabras)
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
    public boolean cambiarEstado(int idCotizacion, String estadoActual, String estadoNuevo) {
        String sql = "UPDATE cotizacion SET estado = ? WHERE id_cotizacion = ? AND estado = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, estadoNuevo);
            ps.setInt(2, idCotizacion);
            ps.setString(3, estadoActual);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("Error en CotizacionDAOImpl.cambiarEstado: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int contarPorEstado(String estado) {
        return contar("SELECT COUNT(*) FROM cotizacion WHERE estado = ?", estado);
    }

    @Override
    public int contarPendientesPorCliente(int idCliente) {
        String sql = "SELECT COUNT(*) FROM cotizacion c JOIN evento e ON e.id_evento = c.id_evento " +
                "WHERE e.id_cliente = ? AND c.estado = 'PENDIENTE'";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            System.err.println("Error en CotizacionDAOImpl.contarPendientesPorCliente: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    private int contar(String sql, String estado) {
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            System.err.println("Error en CotizacionDAOImpl.contarPorEstado: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /** Carga las líneas de cada cotización con la misma conexión. */
    private void cargarDetalles(Connection conn, List<Cotizacion> cotizaciones) throws SQLException {
        String sql = "SELECT d.id_detalle, d.id_cotizacion, d.id_servicio, d.cantidad, d.precio_unitario, d.subtotal, " +
                "s.nombre AS servicio_nombre " +
                "FROM detalle_cotizacion d JOIN servicio s ON s.id_servicio = d.id_servicio " +
                "WHERE d.id_cotizacion = ? ORDER BY d.id_detalle";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Cotizacion c : cotizaciones) {
                ps.setInt(1, c.getIdCotizacion());
                List<DetalleCotizacion> detalles = new ArrayList<>();
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        DetalleCotizacion d = new DetalleCotizacion(
                                rs.getInt("id_servicio"), rs.getInt("cantidad"),
                                rs.getBigDecimal("precio_unitario"), rs.getBigDecimal("subtotal"));
                        d.setIdDetalle(rs.getInt("id_detalle"));
                        d.setIdCotizacion(rs.getInt("id_cotizacion"));
                        d.setNombreServicio(rs.getString("servicio_nombre"));
                        detalles.add(d);
                    }
                }
                c.setDetalles(detalles);
            }
        }
    }

    private Cotizacion mapear(ResultSet rs) throws SQLException {
        Cotizacion c = new Cotizacion();
        c.setIdCotizacion(rs.getInt("id_cotizacion"));
        c.setIdEvento(rs.getInt("id_evento"));
        Date fecha = rs.getDate("fecha");
        if (fecha != null) c.setFecha(fecha.toLocalDate());
        c.setSubtotal(rs.getBigDecimal("subtotal"));
        c.setDescuento(rs.getBigDecimal("descuento"));
        c.setTotal(rs.getBigDecimal("total"));
        c.setEstado(rs.getString("estado"));
        c.setObservaciones(rs.getString("observaciones"));
        Timestamp ts = rs.getTimestamp("creado_en");
        if (ts != null) c.setCreadoEn(ts.toLocalDateTime());
        c.setIdCliente(rs.getInt("id_cliente"));
        c.setNombreEvento(rs.getString("evento_nombre"));
        c.setNombreCliente(rs.getString("cliente_nombre"));
        Date fe = rs.getDate("fecha_evento");
        if (fe != null) c.setFechaEvento(fe.toLocalDate());
        c.setEstadoEvento(rs.getString("evento_estado"));
        return c;
    }
}
