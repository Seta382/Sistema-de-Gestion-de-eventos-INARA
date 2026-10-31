package pe.edu.eventos.service;

import pe.edu.eventos.dao.PagoDAO;
import pe.edu.eventos.dao.impl.PagoDAOImpl;
import pe.edu.eventos.dao.EventoDAO;
import pe.edu.eventos.dao.impl.EventoDAOImpl;
import pe.edu.eventos.model.Evento;
import pe.edu.eventos.model.Pago;
import pe.edu.eventos.model.ResumenPago;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

/**
 * Reglas de negocio de los pagos.
 * - Solo se registran pagos cuando el evento está CONFIRMADO, EN_PROCESO o FINALIZADO
 *   (o sea, con cotización aceptada y acuerdo firmado).
 * - Un pago COMPLETADO no puede superar el saldo pendiente.
 * - PENDIENTE (aún sin verificar) pasa a COMPLETADO o RECHAZADO; un COMPLETADO se puede anular (RECHAZADO).
 */
public class PagoService {

    private static final BigDecimal MONTO_MAXIMO = new BigDecimal("99999999.99");
    private static final int COMPROBANTE_MAXIMO = 255;

    private final PagoDAO pagoDAO;
    private final EventoDAO eventoDAO;

    public PagoService() {
        this(new PagoDAOImpl(), new EventoDAOImpl());
    }

    public PagoService(PagoDAO pagoDAO, EventoDAO eventoDAO) {
        this.pagoDAO = pagoDAO;
        this.eventoDAO = eventoDAO;
    }

    /**
     * @param montoTxt texto del formulario (acepta coma o punto decimal)
     * @return null si se registró; un mensaje de error si no
     */
    public String registrar(int idEvento, String montoTxt, String metodo, String estado, String comprobante) {
        Evento evento = eventoDAO.buscarPorId(idEvento);
        if (evento == null) return "El evento indicado no existe.";
        String motivo = motivoNoAdmitePagos(evento);
        if (motivo != null) return motivo;

        BigDecimal monto = parsearMonto(montoTxt);
        if (monto == null) return "Ingresa un monto válido, por ejemplo 500 o 1250.50.";
        if (monto.signum() <= 0) return "El monto debe ser mayor que cero.";
        if (monto.compareTo(MONTO_MAXIMO) > 0) return "El monto es demasiado alto.";

        String m = metodo == null ? "" : metodo.trim().toUpperCase(Locale.ROOT);
        if (!Pago.METODOS.contains(m)) return "Elige un método de pago válido.";

        String est = estado == null || estado.isBlank() ? Pago.COMPLETADO : estado.trim().toUpperCase(Locale.ROOT);
        if (!Pago.COMPLETADO.equals(est) && !Pago.PENDIENTE.equals(est)) {
            return "El estado inicial del pago no es válido.";
        }

        String comp = comprobante == null ? null : comprobante.trim();
        if (comp != null && comp.isEmpty()) comp = null;
        if (comp != null && comp.length() > COMPROBANTE_MAXIMO) {
            return "El enlace del comprobante no puede superar los " + COMPROBANTE_MAXIMO + " caracteres.";
        }
        if (comp != null && !comp.toLowerCase(Locale.ROOT).startsWith("http://")
                && !comp.toLowerCase(Locale.ROOT).startsWith("https://")) {
            return "El comprobante debe ser un enlace que empiece con http:// o https://.";
        }

        BigDecimal total = pagoDAO.totalAcordado(idEvento);
        if (total == null) return "Este evento no tiene una cotización aceptada; no se pueden registrar pagos.";
        if (Pago.COMPLETADO.equals(est)) {
            ResumenPago r = ResumenPago.calcular(total, pagoDAO.listarPorEvento(idEvento));
            if (monto.compareTo(r.getSaldo()) > 0) {
                return "El monto supera el saldo pendiente (S/ " + fmt(r.getSaldo()) + ").";
            }
        }

        Pago p = new Pago(idEvento, monto, m, est, comp);
        return pagoDAO.registrar(p) != null
                ? null
                : "No se pudo registrar el pago. Revisa que no supere el saldo pendiente y vuelve a intentar.";
    }

    /** Verifica un pago PENDIENTE: pasa a COMPLETADO si no supera el total acordado. */
    public String confirmar(int idPago) {
        Pago p = pagoDAO.buscarPorId(idPago);
        if (p == null) return "El pago indicado no existe.";
        if (!p.isPendiente()) return "Este pago ya no está pendiente (estado actual: " + p.getEstado() + ").";
        return pagoDAO.confirmar(idPago)
                ? null
                : "No se pudo confirmar: el pago superaría el total acordado o cambió mientras lo revisabas.";
    }

    /** Rechaza un pago PENDIENTE o anula uno COMPLETADO. */
    public String rechazar(int idPago) {
        Pago p = pagoDAO.buscarPorId(idPago);
        if (p == null) return "El pago indicado no existe.";
        if (Pago.RECHAZADO.equals(p.getEstado())) return "Este pago ya está rechazado.";
        return pagoDAO.cambiarEstado(idPago, p.getEstado(), Pago.RECHAZADO)
                ? null
                : "El pago cambió mientras lo revisabas. Recarga la página.";
    }

    /** @return null si el evento admite pagos; si no, el motivo */
    public String motivoNoAdmitePagos(Evento evento) {
        if (evento == null) return "El evento indicado no existe.";
        String e = evento.getEstado();
        if ("CONFIRMADO".equals(e) || "EN_PROCESO".equals(e) || "FINALIZADO".equals(e)) return null;
        if ("CANCELADO".equals(e)) return "El evento está cancelado; no se pueden registrar pagos.";
        return "Los pagos se registran cuando el cliente acepta el acuerdo (el evento queda CONFIRMADO).";
    }

    /** Convierte "1,250.50", "1250,50" o "500" a BigDecimal con 2 decimales; null si no es válido. */
    static BigDecimal parsearMonto(String txt) {
        if (txt == null) return null;
        String t = txt.trim().replace("S/", "").replace(" ", "");
        if (t.isEmpty()) return null;
        int coma = t.lastIndexOf(','), punto = t.lastIndexOf('.');
        if (coma >= 0 && punto >= 0) {
            // el último separador es el decimal; el otro es de miles
            if (coma > punto) t = t.replace(".", "").replace(',', '.');
            else t = t.replace(",", "");
        } else if (coma >= 0) {
            t = t.replace(',', '.');
        }
        if (!t.matches("\\d{1,10}(\\.\\d{1,2})?")) return null;
        return new BigDecimal(t).setScale(2, RoundingMode.HALF_UP);
    }

    public ResumenPago resumen(int idEvento) {
        return ResumenPago.calcular(pagoDAO.totalAcordado(idEvento), pagoDAO.listarPorEvento(idEvento));
    }

    public List<Pago> listarPorEvento(int idEvento) { return pagoDAO.listarPorEvento(idEvento); }

    public Pago buscarPorId(int idPago) { return pagoDAO.buscarPorId(idPago); }

    public List<Pago> buscar(String texto, String filtro) { return pagoDAO.buscar(texto, filtro); }

    public int contarPendientes() { return pagoDAO.contarPorEstado(Pago.PENDIENTE); }

    private String fmt(BigDecimal v) { return String.format(Locale.US, "%,.2f", v); }
}
