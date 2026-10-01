package pe.edu.eventos.service;

import pe.edu.eventos.dao.UsuarioDAO;
import pe.edu.eventos.dao.ClienteDAO;
import pe.edu.eventos.dao.impl.UsuarioDAOImpl;
import pe.edu.eventos.dao.impl.ClienteDAOImpl;
import pe.edu.eventos.model.Usuario;
import pe.edu.eventos.model.Cliente;
import pe.edu.eventos.util.Constantes;
import pe.edu.eventos.util.PasswordUtil;

import java.util.List;

/**
 * Capa de servicio para la gestión de usuarios y reglas de negocio.
 */
public class UsuarioService {

    private final UsuarioDAO usuarioDAO;
    private final ClienteDAO clienteDAO;

    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAOImpl();
        this.clienteDAO = new ClienteDAOImpl();
    }

    public UsuarioService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
        this.clienteDAO = new ClienteDAOImpl();
    }

    /**
     * Cambios en iniciarSesion, ahora se busca por correo
     */
    public Usuario iniciarSesion(String correo, String password) {
        Usuario usuario = usuarioDAO.buscarPorCorreo(correo);
        if (usuario != null
                && PasswordUtil.verificarPassword(password, usuario.getPassword())
                && !"INACTIVO".equalsIgnoreCase(usuario.getEstado())) {
            return usuario;
        }
        return null;
    }

    /**
     * Registra un nuevo usuario. Si es un Cliente (rol CLIENTE), además crea
     * su fila en la tabla "cliente" (dni, direccion), necesaria para que
     * CitaServlet pueda resolver su id_cliente más adelante.
     *
     * @param usuario usuario a registrar.
     * @return true si se registró con éxito, false si el correo ya existe o hay error.
     */
    public boolean registrar(Usuario usuario) {
        if (usuario == null || usuario.getCorreo() == null || usuario.getPassword() == null) {
            return false;
        }

        String correoLimpio = usuario.getCorreo().trim();
        if (usuarioDAO.buscarPorCorreo(correoLimpio) != null) {
            return false; // Correo duplicado
        }

        // Asignar rol por defecto si no viene especificado
        if (usuario.getRol() == null || usuario.getRol().trim().isEmpty()) {
            usuario.setRol(Constantes.ROL_CLIENTE);
        }

        // Encriptar contraseña con SHA-256
        usuario.setPassword(PasswordUtil.hashPassword(usuario.getPassword()));
        usuario.setCorreo(correoLimpio);

        boolean creado = usuarioDAO.registrar(usuario); // debe dejar usuario.getId() seteado
        if (!creado) {
            return false;
        }

        if (usuario instanceof Cliente cliente) {
            Integer idCliente = clienteDAO.crear(usuario.getId(), cliente.getDni(), cliente.getDireccion());
            if (idCliente == null) {
                return false; // el usuario quedó creado pero la fila cliente falló; revisar logs
            }
            cliente.setIdCliente(idCliente);
        }

        return true;
    }

    public Usuario buscarPorCorreo(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            return null;
        }
        return usuarioDAO.buscarPorCorreo(correo.trim());
    }

    public Usuario buscarPorId(int id) {
        return usuarioDAO.buscarPorId(id);
    }

    public List<Usuario> listarTodos() {
        return usuarioDAO.listarTodos();
    }
}
