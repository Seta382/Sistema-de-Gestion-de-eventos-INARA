package pe.edu.eventos.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Regla común para eventos y citas: no se pueden registrar en el pasado.
 * Usa la hora de Lima (no la del servidor, que en la nube suele estar en UTC).
 */
public final class ReglasFecha {

    public static final ZoneId ZONA_NEGOCIO = ZoneId.of("America/Lima");

    private ReglasFecha() {
    }

    /**
     * @param sujeto texto que completa el mensaje, por ejemplo "del evento" o "de la cita"
     * @return null si la fecha/hora es válida; si no, el mensaje de error para el usuario
     */
    public static String validarNoPasada(LocalDate fecha, LocalTime hora, String sujeto) {
        return validarNoPasada(fecha, hora, sujeto, LocalDate.now(ZONA_NEGOCIO), LocalTime.now(ZONA_NEGOCIO));
    }

    /** Igual que la anterior, con "hoy" y "ahora" explícitos (para poder probarla). */
    public static String validarNoPasada(LocalDate fecha, LocalTime hora, String sujeto,
                                         LocalDate hoy, LocalTime ahora) {
        if (fecha == null) {
            return "La fecha " + sujeto + " es obligatoria.";
        }
        if (fecha.isBefore(hoy)) {
            return "La fecha " + sujeto + " no puede ser anterior a hoy.";
        }
        if (fecha.equals(hoy) && hora != null && hora.isBefore(ahora)) {
            return "La hora " + sujeto + " ya pasó. Elige una hora posterior o una fecha futura.";
        }
        return null;
    }
}
