package pe.edu.eventos.facade;

import pe.edu.eventos.dao.ClienteDAO;
import pe.edu.eventos.dao.EventoClienteDAO;
import pe.edu.eventos.dao.impl.ClienteDAOImpl;
import pe.edu.eventos.dao.impl.EventoClienteDAOImpl;
import pe.edu.eventos.dto.MiEventoDTO;
import pe.edu.eventos.model.Acuerdo;
import pe.edu.eventos.model.Cotizacion;
import pe.edu.eventos.model.Evento;
import pe.edu.eventos.model.Pago;
import pe.edu.eventos.model.ResumenPago;
import pe.edu.eventos.service.AcuerdoService;
import pe.edu.eventos.service.CotizacionService;
import pe.edu.eventos.service.PagoService;
import pe.edu.eventos.service.SeguimientoService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Arma la pantalla "Mi evento" del cliente juntando cotización, acuerdo, pagos y seguimiento. */
public class MiEventoFacade {

    static final String[] ETAPAS = {"SOLICITADO", "COTIZADO", "CONFIRMADO", "EN_PROCESO", "FINALIZADO"};
    static final String[] NOMBRES_ETAPAS = {"Solicitado", "Cotizado", "Confirmado", "En preparación", "Finalizado"};

    private final ClienteDAO clienteDAO;
    private final EventoClienteDAO eventoClienteDAO;
    private final CotizacionService cotizacionService;
    private final AcuerdoService acuerdoService;
    private final PagoService pagoService;
    private final SeguimientoService seguimientoService;

    public MiEventoFacade() {
        this(new ClienteDAOImpl(), new EventoClienteDAOImpl(), new CotizacionService(),
                new AcuerdoService(), new PagoService(), new SeguimientoService());
    }

    public MiEventoFacade(ClienteDAO clienteDAO, EventoClienteDAO eventoClienteDAO,
                          CotizacionService cotizacionService, AcuerdoService acuerdoService,
                          PagoService pagoService, SeguimientoService seguimientoService) {
        this.clienteDAO = clienteDAO;
        this.eventoClienteDAO = eventoClienteDAO;
        this.cotizacionService = cotizacionService;
        this.acuerdoService = acuerdoService;
        this.pagoService = pagoService;
        this.seguimientoService = seguimientoService;
    }

    public List<Evento> listarEventos(int idUsuario) {
        Integer idCliente = clienteDAO.buscarIdPorUsuario(idUsuario);
        return idCliente == null ? Collections.emptyList() : eventoClienteDAO.listarPorCliente(idCliente);
    }

    /**
     * @param idEventoElegido evento pedido por el cliente; null = el primero de su lista
     * @return null si el cliente no tiene eventos. Un id que no es suyo cae en el primer evento.
     */
    public MiEventoDTO armar(int idUsuario, Integer idEventoElegido) {
        List<Evento> eventos = listarEventos(idUsuario);
        if (eventos.isEmpty()) return null;

        Evento evento = eventos.get(0);
        if (idEventoElegido != null) {
            for (Evento e : eventos) {
                if (e.getIdEvento().equals(idEventoElegido)) {
                    evento = e;
                    break;
                }
            }
        }
        int id = evento.getIdEvento();

        List<Cotizacion> cotizaciones = cotizacionService.listarPorEvento(id);
        List<Acuerdo> acuerdos = acuerdoService.listarPorEvento(id);
        Cotizacion aceptada = acuerdoService.cotizacionAceptada(cotizaciones);
        Acuerdo vigente = null;
        for (Acuerdo a : acuerdos) {
            if (a.isVigente()) {
                vigente = a;
                break;
            }
        }

        ResumenPago resumen = pagoService.resumen(id);
        List<Pago> pagos = new ArrayList<>();
        for (Pago p : pagoService.listarPorEvento(id)) {
            if (!Pago.RECHAZADO.equals(p.getEstado())) pagos.add(p);
        }

        String[] paso = calcularProximoPaso(evento.getEstado(), cotizaciones, aceptada, vigente, resumen);
        return new MiEventoDTO(eventos, evento, construirEtapas(evento.getEstado()), aceptada, vigente,
                resumen, pagos, seguimientoService.listarPorEvento(id), paso[0], paso[1], paso[2]);
    }

    /** Marca cada etapa como HECHO, ACTUAL o PENDIENTE según el estado del evento. */
    static List<MiEventoDTO.Etapa> construirEtapas(String estadoEvento) {
        int actual = -1;
        for (int i = 0; i < ETAPAS.length; i++) {
            if (ETAPAS[i].equals(estadoEvento)) actual = i;
        }
        List<MiEventoDTO.Etapa> lista = new ArrayList<>();
        for (int i = 0; i < ETAPAS.length; i++) {
            String estado = actual < 0 ? "PENDIENTE" : (i < actual ? "HECHO" : (i == actual ? "ACTUAL" : "PENDIENTE"));
            // Un evento finalizado ya terminó todas sus etapas
            if (i == actual && "FINALIZADO".equals(estadoEvento)) estado = "HECHO";
            lista.add(new MiEventoDTO.Etapa(NOMBRES_ETAPAS[i], estado));
        }
        return lista;
    }

    /** @return {texto, url (o null), texto del botón (o null)} */
    static String[] calcularProximoPaso(String estado, List<Cotizacion> cotizaciones, Cotizacion aceptada,
                                        Acuerdo vigente, ResumenPago resumen) {
        if ("CANCELADO".equals(estado)) {
            return new String[]{"Este evento fue cancelado. Si quieres retomarlo, escríbenos y lo vemos juntos.", null, null};
        }
        if ("FINALIZADO".equals(estado)) {
            return new String[]{"¡Tu evento ya se realizó! Gracias por confiar en INARA.", null, null};
        }
        if ("SOLICITADO".equals(estado)) {
            return new String[]{"Estamos preparando tu cotización. Te avisaremos apenas esté lista.", null, null};
        }
        if ("COTIZADO".equals(estado)) {
            for (Cotizacion c : cotizaciones) {
                if (Cotizacion.PENDIENTE.equals(c.getEstado())) {
                    return new String[]{"Tienes una cotización lista. Revísala y dinos si la aceptas.",
                            "/mis-cotizaciones", "Ver cotización"};
                }
            }
            if (aceptada == null) {
                return new String[]{"Estamos ajustando tu cotización. Te avisaremos cuando esté lista.", null, null};
            }
            if (vigente != null && vigente.isPendiente()) {
                return new String[]{"Tu acuerdo está listo. Léelo y acéptalo para confirmar tu evento.",
                        "/mis-acuerdos", "Ver acuerdo"};
            }
            return new String[]{"Aceptaste tu cotización. Estamos redactando el acuerdo del evento.", null, null};
        }
        // CONFIRMADO o EN_PROCESO
        if (resumen.isTieneTotal() && !resumen.isPagadoCompleto()) {
            return new String[]{"Coordina con nuestro equipo el pago del saldo pendiente. "
                    + "Cuando lo registremos lo verás aquí.", null, null};
        }
        if ("EN_PROCESO".equals(estado)) {
            return new String[]{"Estamos preparando tu evento. Aquí verás cada avance.", null, null};
        }
        return new String[]{"Tu evento está confirmado. Pronto verás aquí los avances de la preparación.", null, null};
    }
}
