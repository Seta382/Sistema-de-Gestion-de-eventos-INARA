package pe.edu.eventos.dao.impl;

import pe.edu.eventos.dao.EventoDAO;
import pe.edu.eventos.model.Evento;
import pe.edu.eventos.model.EventoArticulo;
import pe.edu.eventos.util.BusquedaUtil;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EventoDAOImpl implements EventoDAO {

    // Columnas y joins comunes a todas las consultas que devuelven eventos listos para mostrar
    private static final String SELECT_EVENTO =
            "SELECT e.id_evento, e.nombre, e.id_cliente, e.id_empleado, e.id_tipo_evento, " +
                    "e.fecha_evento, e.hora_evento, e.lugar, e.num_invitados, e.presupuesto, " +
                    "e.descripcion, e.estado, e.creado_en, e.id_cita, " +
                    "te.nombre AS tipo_celebracion, " +
                    "CONCAT(uc.nombre, ' ', uc.apellido) AS cliente_nombre, " +
                    "CONCAT(ue.nombre, ' ', ue.apellido) AS coordinador_nombre ";

    private static final String FROM_EVENTO =
            "FROM public.evento e " +
                    "LEFT JOIN public.tipo_evento te ON e.id_tipo_evento = te.id_tipo_evento " +
                    "LEFT JOIN public.cliente c ON e.id_cliente = c.id_cliente " +
                    "LEFT JOIN public.usuario uc ON c.id_usuario = uc.id " +
                    "LEFT JOIN public.empleado emp ON e.id_empleado = emp.id_empleado " +
                    "LEFT JOIN public.usuario ue ON emp.id_usuario = ue.id ";

    @Override
    public boolean registrarEventoCompleto(Evento evento, List<EventoArticulo> insumos, List<Integer> idEmpleadosPersonal) {
        // FIX: se agrega id_cita al INSERT (columna 12) para enlazar el evento con la cita de origen
        String sqlEvento = "INSERT INTO public.evento " +
                "(nombre, id_cliente, id_empleado, id_tipo_evento, fecha_evento, hora_evento, lugar, num_invitados, presupuesto, descripcion, estado, id_cita) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String sqlCheckStock = "SELECT id_articulo, nombre, stock, precio FROM public.articulo WHERE id_articulo = ? FOR UPDATE";
        String sqlUpdateStock = "UPDATE public.articulo SET stock = stock - ? WHERE id_articulo = ?";
        String sqlEventoArticulo = "INSERT INTO public.evento_articulo (id_evento, id_articulo, cantidad, precio_unitario, subtotal) VALUES (?, ?, ?, ?, ?)";
        String sqlEventoEmpleado = "INSERT INTO public.evento_empleado (id_evento, id_empleado, rol_en_evento) VALUES (?, ?, ?) ON CONFLICT (id_evento, id_empleado) DO NOTHING";

        // FIX: si el evento viene de una cita, esa cita pasa a FINALIZADA (ya se convirtió en evento)
        String sqlActualizarCita = "UPDATE public.cita SET estado = 'FINALIZADA' WHERE id_cita = ?";

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

                // FIX: id_cita (columna 12), nullable
                if (evento.getIdCita() != null && evento.getIdCita() > 0) {
                    psEv.setInt(12, evento.getIdCita());
                } else {
                    psEv.setNull(12, Types.INTEGER);
                }

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

            // 2.1 FIX: si el evento viene de una cita, marcarla como FINALIZADA
            if (evento.getIdCita() != null && evento.getIdCita() > 0) {
                try (PreparedStatement psCita = conn.prepareStatement(sqlActualizarCita)) {
                    psCita.setInt(1, evento.getIdCita());
                    psCita.executeUpdate();
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

                    psStock.setInt(1, item.getCantidad());
                    psStock.setInt(2, item.getIdArticulo());
                    psStock.executeUpdate();
                }
            }

            // 4. Insertar personal operativo en evento_empleado
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
        String sql = SELECT_EVENTO + FROM_EVENTO +
                "ORDER BY e.creado_en DESC, e.fecha_evento ASC";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearEvento(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en EventoDAOImpl.listarEventosActivos: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public Evento buscarPorId(int idEvento) {
        String sql = SELECT_EVENTO + FROM_EVENTO + "WHERE e.id_evento = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEvento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearEvento(rs);
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

    @Override
    public List<Evento> listarEventosRecientes(int limite) {
        int tope = Math.max(1, Math.min(limite, 100));
        List<Evento> lista = new ArrayList<>();
        String sql = SELECT_EVENTO + FROM_EVENTO +
                "ORDER BY e.creado_en DESC, e.fecha_evento ASC LIMIT ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tope);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearEvento(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en EventoDAOImpl.listarEventosRecientes: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public List<Evento> buscarEventos(String texto, String filtro) {
        List<Evento> lista = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        String sql = construirConsultaBusqueda(texto, filtro, LocalDate.now(ZONA_NEGOCIO), params);
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof Date) {
                    ps.setDate(i + 1, (Date) p);
                } else {
                    ps.setString(i + 1, (String) p);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearEvento(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en EventoDAOImpl.buscarEventos: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Arma el SQL del cronograma y llena params en el orden de los "?" (separado para poder probarlo).
     * "hoy" entra como parámetro (el primero) y se usa vía h.hoy, así la fecha de referencia es una sola
     * y no depende de la zona horaria del servidor de base de datos.
     */
    static String construirConsultaBusqueda(String texto, String filtro, LocalDate hoy, List<Object> params) {
        StringBuilder sql = new StringBuilder(SELECT_EVENTO + FROM_EVENTO +
                "CROSS JOIN (SELECT CAST(? AS date) AS hoy) h WHERE 1=1 ");
        params.add(Date.valueOf(hoy));

        String f = filtro == null ? "" : filtro.trim().toUpperCase();
        if (FILTRO_PROXIMOS.equals(f)) {
            sql.append("AND e.fecha_evento >= h.hoy ");
        } else if (FILTRO_PASADOS.equals(f)) {
            sql.append("AND e.fecha_evento < h.hoy ");
        }

        // Cada palabra debe coincidir en al menos uno de estos campos (AND entre palabras)
        for (String palabra : BusquedaUtil.tokenizar(texto)) {
            sql.append("AND (").append(BusquedaUtil.sinTildesSql("e.nombre")).append(" ILIKE ? ")
                    .append("OR ").append(BusquedaUtil.sinTildesSql("CONCAT(uc.nombre, ' ', uc.apellido)")).append(" ILIKE ? ")
                    .append("OR c.dni ILIKE ? ")
                    .append("OR ").append(BusquedaUtil.sinTildesSql("te.nombre")).append(" ILIKE ? ")
                    .append("OR ").append(BusquedaUtil.sinTildesSql("e.lugar")).append(" ILIKE ? ")
                    .append("OR ").append(BusquedaUtil.sinTildesSql("CONCAT(ue.nombre, ' ', ue.apellido)")).append(" ILIKE ? ")
                    .append("OR e.estado ILIKE ? ")
                    .append("OR TO_CHAR(e.fecha_evento, 'YYYY-MM-DD') ILIKE ? ")
                    .append("OR TO_CHAR(e.fecha_evento, 'DD/MM/YYYY') ILIKE ?) ");
            // El patrón también va sin tildes: "salon" encuentra "Salón" y "salón" también
            String patron = BusquedaUtil.patronContieneSinTildes(palabra);
            for (int i = 0; i < 9; i++) {
                params.add(patron);
            }
        }

        // Próximos primero (el más cercano arriba); después los pasados (el más reciente arriba)
        sql.append("ORDER BY (e.fecha_evento < h.hoy), ")
                .append("CASE WHEN e.fecha_evento >= h.hoy THEN e.fecha_evento END ASC, ")
                .append("e.fecha_evento DESC, e.hora_evento ASC");
        return sql.toString();
    }

    @Override
    public Map<String, Integer> contarPorPeriodo() {
        Map<String, Integer> conteo = new HashMap<>();
        conteo.put(FILTRO_TODOS, 0);
        conteo.put(FILTRO_PROXIMOS, 0);
        conteo.put(FILTRO_PASADOS, 0);

        String sql = "SELECT COUNT(*) AS total, COUNT(*) FILTER (WHERE fecha_evento >= ?) AS proximos " +
                "FROM public.evento";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(LocalDate.now(ZONA_NEGOCIO)));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int proximos = rs.getInt("proximos");
                    conteo.put(FILTRO_TODOS, total);
                    conteo.put(FILTRO_PROXIMOS, proximos);
                    conteo.put(FILTRO_PASADOS, total - proximos);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en EventoDAOImpl.contarPorPeriodo: " + e.getMessage());
            e.printStackTrace();
        }
        return conteo;
    }

        @Override
    public boolean cambiarEstado(int idEvento, String estadoActual, String estadoNuevo) {
        String sql = "UPDATE public.evento SET estado = ? WHERE id_evento = ? AND estado = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, estadoNuevo);
            ps.setInt(2, idEvento);
            ps.setString(3, estadoActual);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("Error en EventoDAOImpl.cambiarEstado: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private Evento mapearEvento(ResultSet rs) throws SQLException {
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

        // FIX: antes no se leía id_cita de vuelta
        int idCita = rs.getInt("id_cita");
        ev.setIdCita(rs.wasNull() ? null : idCita);

        ev.setTipoCelebracion(rs.getString("tipo_celebracion"));
        ev.setNombreCliente(rs.getString("cliente_nombre"));
        ev.setNombreCoordinador(rs.getString("coordinador_nombre"));
        return ev;
    }
}
