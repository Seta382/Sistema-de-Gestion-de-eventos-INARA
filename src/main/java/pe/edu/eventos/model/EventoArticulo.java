package pe.edu.eventos.model;

import java.math.BigDecimal;

/**
 * Modelo que representa el detalle de insumos/artículos asignados a un Evento.
 * Mapea directamente con la tabla 'evento_articulo' en PostgreSQL (Supabase).
 */
public class EventoArticulo {

    private Integer idEventoArticulo;
    private Integer idEvento;
    private Integer idArticulo;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    // Campo auxiliar para vistas
    private String nombreArticulo;

    public EventoArticulo() {
        this.cantidad = 1;
        this.precioUnitario = BigDecimal.ZERO;
        this.subtotal = BigDecimal.ZERO;
    }

    public EventoArticulo(Integer idEvento, Integer idArticulo, Integer cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {
        this.idEvento = idEvento;
        this.idArticulo = idArticulo;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public Integer getIdEventoArticulo() {
        return idEventoArticulo;
    }

    public void setIdEventoArticulo(Integer idEventoArticulo) {
        this.idEventoArticulo = idEventoArticulo;
    }

    public Integer getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(Integer idEvento) {
        this.idEvento = idEvento;
    }

    public Integer getIdArticulo() {
        return idArticulo;
    }

    public void setIdArticulo(Integer idArticulo) {
        this.idArticulo = idArticulo;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public String getNombreArticulo() {
        return nombreArticulo;
    }

    public void setNombreArticulo(String nombreArticulo) {
        this.nombreArticulo = nombreArticulo;
    }

    @Override
    public String toString() {
        return "EventoArticulo{" +
                "idArticulo=" + idArticulo +
                ", cantidad=" + cantidad +
                ", subtotal=" + subtotal +
                '}';
    }
}
