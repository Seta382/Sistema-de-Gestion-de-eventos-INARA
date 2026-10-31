package pe.edu.eventos.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Acuerdo contractual de un evento: las condiciones que el cliente acepta tras aprobar una cotización. */
public class Acuerdo {

    public static final String PENDIENTE = "PENDIENTE";
    public static final String ACEPTADO = "ACEPTADO";
    public static final String CANCELADO = "CANCELADO";

    private Integer idAcuerdo;
    private Integer idEvento;
    private String condiciones;
    private String estado = PENDIENTE;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaConfirmacion;

    // Solo para mostrar (JOIN con evento, cliente y usuario)
    private Integer idCliente;
    private String nombreEvento;
    private String nombreCliente;
    private LocalDate fechaEvento;
    private String estadoEvento;

    public Acuerdo() {
    }

    public Acuerdo(Integer idEvento, String condiciones) {
        this.idEvento = idEvento;
        this.condiciones = condiciones;
    }

    public boolean isPendiente() { return PENDIENTE.equals(estado); }
    public boolean isAceptado() { return ACEPTADO.equals(estado); }
    /** PENDIENTE o ACEPTADO: el que cuenta como acuerdo del evento. */
    public boolean isVigente() { return isPendiente() || isAceptado(); }

    public Integer getIdAcuerdo() { return idAcuerdo; }
    public void setIdAcuerdo(Integer idAcuerdo) { this.idAcuerdo = idAcuerdo; }

    public Integer getIdEvento() { return idEvento; }
    public void setIdEvento(Integer idEvento) { this.idEvento = idEvento; }

    public String getCondiciones() { return condiciones; }
    public void setCondiciones(String condiciones) { this.condiciones = condiciones; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public LocalDateTime getFechaConfirmacion() { return fechaConfirmacion; }
    public void setFechaConfirmacion(LocalDateTime fechaConfirmacion) { this.fechaConfirmacion = fechaConfirmacion; }

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Fecha de registro lista para mostrar (dd/MM/yyyy HH:mm); vacío si no hay. */
    public String getFechaRegistroTxt() { return fechaRegistro == null ? "" : fechaRegistro.format(FORMATO); }

    /** Fecha de confirmación lista para mostrar (dd/MM/yyyy HH:mm); vacío si no hay. */
    public String getFechaConfirmacionTxt() { return fechaConfirmacion == null ? "" : fechaConfirmacion.format(FORMATO); }

    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }

    public String getNombreEvento() { return nombreEvento; }
    public void setNombreEvento(String nombreEvento) { this.nombreEvento = nombreEvento; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public LocalDate getFechaEvento() { return fechaEvento; }
    public void setFechaEvento(LocalDate fechaEvento) { this.fechaEvento = fechaEvento; }

    public String getEstadoEvento() { return estadoEvento; }
    public void setEstadoEvento(String estadoEvento) { this.estadoEvento = estadoEvento; }
}
