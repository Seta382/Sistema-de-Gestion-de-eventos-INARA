package pe.edu.eventos.dao.impl;

import pe.edu.eventos.dao.CitaDAO;
import pe.edu.eventos.model.Cita;
import pe.edu.eventos.util.ConexionDB;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class CitaDAOImpl implements CitaDAO {

    private static final String COLUMNAS =
            "id_cita, id_cliente, id_empleado, fecha, hora, modalidad, lugar, motivo, estado, creado_en";

    @Override
    public boolean crear(Cita cita) {
        String sql = "INSERT INTO public.cita (id_cliente, fecha, hora, modalidad, lugar, motivo, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, cita.getIdCliente());
            ps.setDate(2, cita.getFecha());
            ps.setTime(3, cita.getHora());
            ps.setString(4, cita.getModalidad());
            ps.setString(5, cita.getLugar());
            ps.setString(6, cita.getMotivo());
            ps.setString(7, cita.getEstado());
            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        cita.setIdCita(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Cita buscarPorId(int idCita) {
        String sql = "SELECT " + COLUMNAS + " FROM public.cita WHERE id_cita = ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCita);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Cita> listarPorCliente(int idCliente) {
        String sql = "SELECT " + COLUMNAS + " FROM public.cita WHERE id_cliente = ? ORDER BY fecha, hora";
        return listar(sql, idCliente);
    }

    @Override
    public List<Cita> listarPorEmpleado(int idEmpleado) {
        String sql = "SELECT " + COLUMNAS + " FROM public.cita WHERE id_empleado = ? ORDER BY fecha, hora";
        return listar(sql, idEmpleado);
    }

    @Override
    public List<Cita> listarPorEstado(String estado) {
        List<Cita> citas = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM public.cita WHERE estado = ? ORDER BY fecha, hora";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    citas.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return citas;
    }

    @Override
    public List<Cita> listarConfirmadasSinEvento() {
        List<Cita> citas = new ArrayList<>();
        String sql = "SELECT c.id_cita, c.id_cliente, c.id_empleado, c.fecha, c.hora, c.modalidad, " +
                "c.lugar, c.motivo, c.estado, c.creado_en, " +
                "CONCAT(u.nombre, ' ', u.apellido) AS nombre_cliente " +
                "FROM public.cita c " +
                "JOIN public.cliente cli ON c.id_cliente = cli.id_cliente " +
                "JOIN public.usuario u ON cli.id_usuario = u.id " +
                "WHERE c.estado = 'CONFIRMADA' " +
                "AND NOT EXISTS (SELECT 1 FROM public.evento e WHERE e.id_cita = c.id_cita) " +
                "ORDER BY c.fecha, c.hora";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Cita cita = mapear(rs);
                cita.setNombreCliente(rs.getString("nombre_cliente"));
                citas.add(cita);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return citas;
    }

    @Override
    public List<Cita> listarTodasConCliente(String estado, String busqueda) {
        List<Cita> citas = new ArrayList<>();
        List<String> params = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT c.id_cita, c.id_cliente, c.id_empleado, c.fecha, c.hora, c.modalidad, " +
                        "c.lugar, c.motivo, c.estado, c.creado_en, " +
                        "CONCAT(u.nombre, ' ', u.apellido) AS nombre_cliente " +
                        "FROM public.cita c " +
                        "JOIN public.cliente cli ON c.id_cliente = cli.id_cliente " +
                        "JOIN public.usuario u ON cli.id_usuario = u.id " +
                        "WHERE 1=1 ");

        if (estado != null && !estado.trim().isEmpty()) {
            sql.append("AND c.estado = ? ");
            params.add(estado.trim().toUpperCase());
        }

        // Cada palabra escrita debe coincidir en al menos uno de estos campos (AND entre palabras)
        for (String palabra : tokenizar(busqueda)) {
            sql.append("AND (CONCAT(u.nombre, ' ', u.apellido) ILIKE ? ")
                    .append("OR cli.dni ILIKE ? ")
                    .append("OR c.motivo ILIKE ? ")
                    .append("OR c.lugar ILIKE ? ")
                    .append("OR c.modalidad ILIKE ? ")
                    .append("OR TO_CHAR(c.fecha, 'YYYY-MM-DD') ILIKE ? ")
                    .append("OR TO_CHAR(c.fecha, 'DD/MM/YYYY') ILIKE ?) ");
            String patron = "%" + escaparLike(palabra) + "%";
            for (int i = 0; i < 7; i++) {
                params.add(patron);
            }
        }

        sql.append("ORDER BY c.fecha, c.hora");

        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Cita cita = mapear(rs);
                    cita.setNombreCliente(rs.getString("nombre_cliente"));
                    citas.add(cita);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return citas;
    }

    /** Separa el texto en palabras (máx. 6, máx. 50 caracteres cada una). */
    private List<String> tokenizar(String texto) {
        List<String> palabras = new ArrayList<>();
        if (texto == null || texto.trim().isEmpty()) {
            return palabras;
        }
        for (String p : texto.trim().split("\\s+")) {
            if (p.isEmpty()) continue;
            palabras.add(p.length() > 50 ? p.substring(0, 50) : p);
            if (palabras.size() == 6) break;
        }
        return palabras;
    }

    /** Escapa los comodines de LIKE para que "50%" o "a_b" se busquen tal cual (escape por defecto: \). */
    private String escaparLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    @Override
    public int contarPorEstado(String estado) {
        String sql = "SELECT COUNT(*) FROM public.cita WHERE estado = ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean actualizarEstado(int idCita, String nuevoEstado) {
        String sql = "UPDATE public.cita SET estado = ? WHERE id_cita = ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idCita);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean reprogramar(int idCita, Date nuevaFecha, Time nuevaHora) {
        String sql = "UPDATE public.cita SET fecha = ?, hora = ?, estado = 'REPROGRAMADA' WHERE id_cita = ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, nuevaFecha);
            ps.setTime(2, nuevaHora);
            ps.setInt(3, idCita);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean asignarEmpleado(int idCita, int idEmpleado) {
        String sql = "UPDATE public.cita SET id_empleado = ? WHERE id_cita = ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpleado);
            ps.setInt(2, idCita);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean eliminar(int idCita) {
        String sql = "DELETE FROM public.cita WHERE id_cita = ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCita);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private List<Cita> listar(String sql, int parametro) {
        List<Cita> citas = new ArrayList<>();
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    citas.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return citas;
    }

    private Cita mapear(ResultSet rs) throws SQLException {
        Cita cita = new Cita();
        cita.setIdCita(rs.getInt("id_cita"));
        cita.setIdCliente(rs.getInt("id_cliente"));
        int idEmpleado = rs.getInt("id_empleado");
        cita.setIdEmpleado(rs.wasNull() ? null : idEmpleado);
        cita.setFecha(rs.getDate("fecha"));
        cita.setHora(rs.getTime("hora"));
        cita.setModalidad(rs.getString("modalidad"));
        cita.setLugar(rs.getString("lugar"));
        cita.setMotivo(rs.getString("motivo"));
        cita.setEstado(rs.getString("estado"));
        cita.setCreadoEn(rs.getTimestamp("creado_en"));
        return cita;
    }
}
