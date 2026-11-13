package pe.edu.eventos.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilidad para el hashing y verificación segura de contraseñas.
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    /**
     * Genera un hash SHA-256 en formato hexadecimal para una contraseña en texto plano.
     * @param password contraseña a hashear.
     * @return hash en formato hexadecimal.
     */
    public static String hashPassword(String password) {
        if (password == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algoritmo SHA-256 no disponible", e);
        }
    }

    /**
     * Verifica si una contraseña coincide con el hash SHA-256 guardado.
     * Ya NO se aceptan contraseñas en texto plano: si en la base queda alguna sin hashear
     * (por ejemplo un usuario de prueba antiguo), hay que convertirla con database/02_hashear_passwords.sql.
     * Tampoco sirve escribir el hash guardado como si fuera la contraseña.
     * @param plainPassword contraseña en texto plano introducida por el usuario.
     * @param storedPassword hash SHA-256 (hexadecimal) almacenado en la base de datos.
     * @return true si coincide, false en caso contrario.
     */
    public static boolean verificarPassword(String plainPassword, String storedPassword) {
        if (plainPassword == null || storedPassword == null) {
            return false;
        }
        String hashed = hashPassword(plainPassword);
        // Comparación en tiempo constante (no revela cuántos caracteres coinciden)
        return MessageDigest.isEqual(
                hashed.getBytes(StandardCharsets.UTF_8),
                storedPassword.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
    }
}
