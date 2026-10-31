package pe.edu.eventos.service;

import pe.edu.eventos.dao.AcuerdoDAO;
import pe.edu.eventos.dao.CotizacionDAO;
import pe.edu.eventos.dao.EventoDAO;
import pe.edu.eventos.dao.impl.AcuerdoDAOImpl;
import pe.edu.eventos.dao.impl.CotizacionDAOImpl;
import pe.edu.eventos.dao.impl.EventoDAOImpl;
import pe.edu.eventos.model.Acuerdo;
import pe.edu.eventos.model.Cotizacion;
import pe.edu.eventos.model.DetalleCotizacion;
import pe.edu.eventos.model.Evento;

import java.util.List;
import java.util.Locale;

/**
 * Reglas de negocio de los acuerdos.
 * - Solo se crea un acuerdo cuando el evento tiene una cotización ACEPTADA.
 * - Hay un solo acuerdo vigente (PENDIENTE o ACEPTADO) por evento.
 * - Al aceptarse el acuerdo, el evento pasa a CONFIRMADO (lo hace el DAO en la misma transacción).
 */
public class AcuerdoService {

    private static final int CONDICIONES_MAXIMO = 5000;

    private final AcuerdoDAO acuerdoDAO;
    private final CotizacionDAO cotizacionDAO;
    private final EventoDAO eventoDAO;

    public AcuerdoService() {
        this(new AcuerdoDAOImpl(), new CotizacionDAOImpl(), new EventoDAOImpl());
    }

    public AcuerdoService(AcuerdoDAO acuerdoDAO, CotizacionDAO cotizacionDAO, EventoDAO eventoDAO) {
        this.acuerdoDAO = acuerdoDAO;
        this.cotizacionDAO = cotizacionDAO;
        this.eventoDAO = eventoDAO;
    }

    /** @return null si se creó; un mensaje de error si no */
    public String crear(int idEvento, String condiciones) {
        Evento evento = eventoDAO.buscarPorId(idEvento);
        if (evento == null) {
            return "El evento indicado no existe.";
        }
        String motivo = motivoNoAcordable(evento, cotizacionDAO.listarPorEvento(idEvento),
                acuerdoDAO.listarPorEvento(idEvento));
        if (motivo != null) return motivo;

        String error = validarCondiciones(condiciones);
        if (error != null) return error;

        return acuerdoDAO.registrar(new Acuerdo(idEvento, condiciones.trim())) != null
                ? null
                : "No se pudo registrar el acuerdo. Es posible que otra persona ya lo haya creado; recarga la página.";
    }

    /** Edita las condiciones mientras el cliente no haya respondido. */
    public String actualizarCondiciones(int idAcuerdo, String condiciones) {
        Acuerdo a = acuerdoDAO.buscarPorId(idAcuerdo);
        if (a == null) return "El acuerdo indicado no existe.";
        if (!a.isPendiente()) {
            return "Solo se pueden editar las condiciones de un acuerdo pendiente (estado actual: " + a.getEstado() + ").";
        }
        String error = validarCondiciones(condiciones);
        if (error != null) return error;

        return acuerdoDAO.actualizarCondiciones(idAcuerdo, condiciones.trim())
                ? null
                : "El acuerdo cambió mientras lo editabas. Recarga la página.";
    }

    /** El personal registra la aceptación del cliente (por ejemplo, si firmó en persona). */
    public String aceptarComoPersonal(int idAcuerdo) {
        return aceptar(acuerdoDAO.buscarPorId(idAcuerdo), null);
    }

    /** El cliente acepta su propio acuerdo desde el portal. */
    public String aceptarComoCliente(int idAcuerdo, int idCliente) {
        return aceptar(acuerdoDAO.buscarPorId(idAcuerdo), idCliente);
    }

    private String aceptar(Acuerdo a, Integer idClienteSolicitante) {
        // Un cliente que pide un acuerdo ajeno recibe el mismo mensaje que si no existiera
        if (a == null || (idClienteSolicitante != null && !idClienteSolicitante.equals(a.getIdCliente()))) {
            return "El acuerdo indicado no existe.";
        }
        if (!a.isPendiente()) {
            return "Este acuerdo ya no está pendiente (estado actual: " + a.getEstado() + ").";
        }
        if (esEventoCerrado(a.getEstadoEvento())) {
            return "El evento está " + a.getEstadoEvento().toLowerCase() + "; ya no se puede aceptar este acuerdo.";
        }
        return acuerdoDAO.aceptar(a.getIdAcuerdo())
                ? null
                : "El acuerdo cambió mientras lo revisabas. Recarga la página.";
    }

    /** Cancela un acuerdo pendiente o aceptado (si estaba aceptado, el evento vuelve a COTIZADO). */
    public String cancelar(int idAcuerdo) {
        Acuerdo a = acuerdoDAO.buscarPorId(idAcuerdo);
        if (a == null) return "El acuerdo indicado no existe.";
        if (!a.isVigente()) {
            return "Este acuerdo ya está cancelado.";
        }
        if (a.isAceptado() && !"CONFIRMADO".equals(a.getEstadoEvento())) {
            return "El evento ya está " + String.valueOf(a.getEstadoEvento()).toLowerCase()
                    + "; no se puede cancelar un acuerdo aceptado en esta etapa.";
        }
        return acuerdoDAO.cancelar(idAcuerdo)
                ? null
                : "El acuerdo cambió mientras lo revisabas. Recarga la página.";
    }

