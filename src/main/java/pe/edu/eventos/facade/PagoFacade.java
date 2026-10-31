package pe.edu.eventos.facade;

import pe.edu.eventos.dao.EventoDAO;
import pe.edu.eventos.dao.impl.EventoDAOImpl;
import pe.edu.eventos.dto.PanelPagoDTO;
import pe.edu.eventos.model.Evento;
import pe.edu.eventos.model.Pago;
import pe.edu.eventos.service.PagoService;

import java.util.List;

/** Punto de entrada del servlet de pagos. */
public class PagoFacade {

    private final PagoService pagoService;
    private final EventoDAO eventoDAO;

    public PagoFacade() {
        this(new PagoService(), new EventoDAOImpl());
    }

    public PagoFacade(PagoService pagoService, EventoDAO eventoDAO) {
        this.pagoService = pagoService;
        this.eventoDAO = eventoDAO;
    }

    /** @return el panel del evento; null si el evento no existe */
    public PanelPagoDTO armarPanel(int idEvento) {
        Evento evento = eventoDAO.buscarPorId(idEvento);
        if (evento == null) return null;
        return new PanelPagoDTO(evento, pagoService.resumen(idEvento),
                pagoService.listarPorEvento(idEvento), pagoService.motivoNoAdmitePagos(evento));
    }

    public String registrar(int idEvento, String monto, String metodo, String estado, String comprobante) {
        return pagoService.registrar(idEvento, monto, metodo, estado, comprobante);
    }

    public String confirmar(int idPago) { return pagoService.confirmar(idPago); }

    public String rechazar(int idPago) { return pagoService.rechazar(idPago); }

    public Pago buscarPorId(int idPago) { return pagoService.buscarPorId(idPago); }

    public List<Pago> buscar(String texto, String filtro) { return pagoService.buscar(texto, filtro); }

    public int contarPendientes() { return pagoService.contarPendientes(); }
}
