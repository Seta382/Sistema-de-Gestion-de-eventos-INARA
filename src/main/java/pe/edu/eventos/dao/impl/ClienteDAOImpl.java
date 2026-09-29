package pe.edu.eventos.dao.impl;

import pe.edu.eventos.dao.ClienteDAO;
import pe.edu.eventos.model.Cliente;
import pe.edu.eventos.util.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class ClienteDAOImpl implements ClienteDAO {

    @Override
    public Integer crear(int idUsuario, String dni, String direccion) {
        String sql = "INSERT INTO public.cliente (id_usuario, dni, direccion) VALUES (?, ?, ?)";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idUsuario);
            ps.setString(2, dni);
            ps.setString(3, direccion);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Integer buscarIdPorUsuario(int idUsuario) {
        String sql = "SELECT id_cliente FROM public.cliente WHERE id_usuario = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_cliente");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Cliente> listarClientesConUsuario() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT c.id_cliente, c.id_usuario, c.dni, c.direccion, " +
                "u.nombre, u.apellido, u.correo, u.telefono " +
                "FROM public.cliente c " +
                "JOIN public.usuario u ON c.id_usuario = u.id " +
                "ORDER BY u.nombre ASC, u.apellido ASC";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Cliente cli = new Cliente();
                cli.setIdCliente(rs.getInt("id_cliente"));
                cli.setId(rs.getInt("id_usuario"));
                cli.setDni(rs.getString("dni"));
                cli.setDireccion(rs.getString("direccion"));
                cli.setNombre(rs.getString("nombre"));
                cli.setApellido(rs.getString("apellido"));
                cli.setCorreo(rs.getString("correo"));
                cli.setTelefono(rs.getString("telefono"));
                lista.add(cli);
            }
        } catch (SQLException e) {
            System.err.println("Error en ClienteDAOImpl.listarClientesConUsuario: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }
}