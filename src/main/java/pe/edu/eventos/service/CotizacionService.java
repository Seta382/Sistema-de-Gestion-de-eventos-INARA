package pe.edu.eventos.service;

import pe.edu.eventos.dao.CotizacionDAO;
import pe.edu.eventos.dao.EventoDAO;
import pe.edu.eventos.dao.ServicioDAO;
import pe.edu.eventos.dao.impl.CotizacionDAOImpl;
import pe.edu.eventos.dao.impl.EventoDAOImpl;
import pe.edu.eventos.dao.impl.ServicioDAOImpl;
import pe.edu.eventos.model.Cotizacion;
import pe.edu.eventos.model.DetalleCotizacion;
import pe.edu.eventos.model.Evento;
import pe.edu.eventos.model.Servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reglas de negocio de las cotizaciones.
 * - Los precios siempre se leen de la base (nunca se confía en lo que manda el navegador).
 * - Solo hay una cotización PENDIENTE por evento: al crear otra, la anterior pasa a MODIFICADA.
 * - Con una cotización ACEPTADA ya no se cotiza de nuevo (sigue el acuerdo).
 */
public class CotizacionService {

    // cotizacion.total es NUMERIC(10,2): como máximo 99,999,999.99
    private static final BigDecimal TOTAL_MAXIMO = new BigDecimal("99999999.99");
    private static final int CANTIDAD_MAXIMA = 999;
    private static final int OBSERVACIONES_MAXIMO = 1000;

    private final CotizacionDAO cotizacionDAO;
    private final EventoDAO eventoDAO;
    private final ServicioDAO servicioDAO;

    public CotizacionService() {
        this(new CotizacionDAOImpl(), new EventoDAOImpl(), new ServicioDAOImpl());
    }

    public CotizacionService(CotizacionDAO cotizacionDAO, EventoDAO eventoDAO, ServicioDAO servicioDAO) {
        this.cotizacionDAO = cotizacionDAO;
        this.eventoDAO = eventoDAO;
        this.servicioDAO = servicioDAO;
    }

    /**
     * Crea una cotización PENDIENTE.
     *
     * @param cantidades  idServicio -> cantidad (solo los servicios incluidos)
     * @param descuento   monto en soles (null = sin descuento)
     * @return null si se creó; un mensaje de error si no
     */
    public String crear(int idEvento, Map<Integer, Integer> cantidades, BigDecimal descuento, String observaciones) {
        Evento evento = eventoDAO.buscarPorId(idEvento);
        if (evento == null) {
            return "El evento indicado no existe.";
        }
        String motivo = motivoNoCotizable(evento, cotizacionDAO.listarPorEvento(idEvento));
        if (motivo != null) return motivo;

        if (cantidades == null || cantidades.isEmpty()) {
            return "Selecciona al menos un servicio para cotizar.";
        }

        List<DetalleCotizacion> detalles = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (Map.Entry<Integer, Integer> par : cantidades.entrySet()) {
            Integer cantidad = par.getValue();
            if (cantidad == null || cantidad < 1 || cantidad > CANTIDAD_MAXIMA) {
                return "La cantidad de cada servicio debe estar entre 1 y " + CANTIDAD_MAXIMA + ".";
            }
            Servicio s = servicioDAO.buscarPorId(par.getKey());
            if (s == null) {
                return "Uno de los servicios seleccionados ya no existe.";
            }
            if (!s.isActivo()) {
                return "El servicio «" + s.getNombre() + "» está dado de baja. Quítalo de la cotización.";
            }
            BigDecimal linea = s.getPrecio().multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
            detalles.add(new DetalleCotizacion(s.getIdServicio(), cantidad, s.getPrecio(), linea));
            subtotal = subtotal.add(linea);
        }

        BigDecimal desc = descuento == null ? BigDecimal.ZERO : descuento.setScale(2, RoundingMode.HALF_UP);
        if (desc.compareTo(BigDecimal.ZERO) < 0) {
            return "El descuento no puede ser negativo.";
        }
        if (desc.compareTo(subtotal) >= 0) {
            return "El descuento debe ser menor al subtotal (S/ " + subtotal.toPlainString() + ").";
        }
        BigDecimal total = subtotal.subtract(desc);
        if (subtotal.compareTo(TOTAL_MAXIMO) > 0) {
            return "El total de la cotización supera el máximo permitido.";
        }

        String obs = observaciones == null ? null : observaciones.trim();
        if (obs != null && obs.length() > OBSERVACIONES_MAXIMO) {
            return "Las observaciones no pueden superar los " + OBSERVACIONES_MAXIMO + " caracteres.";
        }

        Cotizacion c = new Cotizacion();
        c.setIdEvento(idEvento);
        c.setSubtotal(subtotal);
        c.setDescuento(desc);
        c.setTotal(total);
        c.setObservaciones(obs == null || obs.isEmpty() ? null : obs);

        return cotizacionDAO.registrar(c, detalles) != null
                ? null
                : "No se pudo registrar la cotización. Es posible que el evento ya tenga una cotización aceptada; recarga la página.";
    }

