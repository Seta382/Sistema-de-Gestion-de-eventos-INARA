package pe.edu.eventos.dao;


public interface ClienteDAO {
    Integer crear(int idUsuario, String dni, String direccion);
    Integer buscarIdPorUsuario(int idUsuario);
}
