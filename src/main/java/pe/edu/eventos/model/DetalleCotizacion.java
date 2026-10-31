package pe.edu.eventos.model;

import java.math.BigDecimal;

/** Una línea de la cotización: un servicio, su cantidad y el precio vigente al momento de cotizar. */
public class DetalleCotizacion {

    private Integer idDetalle;
    private Integer idCotizacion;
    private Integer idServicio;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    // Solo para mostrar (viene del JOIN con servicio)
    private String nombreServicio;

    public DetalleCotizacion() {
    }

    public DetalleCotizacion(Integer idServicio, Integer cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {
        this.idServicio = idServicio;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public Integer getIdDetalle() { return idDetalle; }
    public void setIdDetalle(Integer idDetalle) { this.idDetalle = idDetalle; }

    public Integer getIdCotizacion() { return idCotizacion; }
    public void setIdCotizacion(Integer idCotizacion) { this.idCotizacion = idCotizacion; }

    public Integer getIdServicio() { return idServicio; }
    public void setIdServicio(Integer idServicio) { this.idServicio = idServicio; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public String getNombreServicio() { return nombreServicio; }
    public void setNombreServicio(String nombreServicio) { this.nombreServicio = nombreServicio; }
}