    /** El personal registra la respuesta del cliente (por ejemplo, si aceptó por teléfono o WhatsApp). */
    public String responderComoPersonal(int idCotizacion, boolean aceptar) {
        return responder(cotizacionDAO.buscarPorId(idCotizacion), null, aceptar);
    }

    /** El cliente responde su propia cotización desde su portal. */
    public String responderComoCliente(int idCotizacion, int idCliente, boolean aceptar) {
        return responder(cotizacionDAO.buscarPorId(idCotizacion), idCliente, aceptar);
    }

    private String responder(Cotizacion c, Integer idClienteSolicitante, boolean aceptar) {
        // Un cliente que pide una cotización ajena recibe el mismo mensaje que si no existiera
        if (c == null || (idClienteSolicitante != null && !idClienteSolicitante.equals(c.getIdCliente()))) {
            return "La cotización indicada no existe.";
        }
        if (!c.isPendiente()) {
            return "Esta cotización ya no está pendiente (estado actual: " + c.getEstado() + ").";
        }
        if (esEventoCerrado(c.getEstadoEvento())) {
            return "El evento está " + c.getEstadoEvento().toLowerCase() + "; ya no se puede responder esta cotización.";
        }
        String nuevo = aceptar ? Cotizacion.ACEPTADA : Cotizacion.RECHAZADA;
        return cotizacionDAO.cambiarEstado(c.getIdCotizacion(), Cotizacion.PENDIENTE, nuevo)
                ? null
                : "La cotización cambió mientras la revisabas. Recarga la página.";
    }

    /**
     * @return null si el evento admite una cotización nueva; si no, el motivo.
     */
    public String motivoNoCotizable(Evento evento, List<Cotizacion> cotizaciones) {
        if (evento == null) return "El evento indicado no existe.";
        if (esEventoCerrado(evento.getEstado())) {
            return "El evento está " + evento.getEstado().toLowerCase() + "; no se pueden crear cotizaciones nuevas.";
        }
        for (Cotizacion c : cotizaciones) {
            if (Cotizacion.ACEPTADA.equals(c.getEstado())) {
                return "Este evento ya tiene la cotización #" + c.getIdCotizacion()
                        + " aceptada. El siguiente paso es el acuerdo con el cliente.";
            }
        }
        return null;
    }

    private boolean esEventoCerrado(String estadoEvento) {
        return "CANCELADO".equals(estadoEvento) || "FINALIZADO".equals(estadoEvento);
    }

    public List<Cotizacion> listarPorEvento(int idEvento) {
        return cotizacionDAO.listarPorEvento(idEvento);
    }

    public List<Cotizacion> listarPorCliente(int idCliente) {
        return cotizacionDAO.listarPorCliente(idCliente);
    }

    public List<Cotizacion> buscar(String texto, String filtro) {
        return cotizacionDAO.buscar(texto, filtro);
    }

    public int contarPendientes() {
        return cotizacionDAO.contarPorEstado(Cotizacion.PENDIENTE);
    }

    public int contarPendientesDelCliente(int idCliente) {
        return cotizacionDAO.contarPendientesPorCliente(idCliente);
    }
}
