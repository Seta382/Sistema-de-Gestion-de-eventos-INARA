package pe.edu.eventos.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Cuánto se acordó, cuánto se pagó y cuánto falta en un evento. */
public class ResumenPago {

    private final BigDecimal total;
    private final BigDecimal pagado;
    private final BigDecimal porConfirmar;

    public ResumenPago(BigDecimal total, BigDecimal pagado, BigDecimal porConfirmar) {
        this.total = total;
        this.pagado = pagado;
        this.porConfirmar = porConfirmar;
    }

    /** total = null cuando el evento aún no tiene cotización aceptada. */
    public static ResumenPago calcular(BigDecimal total, List<Pago> pagos) {
        BigDecimal pagado = BigDecimal.ZERO;
        BigDecimal porConfirmar = BigDecimal.ZERO;
        for (Pago p : pagos) {
            if (p.isCompletado()) {
                pagado = pagado.add(p.getMonto());
            } else if (p.isPendiente()) {
                porConfirmar = porConfirmar.add(p.getMonto());
            }
        }
        return new ResumenPago(total, pagado, porConfirmar);
    }

    /** Total acordado (cotización aceptada); null si todavía no hay. */
    public BigDecimal getTotal() { return total; }

    /** Suma de los pagos COMPLETADO. */
    public BigDecimal getPagado() { return pagado; }

    /** Suma de los pagos PENDIENTE (aún no verificados). */
    public BigDecimal getPorConfirmar() { return porConfirmar; }

    public boolean isTieneTotal() { return total != null; }

    /** Lo que falta pagar; nunca negativo. Cero si no hay total. */
    public BigDecimal getSaldo() {
        if (total == null) return BigDecimal.ZERO;
        BigDecimal s = total.subtract(pagado);
        return s.signum() < 0 ? BigDecimal.ZERO : s;
    }

    public boolean isPagadoCompleto() { return total != null && pagado.compareTo(total) >= 0; }

    /** Porcentaje pagado (0 a 100, entero). */
    public int getPorcentaje() {
        if (total == null || total.signum() <= 0) return 0;
        int p = pagado.multiply(BigDecimal.valueOf(100)).divide(total, 0, RoundingMode.DOWN).intValue();
        return Math.min(p, 100);
    }
}
