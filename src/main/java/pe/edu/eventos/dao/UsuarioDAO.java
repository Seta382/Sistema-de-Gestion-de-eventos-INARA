
import pe.edu.eventos.model.Usuario;
import java.util.List;

/**
 * Interfaz para las operaciones de acceso a datos de la entidad Usuario.
 */
public interface UsuarioDAO {

    /**
     * Autentica un usuario verificando su correo y contraseña.
     * @param correo correo electrónico del usuario.
     * @param password contraseña del usuario.
     * @return Usuario autenticado o null si las credenciales son inválidas.
     */
    Usuario autenticar(String correo, String password);

    /**
     * Busca un usuario por su correo electrónico.
     * @param correo correo a buscar.
     * @return Usuario encontrado o null.
     */
    Usuario buscarPorCorreo(String correo);

    /**
     * Busca un usuario por su identificador único.
     * @param id identificador del usuario.
     * @return Usuario encontrado o null.
     */
    Usuario buscarPorId(int id);

    /**
     * Registra un nuevo usuario en la base de datos.
     * @param usuario datos del usuario a insertar.
     * @return true si se registró con éxito, false en caso contrario.
     */
    boolean registrar(Usuario usuario);

    /**
     * Obtiene el listado de todos los usuarios registrados.
     * @return Lista de usuarios.
     */
    List<Usuario> listarTodos();
}
