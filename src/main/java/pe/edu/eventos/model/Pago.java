package pe.edu.eventos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/** Pago de un evento. Solo los COMPLETADO cuentan para el saldo. */
public class Pago {

    public static final String PENDIENTE = "PENDIENTE";
    public static final String COMPLETADO = "COMPLETADO";
    public static final String RECHAZADO = "RECHAZADO";

    public static final List<String> METODOS =
            Arrays.asList("TRANSFERENCIA", "EFECTIVO", "YAPE", "PLIN", "TARJETA");

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Integer idPago;
    private Integer idEvento;
    private BigDecimal monto;
    private LocalDateTime fechaPago;
    private String metodoPago;
    private String estado = COMPLETADO;
    private String comprobanteUrl;

    // Solo para mostrar (JOIN con evento, cliente y usuario)
    private Integer idCliente;
    private String nombreEvento;
    private String nombreCliente;
    private String estadoEvento;

    public Pago() {
    }

    public Pago(Integer idEvento, BigDecimal monto, String metodoPago, String estado, String comprobanteUrl) {
        this.idEvento = idEvento;
        this.monto = monto;
        this.metodoPago = metodoPago;
        this.estado = estado;
        this.comprobanteUrl = comprobanteUrl;
    }

    public boolean isPendiente() { return PENDIENTE.equals(estado); }
    public boolean isCompletado() { return COMPLETADO.equals(estado); }

    public Integer getIdPago() { return idPago; }
    public void setIdPago(Integer idPago) { this.idPago = idPago; }

    public Integer getIdEvento() { return idEvento; }
    public void setIdEvento(Integer idEvento) { this.idEvento = idEvento; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    /** Fecha del pago; null = la base de datos pone la fecha actual al registrar. */
    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }

    public String getFechaPagoTxt() { return fechaPago == null ? "" : fechaPago.format(FORMATO); }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getComprobanteUrl() { return comprobanteUrl; }
    public void setComprobanteUrl(String comprobanteUrl) { this.comprobanteUrl = comprobanteUrl; }

    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }

    public String getNombreEvento() { return nombreEvento; }
    public void setNombreEvento(String nombreEvento) { this.nombreEvento = nombreEvento; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getEstadoEvento() { return estadoEvento; }
    public void setEstadoEvento(String estadoEvento) { this.estadoEvento = estadoEvento; }
}
