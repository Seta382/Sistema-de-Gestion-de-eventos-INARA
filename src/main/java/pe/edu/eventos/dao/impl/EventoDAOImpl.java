package pe.edu.eventos.dao.impl;

import pe.edu.eventos.dao.EventoDAO;
import pe.edu.eventos.model.Evento;
import pe.edu.eventos.model.EventoArticulo;
import pe.edu.eventos.util.ConexionDB;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EventoDAOImpl implements EventoDAO {

    @Override
    public boolean registrarEventoCompleto(Evento evento, List<EventoArticulo> insumos, List<Integer> idEmpleadosPersonal) {
        String sqlEvento = "INSERT INTO public.evento " +
                "(nombre, id_cliente, id_empleado, id_tipo_evento, fecha_evento, hora_evento, lugar, num_invitados, presupuesto, descripcion, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String sqlCheckStock = "SELECT id_articulo, nombre, stock, precio FROM public.articulo WHERE id_articulo = ? FOR UPDATE";
        String sqlUpdateStock = "UPDATE public.articulo SET stock = stock - ? WHERE id_articulo = ?";
        String sqlEventoArticulo = "INSERT INTO public.evento_articulo (id_evento, id_articulo, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
        String sqlEventoEmpleado = "INSERT INTO public.evento_empleado (id_evento, id_empleado, rol_en_evento) VALUES (?, ?, ?) ON CONFLICT (id_evento, id_empleado) DO NOTHING";

        Connection conn = null;
        try {
            conn = ConexionDB.obtenerConexion();
            conn.setAutoCommit(false); // Inicia transacción atómica

            // 1. Validar stock en la BD con bloqueo pesimista (FOR UPDATE)
            for (EventoArticulo item : insumos) {
                try (PreparedStatement psCheck = conn.prepareStatement(sqlCheckStock)) {
                    psCheck.setInt(1, item.getIdArticulo());
                    try (ResultSet rs = psCheck.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("El artículo con ID " + item.getIdArticulo() + " no existe.");
                        }
                        int stockActual = rs.getInt("stock");
                        String nombreArt = rs.getString("nombre");
                        BigDecimal precioBD = rs.getBigDecimal("precio");

                        if (item.getCantidad() == null || item.getCantidad() <= 0) {
                            throw new SQLException("Cantidad inválida (" + item.getCantidad() + ") para el insumo '" + nombreArt + "'.");
                        }
                        if (item.getCantidad() > stockActual) {
                            throw new SQLException("Stock insuficiente para '" + nombreArt + "'. Stock actual: " + stockActual + ", solicitado: " + item.getCantidad());
                        }

                        // Asegurar el precio unitario y subtotal si no venían seteados
                        if (item.getPrecioUnitario() == null || item.getPrecioUnitario().compareTo(BigDecimal.ZERO) <= 0) {
                            item.setPrecioUnitario(precioBD);
                        }
                        item.setSubtotal(item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad())));
                    }
                }
            }

            // 2. Insertar cabecera del Evento
            int idEventoGenerado;
            try (PreparedStatement psEv = conn.prepareStatement(sqlEvento, Statement.RETURN_GENERATED_KEYS)) {
                psEv.setString(1, evento.getNombre() != null ? evento.getNombre().trim() : "Evento INARA");
                psEv.setInt(2, evento.getIdCliente());

                if (evento.getIdEmpleado() != null && evento.getIdEmpleado() > 0) {
                    psEv.setInt(3, evento.getIdEmpleado());
                } else {
                    psEv.setNull(3, Types.INTEGER);
                }

                psEv.setInt(4, evento.getIdTipoEvento());
                psEv.setDate(5, Date.valueOf(evento.getFechaEvento()));

                if (evento.getHoraEvento() != null) {
                    psEv.setTime(6, Time.valueOf(evento.getHoraEvento()));
                } else {
                    psEv.setTime(6, Time.valueOf("18:00:00"));
                }

                if (evento.getLugar() != null && !evento.getLugar().trim().isEmpty()) {
                    psEv.setString(7, evento.getLugar().trim());
                } else {
                    psEv.setString(7, "Salón Principal INARA");
                }

                psEv.setInt(8, evento.getNumInvitados() != null ? evento.getNumInvitados() : 0);
                psEv.setBigDecimal(9, evento.getPresupuesto() != null ? evento.getPresupuesto() : BigDecimal.ZERO);
                psEv.setString(10, evento.getDescripcion() != null ? evento.getDescripcion().trim() : "Registro en vivo");
                psEv.setString(11, evento.getEstado() != null ? evento.getEstado() : "CONFIRMADO");

                int filas = psEv.executeUpdate();
                if (filas == 0) {
                    throw new SQLException("No se pudo registrar la cabecera del evento.");
                }

                try (ResultSet rsKeys = psEv.getGeneratedKeys()) {
                    if (rsKeys.next()) {
                        idEventoGenerado = rsKeys.getInt(1);
                        evento.setIdEvento(idEventoGenerado);
                    } else {
                        throw new SQLException("No se obtuvo el ID generado para el evento.");
                    }
                }
            }

            // 3. Insertar insumos y actualizar stock
            try (PreparedStatement psItem = conn.prepareStatement(sqlEventoArticulo);
                 PreparedStatement psStock = conn.prepareStatement(sqlUpdateStock)) {

                for (EventoArticulo item : insumos) {
                    psItem.setInt(1, idEventoGenerado);
                    psItem.setInt(2, item.getIdArticulo());
                    psItem.setInt(3, item.getCantidad());
                    psItem.setBigDecimal(4, item.getPrecioUnitario());
                    psItem.setBigDecimal(5, item.getSubtotal());
                    psItem.executeUpdate();

                    // Descontar del inventario real
                    psStock.setInt(1, item.getCantidad());
                    psStock.setInt(2, item.getIdArticulo());
                    psStock.executeUpdate();
                }
            }

            // 4. Insertar personal operativo en evento_empleado (incluyendo coordinador y equipo sin duplicados)
            Set<Integer> empleadosUnicos = new HashSet<>();
            if (evento.getIdEmpleado() != null && evento.getIdEmpleado() > 0) {
                empleadosUnicos.add(evento.getIdEmpleado());
            }
            if (idEmpleadosPersonal != null) {
                empleadosUnicos.addAll(idEmpleadosPersonal);
            }

            if (!empleadosUnicos.isEmpty()) {
                try (PreparedStatement psStaff = conn.prepareStatement(sqlEventoEmpleado)) {
                    for (Integer empId : empleadosUnicos) {
                        psStaff.setInt(1, idEventoGenerado);
                        psStaff.setInt(2, empId);
                        boolean esCoordinador = (evento.getIdEmpleado() != null && empId.equals(evento.getIdEmpleado()));
                        psStaff.setString(3, esCoordinador ? "COORDINADOR_GENERAL" : "STAFF_OPERATIVO");
                        psStaff.executeUpdate();
                    }
                }
            }

            // 5. Confirmar transacción
            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error en transacción registrarEventoCompleto: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("Rollback de transacción ejecutado exitosamente.");
                } catch (SQLException ex) {
                    System.err.println("Error durante rollback: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    System.err.println("Error cerrando conexión: " + ex.getMessage());
                }
            }
        }
    }

    @Override
    public List<Evento> listarEventosActivos() {
        List<Evento> lista = new ArrayList<>();
        String sql = "SELECT e.id_evento, e.nombre, e.id_cliente, e.id_empleado, e.id_tipo_evento, " +
                "e.fecha_evento, e.hora_evento, e.lugar, e.num_invitados, e.presupuesto, " +
                "e.descripcion, e.estado, e.creado_en, " +
                "te.nombre AS tipo_celebracion, " +
                "CONCAT(uc.nombre, ' ', uc.apellido) AS cliente_nombre, " +
                "CONCAT(ue.nombre, ' ', ue.apellido) AS coordinador_nombre " +
                "FROM public.evento e " +
                "LEFT JOIN public.tipo_evento te ON e.id_tipo_evento = te.id_tipo_evento " +
                "LEFT JOIN public.cliente c ON e.id_cliente = c.id_cliente " +
                "LEFT JOIN public.usuario uc ON c.id_usuario = uc.id " +
                "LEFT JOIN public.empleado emp ON e.id_empleado = emp.id_empleado " +
                "LEFT JOIN public.usuario ue ON emp.id_usuario = ue.id " +
                "ORDER BY e.creado_en DESC, e.fecha_evento ASC";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Evento ev = new Evento();
                ev.setIdEvento(rs.getInt("id_evento"));
                ev.setNombre(rs.getString("nombre"));
                ev.setIdCliente(rs.getInt("id_cliente"));
                ev.setIdEmpleado(rs.getInt("id_empleado"));
                ev.setIdTipoEvento(rs.getInt("id_tipo_evento"));

                Date d = rs.getDate("fecha_evento");
                if (d != null) ev.setFechaEvento(d.toLocalDate());

                Time t = rs.getTime("hora_evento");
                if (t != null) ev.setHoraEvento(t.toLocalTime());

                ev.setLugar(rs.getString("lugar"));
                ev.setNumInvitados(rs.getInt("num_invitados"));
                ev.setPresupuesto(rs.getBigDecimal("presupuesto"));
                ev.setDescripcion(rs.getString("descripcion"));
                ev.setEstado(rs.getString("estado"));

                Timestamp ts = rs.getTimestamp("creado_en");
                if (ts != null) ev.setCreadoEn(ts.toLocalDateTime());

                ev.setTipoCelebracion(rs.getString("tipo_celebracion"));
                ev.setNombreCliente(rs.getString("cliente_nombre"));
                ev.setNombreCoordinador(rs.getString("coordinador_nombre"));

                lista.add(ev);
            }
        } catch (SQLException e) {
            System.err.println("Error en EventoDAOImpl.listarEventosActivos: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public Evento buscarPorId(int idEvento) {
        String sql = "SELECT e.id_evento, e.nombre, e.id_cliente, e.id_empleado, e.id_tipo_evento, " +
                "e.fecha_evento, e.hora_evento, e.lugar, e.num_invitados, e.presupuesto, " +
                "e.descripcion, e.estado, e.creado_en, " +
                "te.nombre AS tipo_celebracion, " +
                "CONCAT(uc.nombre, ' ', uc.apellido) AS cliente_nombre, " +
                "CONCAT(ue.nombre, ' ', ue.apellido) AS coordinador_nombre " +
                "FROM public.evento e " +
                "LEFT JOIN public.tipo_evento te ON e.id_tipo_evento = te.id_tipo_evento " +
                "LEFT JOIN public.cliente c ON e.id_cliente = c.id_cliente " +
                "LEFT JOIN public.usuario uc ON c.id_usuario = uc.id " +
                "LEFT JOIN public.empleado emp ON e.id_empleado = emp.id_empleado " +
                "LEFT JOIN public.usuario ue ON emp.id_usuario = ue.id " +
                "WHERE e.id_evento = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEvento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Evento ev = new Evento();
                    ev.setIdEvento(rs.getInt("id_evento"));
                    ev.setNombre(rs.getString("nombre"));
                    ev.setIdCliente(rs.getInt("id_cliente"));
                    ev.setIdEmpleado(rs.getInt("id_empleado"));
                    ev.setIdTipoEvento(rs.getInt("id_tipo_evento"));

                    Date d = rs.getDate("fecha_evento");
                    if (d != null) ev.setFechaEvento(d.toLocalDate());

                    Time t = rs.getTime("hora_evento");
                    if (t != null) ev.setHoraEvento(t.toLocalTime());

                    ev.setLugar(rs.getString("lugar"));
                    ev.setNumInvitados(rs.getInt("num_invitados"));
                    ev.setPresupuesto(rs.getBigDecimal("presupuesto"));
                    ev.setDescripcion(rs.getString("descripcion"));
                    ev.setEstado(rs.getString("estado"));

                    Timestamp ts = rs.getTimestamp("creado_en");
                    if (ts != null) ev.setCreadoEn(ts.toLocalDateTime());

                    ev.setTipoCelebracion(rs.getString("tipo_celebracion"));
                    ev.setNombreCliente(rs.getString("cliente_nombre"));
                    ev.setNombreCoordinador(rs.getString("coordinador_nombre"));
                    return ev;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en EventoDAOImpl.buscarPorId: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<EventoArticulo> listarInsumosPorEvento(int idEvento) {
        List<EventoArticulo> lista = new ArrayList<>();
        String sql = "SELECT ea.id_evento_articulo, ea.id_evento, ea.id_articulo, ea.cantidad, " +
                "ea.precio_unitario, ea.subtotal, a.nombre AS articulo_nombre " +
                "FROM public.evento_articulo ea " +
                "JOIN public.articulo a ON ea.id_articulo = a.id_articulo " +
                "WHERE ea.id_evento = ? " +
                "ORDER BY ea.id_evento_articulo ASC";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEvento);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EventoArticulo item = new EventoArticulo(
                            rs.getInt("id_evento"),
                            rs.getInt("id_articulo"),
                            rs.getInt("cantidad"),
                            rs.getBigDecimal("precio_unitario"),
                            rs.getBigDecimal("subtotal")
                    );
                    item.setIdEventoArticulo(rs.getInt("id_evento_articulo"));
                    item.setNombreArticulo(rs.getString("articulo_nombre"));
                    lista.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en EventoDAOImpl.listarInsumosPorEvento: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }
}