    /**
     * @return null si se puede crear un acuerdo nuevo para el evento; si no, el motivo.
     */
    public String motivoNoAcordable(Evento evento, List<Cotizacion> cotizaciones, List<Acuerdo> acuerdos) {
        if (evento == null) return "El evento indicado no existe.";
        if (esEventoCerrado(evento.getEstado())) {
            return "El evento está " + evento.getEstado().toLowerCase() + "; no se pueden crear acuerdos nuevos.";
        }
        if (cotizacionAceptada(cotizaciones) == null) {
            return "Para crear el acuerdo, primero el cliente debe aceptar una cotización.";
        }
        for (Acuerdo a : acuerdos) {
            if (a.isVigente()) {
                return "Este evento ya tiene el acuerdo #" + a.getIdAcuerdo() + " "
                        + (a.isAceptado() ? "aceptado" : "pendiente")
                        + ". Cancélalo si quieres redactar uno nuevo.";
            }
        }
        return null;
    }

    public Cotizacion cotizacionAceptada(List<Cotizacion> cotizaciones) {
        for (Cotizacion c : cotizaciones) {
            if (Cotizacion.ACEPTADA.equals(c.getEstado())) return c;
        }
        return null;
    }

    /** Texto inicial para el formulario: servicios y total de la cotización aceptada más cláusulas base. */
    public String generarBorrador(Evento evento, Cotizacion cotizacion) {
        StringBuilder sb = new StringBuilder();
        sb.append("ACUERDO DE SERVICIOS — ")
                .append(evento.getNombre() != null ? evento.getNombre() : "Evento INARA").append("\n\n");
        sb.append("Cliente: ").append(evento.getNombreCliente() != null ? evento.getNombreCliente() : "-").append("\n");
        sb.append("Fecha del evento: ").append(evento.getFechaEvento() != null ? evento.getFechaEvento() : "-").append("\n");
        sb.append("Lugar: ").append(evento.getLugar() != null && !evento.getLugar().isBlank() ? evento.getLugar() : "por definir").append("\n");
        sb.append("Invitados: ").append(evento.getNumInvitados() != null ? evento.getNumInvitados() : 0).append("\n\n");

        if (cotizacion != null) {
            sb.append("Servicios contratados (cotización #").append(cotizacion.getIdCotizacion()).append("):\n");
            for (DetalleCotizacion d : cotizacion.getDetalles()) {
                sb.append("- ").append(d.getNombreServicio()).append(" x").append(d.getCantidad())
                        .append(" — S/ ").append(monto(d.getSubtotal().doubleValue())).append("\n");
            }
            if (cotizacion.getDescuento() != null && cotizacion.getDescuento().signum() > 0) {
                sb.append("Descuento: - S/ ").append(monto(cotizacion.getDescuento().doubleValue())).append("\n");
            }
            sb.append("Total acordado: S/ ").append(monto(cotizacion.getTotal().doubleValue())).append("\n\n");
        }

        sb.append("Condiciones de pago:\n");
        sb.append("- Adelanto del 50% para reservar la fecha.\n");
        sb.append("- El saldo se cancela como máximo 7 días antes del evento.\n\n");
        sb.append("Cambios y cancelaciones:\n");
        sb.append("- Los cambios en los servicios se coordinan con al menos 15 días de anticipación.\n");
        sb.append("- Si el cliente cancela, el adelanto no es reembolsable.\n");
        return sb.toString();
    }

    private String monto(double v) {
        return String.format(Locale.US, "%,.2f", v);
    }

    private String validarCondiciones(String condiciones) {
        String c = condiciones == null ? "" : condiciones.trim();
        if (c.isEmpty()) {
            return "Las condiciones del acuerdo son obligatorias.";
        }
        if (c.length() > CONDICIONES_MAXIMO) {
            return "Las condiciones no pueden superar los " + CONDICIONES_MAXIMO + " caracteres.";
        }
        return null;
    }

    private boolean esEventoCerrado(String estadoEvento) {
        return "CANCELADO".equals(estadoEvento) || "FINALIZADO".equals(estadoEvento);
    }

    public Acuerdo buscarPorId(int idAcuerdo) {
        return acuerdoDAO.buscarPorId(idAcuerdo);
    }

    public List<Acuerdo> listarPorEvento(int idEvento) {
        return acuerdoDAO.listarPorEvento(idEvento);
    }

    public List<Acuerdo> listarPorCliente(int idCliente) {
        return acuerdoDAO.listarPorCliente(idCliente);
    }

    public List<Acuerdo> buscar(String texto, String filtro) {
        return acuerdoDAO.buscar(texto, filtro);
    }

    public int contarPendientes() {
        return acuerdoDAO.contarPorEstado(Acuerdo.PENDIENTE);
    }

    public int contarPendientesDelCliente(int idCliente) {
        return acuerdoDAO.contarPendientesPorCliente(idCliente);
    }
}
