package pe.edu.eventos.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Apoyo para los buscadores con ILIKE: separa el texto en palabras y arma
 * patrones seguros (los comodines % y _ escritos por el usuario se buscan tal cual).
 */
public final class BusquedaUtil {

    private static final int MAX_PALABRAS = 6;
    private static final int MAX_LARGO_PALABRA = 50;
    private static final int MAX_LARGO_TEXTO = 100;

    private BusquedaUtil() {
    }

    /** Texto del buscador listo para usar: nunca null, sin espacios sobrantes y con tope de longitud. */
    public static String normalizar(String texto) {
        if (texto == null) return "";
        String t = texto.trim();
        return t.length() > MAX_LARGO_TEXTO ? t.substring(0, MAX_LARGO_TEXTO) : t;
    }

    /** Separa el texto en palabras (máx. 6, de máx. 50 caracteres cada una). */
    public static List<String> tokenizar(String texto) {
        List<String> palabras = new ArrayList<>();
        if (texto == null || texto.trim().isEmpty()) {
            return palabras;
        }
        for (String p : texto.trim().split("\\s+")) {
            if (p.isEmpty()) continue;
            palabras.add(p.length() > MAX_LARGO_PALABRA ? p.substring(0, MAX_LARGO_PALABRA) : p);
            if (palabras.size() == MAX_PALABRAS) break;
        }
        return palabras;
    }

    /** Escapa \, % y _ (el escape por defecto de LIKE en PostgreSQL es la barra invertida). */
    public static String escaparLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    /** Patrón "contiene" para ILIKE: %palabra% con los comodines escapados. */
    public static String patronContiene(String palabra) {
        return "%" + escaparLike(palabra) + "%";
    }

    // Vocales con tilde (y la ü) y su equivalente sin tilde. La ñ se conserva ("año" y "ano" son palabras
    // distintas), pero la Ñ mayúscula se lleva a minúscula para que la búsqueda no dependa de la
    // configuración regional de la base de datos (ILIKE solo pliega mayúsculas ASCII en algunas).
    private static final String CON_TILDE = "áéíóúüÁÉÍÓÚÜÑ";
    private static final String SIN_TILDE = "aeiouuAEIOUUñ";

    /**
     * Envuelve una columna o expresión SQL para compararla sin tildes (usa translate, de PostgreSQL).
     * Se usa junto con patronContieneSinTildes: la columna y el patrón deben normalizarse igual.
     */
    public static String sinTildesSql(String expresion) {
        return "translate(" + expresion + ", '" + CON_TILDE + "', '" + SIN_TILDE + "')";
    }

    /** Quita las tildes de las vocales (misma tabla que sinTildesSql). */
    public static String quitarTildes(String texto) {
        StringBuilder sb = new StringBuilder(texto.length());
        for (char c : texto.toCharArray()) {
            int i = CON_TILDE.indexOf(c);
            sb.append(i >= 0 ? SIN_TILDE.charAt(i) : c);
        }
        return sb.toString();
    }

    /** Patrón "contiene" sin tildes, para usar contra sinTildesSql(columna). */
    public static String patronContieneSinTildes(String palabra) {
        return "%" + escaparLike(quitarTildes(palabra)) + "%";
    }
}
