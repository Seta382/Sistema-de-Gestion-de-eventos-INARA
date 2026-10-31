package pe.edu.eventos.facade;

import pe.edu.eventos.dao.ClienteDAO;
import pe.edu.eventos.dao.EventoDAO;
import pe.edu.eventos.dao.impl.ClienteDAOImpl;
import pe.edu.eventos.dao.impl.EventoDAOImpl;
import pe.edu.eventos.dto.PanelCotizacionDTO;
import pe.edu.eventos.model.Cotizacion;
import pe.edu.eventos.model.Evento;
import pe.edu.eventos.service.CotizacionService;
import pe.edu.eventos.service.ServicioService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Punto de entrada de los servlets de cotización: junta evento, servicios y cotizaciones
 * para que el controlador no tenga que conocer cada capa.
 */
public class CotizacionFacade {

    private final CotizacionService cotizacionService;
    private final ServicioService servicioService;
    private final EventoDAO eventoDAO;
    private final ClienteDAO clienteDAO;

    public CotizacionFacade() {
        this(new CotizacionService(), new ServicioService(), new EventoDAOImpl(), new ClienteDAOImpl());
    }

    public CotizacionFacade(CotizacionService cotizacionService, ServicioService servicioService,
                            EventoDAO eventoDAO, ClienteDAO clienteDAO) {
        this.cotizacionService = cotizacionService;
        this.servicioService = servicioService;
        this.eventoDAO = eventoDAO;
        this.clienteDAO = clienteDAO;
    }

    /** @return el panel del evento; null si el evento no existe */
    public PanelCotizacionDTO armarPanel(int idEvento) {
        Evento evento = eventoDAO.buscarPorId(idEvento);
        if (evento == null) return null;
        List<Cotizacion> cotizaciones = cotizacionService.listarPorEvento(idEvento);
        String motivo = cotizacionService.motivoNoCotizable(evento, cotizaciones);
        return new PanelCotizacionDTO(evento, servicioService.listarActivos(), cotizaciones, motivo);
    }

    public String crear(int idEvento, Map<Integer, Integer> cantidades, BigDecimal descuento, String observaciones) {
        return cotizacionService.crear(idEvento, cantidades, descuento, observaciones);
    }

    public String responderComoPersonal(int idCotizacion, boolean aceptar) {
        return cotizacionService.responderComoPersonal(idCotizacion, aceptar);
    }

    /** idUsuario es el de la sesión; aquí se convierte al id de cliente. */
    public String responderComoCliente(int idUsuario, int idCotizacion, boolean aceptar) {
        Integer idCliente = clienteDAO.buscarIdPorUsuario(idUsuario);
        if (idCliente == null) return "No se encontró tu perfil de cliente.";
        return cotizacionService.responderComoCliente(idCotizacion, idCliente, aceptar);
    }

    public List<Cotizacion> listarDelCliente(int idUsuario) {
        Integer idCliente = clienteDAO.buscarIdPorUsuario(idUsuario);
        return idCliente == null ? Collections.emptyList() : cotizacionService.listarPorCliente(idCliente);
    }

    public int contarPendientesDelCliente(int idUsuario) {
        Integer idCliente = clienteDAO.buscarIdPorUsuario(idUsuario);
        return idCliente == null ? 0 : cotizacionService.contarPendientesDelCliente(idCliente);
    }

    public List<Cotizacion> buscar(String texto, String filtro) {
        return cotizacionService.buscar(texto, filtro);
    }

    public int contarPendientes() {
        return cotizacionService.contarPendientes();
    }
}
