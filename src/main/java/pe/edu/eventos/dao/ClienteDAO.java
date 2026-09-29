package pe.edu.eventos.dao;


import pe.edu.eventos.model.Cliente;
import java.util.List;

public interface ClienteDAO {
    Integer crear(int idUsuario, String dni, String direccion);
    Integer buscarIdPorUsuario(int idUsuario);
    List<Cliente> listarClientesConUsuario();
}
