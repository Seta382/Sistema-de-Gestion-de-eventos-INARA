package pe.edu.eventos.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Arma enlaces wa.me para que el personal envíe un mensaje al cliente con un clic. */
public final class WhatsAppUtil {

    private static final int DESCRIPCION_MAXIMO = 300;

    private WhatsAppUtil() {
    }

    /**
     * Deja solo dígitos con código de país. Un celular peruano de 9 dígitos (empieza con 9) recibe el 51.
     * @return el número listo para wa.me; null si no parece un teléfono válido
     */
    public static String normalizarTelefono(String telefono) {
        if (telefono == null) return null;
        String d = telefono.replaceAll("\\D", "");
        if (d.startsWith("00")) d = d.substring(2);
        if (d.length() == 9 && d.startsWith("9")) return "51" + d;
        if (d.length() >= 10 && d.length() <= 15) return d;
        return null;
    }

    /** @return enlace https://wa.me/NUMERO?text=...; null si el teléfono no es válido */
    public static String enlace(String telefono, String mensaje) {
        String numero = normalizarTelefono(telefono);
        if (numero == null) return null;
        String texto = mensaje == null ? "" : mensaje;
        return "https://wa.me/" + numero + "?text="
                + URLEncoder.encode(texto, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /** Texto del mensaje de avance para el cliente. */
    public static String mensajeAvance(String nombreCliente, String nombreEvento, String titulo, String descripcion) {
        String primerNombre = nombreCliente == null || nombreCliente.isBlank() ? "" : nombreCliente.trim().split("\\s+")[0];
        String evento = nombreEvento == null || nombreEvento.isBlank() ? "tu evento" : "tu evento «" + nombreEvento.trim() + "»";
        StringBuilder sb = new StringBuilder();
        sb.append(primerNombre.isEmpty() ? "Hola," : "Hola " + primerNombre + ",");
        sb.append(" te compartimos un avance de ").append(evento).append(": ").append(titulo == null ? "" : titulo.trim()).append(".");
        if (descripcion != null && !descripcion.isBlank()) {
            String d = descripcion.trim().replaceAll("\\s*\\R\\s*", " ");
            if (d.length() > DESCRIPCION_MAXIMO) d = d.substring(0, DESCRIPCION_MAXIMO).trim() + "…";
            sb.append(" ").append(d);
        }
        sb.append("\n\nPuedes ver todos los detalles en tu portal INARA, sección «Mi evento». ¡Gracias por confiar en nosotros!");
        return sb.toString();
    }
}
