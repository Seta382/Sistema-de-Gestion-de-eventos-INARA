package pe.edu.eventos.dao;

import pe.edu.eventos.model.Articulo;
import java.util.List;

/**
 * Interfaz de acceso a datos para la entidad Articulo.
 */
public interface ArticuloDAO {

    boolean registrar(Articulo articulo);

    Articulo buscarPorId(int idArticulo);

    List<Articulo> listarTodos();

    List<Articulo> listarActivosConProveedor();

    List<Articulo> listarPorProveedor(int idProveedor);
}
