package pe.edu.eventos.model;

import java.math.BigDecimal;

/**
 * Servicio que ofrece la promotora (decoración, catering, DJ, etc.).
 * Se usa luego en las cotizaciones; el precio vigente se copia a detalle_cotizacion.
 */
public class Servicio {

    private Integer idServicio;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private String estado;

    public Servicio() {
        this.precio = BigDecimal.ZERO;
        this.estado = "ACTIVO";
    }

    public Servicio(Integer idServicio, String nombre, String descripcion, BigDecimal precio, String estado) {
        this.idServicio = idServicio;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.estado = estado;
    }

    public Servicio(String nombre, String descripcion, BigDecimal precio) {
        this(null, nombre, descripcion, precio, "ACTIVO");
    }

    public Integer getIdServicio() { return idServicio; }
    public void setIdServicio(Integer idServicio) { this.idServicio = idServicio; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public boolean isActivo() { return "ACTIVO".equalsIgnoreCase(estado); }

    @Override
    public String toString() {
        return "Servicio{id=" + idServicio + ", nombre='" + nombre + "', precio=" + precio + ", estado='" + estado + "'}";
    }
}
