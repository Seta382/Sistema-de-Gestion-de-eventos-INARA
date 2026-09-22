package pe.edu.eventos.facade;

import pe.edu.eventos.dto.UsuarioDTO;
import pe.edu.eventos.model.Usuario;
import pe.edu.eventos.service.UsuarioService;
import pe.edu.eventos.util.Constantes;

import java.util.ArrayList;
import java.util.List;

/**
 * Patrón Fachada (Facade) para simplificar la interacción entre los Servlets (Controladores)
 * y la lógica de negocio del módulo de usuarios.
 */
public class UsuarioFacade {

    private final UsuarioService usuarioService;

    public UsuarioFacade() {
        this.usuarioService = new UsuarioService();
    }

    public UsuarioFacade(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Autentica un usuario y retorna su información en un DTO.
     * @param correo correo ingresado.
     * @param password contraseña ingresada.
     * @return UsuarioDTO si las credenciales son válidas, null en caso contrario.
     */
    public UsuarioDTO login(String correo, String password) {
        Usuario usuario = usuarioService.iniciarSesion(correo, password);
        return UsuarioDTO.fromEntity(usuario);
    }

    /**
     * Registra un nuevo cliente en el sistema.
     * @param nombre nombre del cliente.
     * @param apellido apellido del cliente.
     * @param correo correo único del cliente.
     * @param password contraseña a cifrar.
     * @param telefono teléfono de contacto.
     * @return true si se registró con éxito, false si el correo ya existe o hubo error.
     */
    public boolean registrarCliente(String nombre, String apellido, String correo, String password, String telefono) {
        Usuario nuevo = new Usuario();
        nuevo.setNombre(nombre);
        nuevo.setApellido(apellido);
        nuevo.setCorreo(correo);
        nuevo.setPassword(password);
        nuevo.setTelefono(telefono);
        nuevo.setRol(Constantes.ROL_CLIENTE);

        return usuarioService.registrar(nuevo);
    }

    /**
     * Busca un usuario por correo y lo retorna como DTO.
     */
    public UsuarioDTO buscarPorCorreo(String correo) {
        Usuario usuario = usuarioService.buscarPorCorreo(correo);
        return UsuarioDTO.fromEntity(usuario);
    }

    /**
     * Lista todos los usuarios registrados convirtiéndolos a DTOs.
     */
    public List<UsuarioDTO> listarUsuarios() {
        List<UsuarioDTO> resultado = new ArrayList<>();
        for (Usuario u : usuarioService.listarTodos()) {
            resultado.add(UsuarioDTO.fromEntity(u));
        }
        return resultado;
    }
}
