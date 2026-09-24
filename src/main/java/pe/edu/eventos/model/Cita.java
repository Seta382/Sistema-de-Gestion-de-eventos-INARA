package pe.edu.eventos.model;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;


public class Cita {

    private Integer idCita;
    private Integer idCliente;
    private Integer idEmpleado; // nullable: se asigna después de PENDIENTE
    private Date fecha;
    private Time hora;
    private String modalidad; // PRESENCIAL, VIRTUAL
    private String lugar;
    private String motivo;
    private String estado; // PENDIENTE, CONFIRMADA, REPROGRAMADA, CANCELADA, FINALIZADA
    private Timestamp creadoEn;

    public Cita() {
    }

    public Cita(Integer idCliente, Date fecha, Time hora, String modalidad, String lugar, String motivo) {
        this.idCliente = idCliente;
        this.fecha = fecha;
        this.hora = hora;
        this.modalidad = modalidad;
        this.lugar = lugar;
        this.motivo = motivo;
        this.estado = "PENDIENTE";
    }

    public Integer getIdCita() {
        return idCita;
    }

    public void setIdCita(Integer idCita) {
        this.idCita = idCita;
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

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Time getHora() {
        return hora;
    }

    public void setHora(Time hora) {
        this.hora = hora;
    }

    public String getModalidad() {
        return modalidad;
    }

    public void setModalidad(String modalidad) {
        this.modalidad = modalidad;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Timestamp getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(Timestamp creadoEn) {
        this.creadoEn = creadoEn;
    }
}
