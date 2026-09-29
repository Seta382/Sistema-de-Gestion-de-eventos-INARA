package pe.edu.eventos.dao;

import pe.edu.eventos.model.Empleado;
import pe.edu.eventos.model.Usuario;
import java.util.List;

/**
 * Interfaz de acceso a datos para la entidad Empleado.
 */
public interface EmpleadoDAO {

    boolean registrarEmpleado(Usuario usuario, Empleado empleado);

    Empleado buscarPorIdUsuario(int idUsuario);

    Empleado buscarPorId(int idEmpleado);

    List<Empleado> listarTodos();

    List<Empleado> listarPersonalOperativo();
}
