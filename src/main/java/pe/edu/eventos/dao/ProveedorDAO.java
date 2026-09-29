package pe.edu.eventos.dao;

import pe.edu.eventos.model.Proveedor;
import java.util.List;

/**
 * Interfaz de acceso a datos para la entidad Proveedor.
 */
public interface ProveedorDAO {

    boolean registrar(Proveedor proveedor);

    Proveedor buscarPorRuc(String ruc);

    Proveedor buscarPorId(int idProveedor);

    List<Proveedor> listarTodos();

    List<Proveedor> listarPorIds(List<Integer> ids);
}
