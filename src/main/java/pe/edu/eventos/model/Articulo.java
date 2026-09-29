package pe.edu.eventos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Modelo que representa la entidad Articulo del sistema INARA.
 * Mapea directamente con la tabla 'articulo' en PostgreSQL (Supabase).
 */
public class Articulo {

    private Integer idArticulo;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer stock;
    private Integer idProveedor;
    private String estado;
    private LocalDateTime creadoEn;

    // Campo auxiliar para vistas
    private String nombreProveedor;

    public Articulo() {
        this.estado = "ACTIVO";
        this.stock = 0;
    }

    public Articulo(Integer idArticulo, String nombre, String descripcion, BigDecimal precio, Integer stock, Integer idProveedor, String estado, LocalDateTime creadoEn) {
        this.idArticulo = idArticulo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock != null ? stock : 0;
        this.idProveedor = idProveedor;
        this.estado = estado != null ? estado : "ACTIVO";
        this.creadoEn = creadoEn;
    }

    public Articulo(String nombre, String descripcion, BigDecimal precio, Integer stock, Integer idProveedor) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock != null ? stock : 0;
        this.idProveedor = idProveedor;
        this.estado = "ACTIVO";
    }

    public Integer getIdArticulo() {
        return idArticulo;
    }

    public void setIdArticulo(Integer idArticulo) {
        this.idArticulo = idArticulo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(Integer idProveedor) {
        this.idProveedor = idProveedor;
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

    public String getNombreProveedor() {
        return nombreProveedor;
    }

    public void setNombreProveedor(String nombreProveedor) {
        this.nombreProveedor = nombreProveedor;
    }

    @Override
    public String toString() {
        return "Articulo{" +
                "idArticulo=" + idArticulo +
                ", nombre='" + nombre + '\'' +
                ", precio=" + precio +
                ", stock=" + stock +
                ", idProveedor=" + idProveedor +
                ", estado='" + estado + '\'' +
                '}';
    }
}
