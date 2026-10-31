package pe.edu.eventos.facade;

import pe.edu.eventos.dao.ClienteDAO;
import pe.edu.eventos.dao.EventoDAO;
import pe.edu.eventos.dao.impl.ClienteDAOImpl;
import pe.edu.eventos.dao.impl.EventoDAOImpl;
import pe.edu.eventos.dto.PanelAcuerdoDTO;
import pe.edu.eventos.model.Acuerdo;
import pe.edu.eventos.model.Cotizacion;
import pe.edu.eventos.model.Evento;
import pe.edu.eventos.service.AcuerdoService;
import pe.edu.eventos.service.CotizacionService;

import java.util.Collections;
import java.util.List;

/**
 * Punto de entrada de los servlets de acuerdo: junta evento, cotización aceptada y acuerdos
 * para que el controlador no tenga que conocer cada capa.
 */
public class AcuerdoFacade {

    private final AcuerdoService acuerdoService;
    private final CotizacionService cotizacionService;
    private final EventoDAO eventoDAO;
    private final ClienteDAO clienteDAO;

    public AcuerdoFacade() {
        this(new AcuerdoService(), new CotizacionService(), new EventoDAOImpl(), new ClienteDAOImpl());
    }

    public AcuerdoFacade(AcuerdoService acuerdoService, CotizacionService cotizacionService,
                         EventoDAO eventoDAO, ClienteDAO clienteDAO) {
        this.acuerdoService = acuerdoService;
        this.cotizacionService = cotizacionService;
        this.eventoDAO = eventoDAO;
        this.clienteDAO = clienteDAO;
    }

    /** @return el panel del evento; null si el evento no existe */
    public PanelAcuerdoDTO armarPanel(int idEvento) {
        Evento evento = eventoDAO.buscarPorId(idEvento);
        if (evento == null) return null;
        List<Cotizacion> cotizaciones = cotizacionService.listarPorEvento(idEvento);
        List<Acuerdo> acuerdos = acuerdoService.listarPorEvento(idEvento);
        Cotizacion aceptada = acuerdoService.cotizacionAceptada(cotizaciones);
        String motivo = acuerdoService.motivoNoAcordable(evento, cotizaciones, acuerdos);
        String borrador = motivo == null ? acuerdoService.generarBorrador(evento, aceptada) : null;
        return new PanelAcuerdoDTO(evento, aceptada, acuerdos, motivo, borrador);
    }

    public String crear(int idEvento, String condiciones) {
        return acuerdoService.crear(idEvento, condiciones);
    }

    public String actualizarCondiciones(int idAcuerdo, String condiciones) {
        return acuerdoService.actualizarCondiciones(idAcuerdo, condiciones);
    }

    public String aceptarComoPersonal(int idAcuerdo) {
        return acuerdoService.aceptarComoPersonal(idAcuerdo);
    }

    public String cancelar(int idAcuerdo) {
        return acuerdoService.cancelar(idAcuerdo);
    }

    /** idUsuario es el de la sesión; aquí se convierte al id de cliente. */
    public String aceptarComoCliente(int idUsuario, int idAcuerdo) {
        Integer idCliente = clienteDAO.buscarIdPorUsuario(idUsuario);
        if (idCliente == null) return "No se encontró tu perfil de cliente.";
        return acuerdoService.aceptarComoCliente(idAcuerdo, idCliente);
    }

    public Acuerdo buscarPorId(int idAcuerdo) {
        return acuerdoService.buscarPorId(idAcuerdo);
    }

    public List<Acuerdo> listarDelCliente(int idUsuario) {
        Integer idCliente = clienteDAO.buscarIdPorUsuario(idUsuario);
        return idCliente == null ? Collections.emptyList() : acuerdoService.listarPorCliente(idCliente);
    }

    public int contarPendientesDelCliente(int idUsuario) {
        Integer idCliente = clienteDAO.buscarIdPorUsuario(idUsuario);
        return idCliente == null ? 0 : acuerdoService.contarPendientesDelCliente(idCliente);
    }

    public List<Acuerdo> buscar(String texto, String filtro) {
        return acuerdoService.buscar(texto, filtro);
    }

    public int contarPendientes() {
        return acuerdoService.contarPendientes();
    }
}
