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
}
