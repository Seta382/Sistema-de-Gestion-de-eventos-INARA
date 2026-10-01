package pe.edu.eventos.service;

import pe.edu.eventos.dao.EmpleadoDAO;
import pe.edu.eventos.dao.UsuarioDAO;
import pe.edu.eventos.dao.impl.EmpleadoDAOImpl;
import pe.edu.eventos.dao.impl.UsuarioDAOImpl;
import pe.edu.eventos.model.Empleado;
import pe.edu.eventos.model.Usuario;
import pe.edu.eventos.util.Constantes;
import pe.edu.eventos.util.PasswordUtil;

import java.util.List;

/**
 * Capa de servicio para el alta, edición y baja de personal administrativo/operativo.
 */
public class EmpleadoService {

    private final EmpleadoDAO empleadoDAO;
    private final UsuarioDAO usuarioDAO;

    public EmpleadoService() {
        this.empleadoDAO = new EmpleadoDAOImpl();
        this.usuarioDAO = new UsuarioDAOImpl();
    }

    /**
     * Registra un nuevo empleado: valida campos, hashea la contraseña y
     * delega al DAO, que inserta usuario + empleado en una sola transacción.
     * @return null si se registró con éxito; un mensaje de error si no.
     */
    public String registrar(Usuario usuario, Empleado empleado) {
        if (usuario == null || usuario.getCorreo() == null || usuario.getCorreo().trim().isEmpty()
                || usuario.getPassword() == null || usuario.getPassword().trim().isEmpty()
                || usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()
                || usuario.getApellido() == null || usuario.getApellido().trim().isEmpty()) {
            return Constantes.MSG_CAMPOS_OBLIGATORIOS;
        }

        String correoLimpio = usuario.getCorreo().trim().toLowerCase();
        if (usuarioDAO.buscarPorCorreo(correoLimpio) != null) {
            return Constantes.MSG_CORREO_DUPLICADO;
        }

        usuario.setCorreo(correoLimpio);
        usuario.setRol(Constantes.ROL_EMPLEADO);
        usuario.setPassword(PasswordUtil.hashPassword(usuario.getPassword()));

        boolean ok = empleadoDAO.registrarEmpleado(usuario, empleado);
        return ok ? null : "No se pudo registrar el personal. Intenta nuevamente.";
    }

    /**
     * Actualiza nombre/apellido/telefono (usuario) + dni/cargo/area (empleado).
     * El correo no se modifica desde aquí a propósito.
     * @return null si se actualizó con éxito; un mensaje de error si no.
     */
    public String actualizar(Usuario usuario, Empleado empleado) {
        if (usuario == null || usuario.getId() == null
                || usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()
                || usuario.getApellido() == null || usuario.getApellido().trim().isEmpty()) {
            return Constantes.MSG_CAMPOS_OBLIGATORIOS;
        }

        boolean ok = empleadoDAO.actualizarEmpleado(usuario, empleado);
        return ok ? null : "No se pudo actualizar el personal. Intenta nuevamente.";
    }

    public boolean darDeBaja(int idUsuario) {
        return empleadoDAO.cambiarEstado(idUsuario, "INACTIVO");
    }

    public boolean reactivar(int idUsuario) {
        return empleadoDAO.cambiarEstado(idUsuario, "ACTIVO");
    }

    public Empleado buscarPorIdUsuario(int idUsuario) {
        return empleadoDAO.buscarPorIdUsuario(idUsuario);
    }

    public Empleado buscarPorId(int idEmpleado) {
        return empleadoDAO.buscarPorId(idEmpleado);
    }

    public List<Empleado> listarTodos() {
        return empleadoDAO.listarTodos();
    }

    public List<Empleado> listarPersonalOperativo() {
        return empleadoDAO.listarPersonalOperativo();
    }
}
