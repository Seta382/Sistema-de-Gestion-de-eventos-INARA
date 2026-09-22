package pe.edu.eventos.util;

/**
 * Constantes globales de la aplicación INARA.
 */
public final class Constantes {

    private Constantes() {
    }

    // Roles del sistema
    public static final String ROL_ADMIN = "ADMIN";
    public static final String ROL_EMPLEADO = "EMPLEADO";
    public static final String ROL_CLIENTE = "CLIENTE";

    // Atributos de sesión
    public static final String SESION_USUARIO = "usuarioLogueado";

    // Mensajes de respuesta
    public static final String MSG_LOGIN_ERROR = "Correo o contraseña incorrectos. Por favor verifica tus datos.";
    public static final String MSG_CORREO_DUPLICADO = "El correo electrónico ya se encuentra registrado.";
    public static final String MSG_REGISTRO_EXITOSO = "¡Registro exitoso! Por favor inicia sesión.";
    public static final String MSG_CAMPOS_OBLIGATORIOS = "Todos los campos obligatorios deben ser completados.";
}
