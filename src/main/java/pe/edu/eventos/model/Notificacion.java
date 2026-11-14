package pe.edu.eventos.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Aviso para un usuario (cliente o personal) sobre algo que pasó en un evento. */
public class Notificacion {

    public static final String COTIZACION = "COTIZACION";
    public static final String ACUERDO = "ACUERDO";
    public static final String PAGO = "PAGO";
    public static final String SEGUIMIENTO = "SEGUIMIENTO";
    public static final String GENERAL = "GENERAL";

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Integer idNotificacion;
    private Integer idUsuario;
    private Integer idEvento;
    private String tipo = GENERAL;
    private String mensaje;
    private boolean leida;
    private LocalDateTime fecha;

    public Integer getIdNotificacion() { return idNotificacion; }
    public void setIdNotificacion(Integer idNotificacion) { this.idNotificacion = idNotificacion; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    /** Evento relacionado; null si ya no existe. */
    public Integer getIdEvento() { return idEvento; }
    public void setIdEvento(Integer idEvento) { this.idEvento = idEvento; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo == null ? GENERAL : tipo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getFechaTxt() { return fecha == null ? "" : fecha.format(FORMATO); }
}
