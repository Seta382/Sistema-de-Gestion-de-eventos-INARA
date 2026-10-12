package pe.edu.eventos.dao;

import pe.edu.eventos.model.Articulo;
import java.util.List;

/**
 * Interfaz de acceso a datos para la entidad Articulo.
 */
public interface ArticuloDAO {

    /** Un insumo activo con stock menor o igual a este valor se considera "stock bajo". */
    int UMBRAL_STOCK_BAJO = 5;

    boolean registrar(Articulo articulo);

    Articulo buscarPorId(int idArticulo);

    List<Articulo> listarTodos();

    List<Articulo> listarActivosConProveedor();

    List<Articulo> listarPorProveedor(int idProveedor);

    List<Articulo> buscar(String texto, String filtro);

    boolean cambiarEstado(int idArticulo, String nuevoEstado);

    boolean actualizar(Articulo articulo, Integer stockEsperado);
}
