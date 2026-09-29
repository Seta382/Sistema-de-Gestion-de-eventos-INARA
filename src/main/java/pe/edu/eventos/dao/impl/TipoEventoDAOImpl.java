package pe.edu.eventos.dao.impl;

import pe.edu.eventos.dao.TipoEventoDAO;
import pe.edu.eventos.model.TipoEvento;
import pe.edu.eventos.util.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TipoEventoDAOImpl implements TipoEventoDAO {

    @Override
    public List<TipoEvento> listarTodos() {
        List<TipoEvento> lista = new ArrayList<>();
        String sql = "SELECT id_tipo_evento, nombre, descripcion FROM public.tipo_evento ORDER BY id_tipo_evento ASC";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new TipoEvento(
                        rs.getInt("id_tipo_evento"),
                        rs.getString("nombre"),
                        rs.getString("descripcion")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error en TipoEventoDAOImpl.listarTodos: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public TipoEvento buscarPorId(int idTipoEvento) {
        String sql = "SELECT id_tipo_evento, nombre, descripcion FROM public.tipo_evento WHERE id_tipo_evento = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idTipoEvento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new TipoEvento(
                            rs.getInt("id_tipo_evento"),
                            rs.getString("nombre"),
                            rs.getString("descripcion")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en TipoEventoDAOImpl.buscarPorId: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
