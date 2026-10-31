package pe.edu.eventos.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Avance registrado por el personal sobre la preparación de un evento. */
public class Seguimiento {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Integer idSeguimiento;
    private Integer idEvento;
    private String titulo;
    private String descripcion;
    private LocalDateTime fechaRegistro;

    public Seguimiento() {
    }

    public Seguimiento(Integer idEvento, String titulo, String descripcion) {
        this.idEvento = idEvento;
        this.titulo = titulo;
        this.descripcion = descripcion;
    }

    public Integer getIdSeguimiento() { return idSeguimiento; }
    public void setIdSeguimiento(Integer idSeguimiento) { this.idSeguimiento = idSeguimiento; }

    public Integer getIdEvento() { return idEvento; }
    public void setIdEvento(Integer idEvento) { this.idEvento = idEvento; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getFechaRegistroTxt() { return fechaRegistro == null ? "" : fechaRegistro.format(FORMATO); }
}
