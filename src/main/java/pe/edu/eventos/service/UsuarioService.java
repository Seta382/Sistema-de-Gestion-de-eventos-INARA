package pe.edu.eventos.service;

import pe.edu.eventos.dao.UsuarioDAO;
import pe.edu.eventos.dao.impl.UsuarioDAOImpl;
import pe.edu.eventos.model.Usuario;
import pe.edu.eventos.util.Constantes;
import pe.edu.eventos.util.PasswordUtil;

import java.util.List;

/**
 * Capa de servicio para la gestión de usuarios y reglas de negocio.
 */
public class UsuarioService {

    private final UsuarioDAO usuarioDAO;

    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAOImpl();
    }

    public UsuarioService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    /**
     * Autentica las credenciales de un usuario.
     * @param correo correo ingresado.
     * @param password contraseña ingresada en texto plano.
     * @return Usuario autenticado o null si es inválido.
     */
    public Usuario iniciarSesion(String correo, String password) {
        if (correo == null || correo.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return null;
        }
        return usuarioDAO.autenticar(correo.trim(), password);
    }

    /**
     * Registra un nuevo usuario con rol CLIENTE, asegurando contraseña cifrada
     * y validando que el correo no esté duplicado.
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

        return usuarioDAO.registrar(usuario);
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
