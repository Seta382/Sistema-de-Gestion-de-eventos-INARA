package pe.edu.eventos.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Modelo que representa la cabecera de un Evento del sistema INARA.
 * Mapea directamente con la tabla 'evento' en PostgreSQL (Supabase).
 */
public class Evento {

    private Integer idEvento;
    private String nombre;
    private Integer idCliente;
    private Integer idEmpleado;
    private Integer idTipoEvento;
    private Integer idCita;
    private LocalDate fechaEvento;
    private LocalTime horaEvento;
    private String lugar;
    private Integer numInvitados;
    private BigDecimal presupuesto;
    private String descripcion;
    private String estado;
    private LocalDateTime creadoEn;

    // Campos auxiliares para renderizar en vistas
    private String nombreCliente;
    private String nombreCoordinador;
    private String tipoCelebracion;

    public Evento() {
        this.estado = "CONFIRMADO";
        this.presupuesto = BigDecimal.ZERO;
        this.numInvitados = 0;
    }

    public Integer getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(Integer idEvento) {
        this.idEvento = idEvento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public Integer getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Integer idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public Integer getIdTipoEvento() {
        return idTipoEvento;
    }

    public void setIdTipoEvento(Integer idTipoEvento) {
        this.idTipoEvento = idTipoEvento;
    }

    public Integer getIdCita() {
        return idCita;
    }

    public void setIdCita(Integer idCita) {
        this.idCita = idCita;
    }

    public LocalDate getFechaEvento() {
        return fechaEvento;
    }

    public void setFechaEvento(LocalDate fechaEvento) {
        this.fechaEvento = fechaEvento;
    }

    public LocalTime getHoraEvento() {
        return horaEvento;
    }

    public void setHoraEvento(LocalTime horaEvento) {
        this.horaEvento = horaEvento;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public Integer getNumInvitados() {
        return numInvitados;
    }

    public void setNumInvitados(Integer numInvitados) {
        this.numInvitados = numInvitados;
    }

    public BigDecimal getPresupuesto() {
        return presupuesto;
    }

    public void setPresupuesto(BigDecimal presupuesto) {
        this.presupuesto = presupuesto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getNombreCoordinador() {
        return nombreCoordinador;
    }

    public void setNombreCoordinador(String nombreCoordinador) {
        this.nombreCoordinador = nombreCoordinador;
    }

    public String getTipoCelebracion() {
        return tipoCelebracion;
    }

    public void setTipoCelebracion(String tipoCelebracion) {
        this.tipoCelebracion = tipoCelebracion;
    }

    @Override
    public String toString() {
        return "Evento{" +
                "idEvento=" + idEvento +
                ", nombre='" + nombre + '\'' +
                ", fechaEvento=" + fechaEvento +
                ", estado='" + estado + '\'' +
                '}';
    }
}
