package pe.edu.eventos.service;

import pe.edu.eventos.dao.EventoDAO;
import pe.edu.eventos.dao.SeguimientoDAO;
import pe.edu.eventos.dao.impl.EventoDAOImpl;
import pe.edu.eventos.dao.impl.SeguimientoDAOImpl;
import pe.edu.eventos.model.Evento;
import pe.edu.eventos.model.Seguimiento;

import java.util.List;

/**
 * Reglas del seguimiento: el personal registra avances mientras el evento está
 * CONFIRMADO, EN_PROCESO o FINALIZADO; el cliente los ve en "Mi evento".
 */
public class SeguimientoService {

    private static final int TITULO_MAXIMO = 150;
    private static final int DESCRIPCION_MAXIMO = 2000;

    private final SeguimientoDAO seguimientoDAO;
    private final EventoDAO eventoDAO;

    public SeguimientoService() {
        this(new SeguimientoDAOImpl(), new EventoDAOImpl());
    }

    public SeguimientoService(SeguimientoDAO seguimientoDAO, EventoDAO eventoDAO) {
        this.seguimientoDAO = seguimientoDAO;
        this.eventoDAO = eventoDAO;
    }

    /** @return null si se registró; un mensaje de error si no */
    public String registrar(int idEvento, String titulo, String descripcion) {
        Evento evento = eventoDAO.buscarPorId(idEvento);
        String motivo = motivoNoAdmiteAvances(evento);
        if (motivo != null) return motivo;

        String error = validar(titulo, descripcion);
        if (error != null) return error;

        return seguimientoDAO.registrar(new Seguimiento(idEvento, titulo.trim(), limpiar(descripcion))) != null
                ? null
                : "No se pudo registrar el avance. Intenta nuevamente.";
    }

    public String actualizar(int idSeguimiento, String titulo, String descripcion) {
        if (seguimientoDAO.buscarPorId(idSeguimiento) == null) return "El avance indicado no existe.";
        String error = validar(titulo, descripcion);
        if (error != null) return error;
        return seguimientoDAO.actualizar(idSeguimiento, titulo.trim(), limpiar(descripcion))
                ? null
                : "No se pudo guardar el avance. Intenta nuevamente.";
    }

    public String eliminar(int idSeguimiento) {
        if (seguimientoDAO.buscarPorId(idSeguimiento) == null) return "El avance indicado no existe.";
        return seguimientoDAO.eliminar(idSeguimiento) ? null : "No se pudo eliminar el avance.";
    }

    /** @return null si el evento admite avances; si no, el motivo */
    public String motivoNoAdmiteAvances(Evento evento) {
        if (evento == null) return "El evento indicado no existe.";
        String e = evento.getEstado();
        if ("CONFIRMADO".equals(e) || "EN_PROCESO".equals(e) || "FINALIZADO".equals(e)) return null;
        if ("CANCELADO".equals(e)) return "El evento está cancelado; no se pueden registrar avances.";
        return "El seguimiento empieza cuando el cliente acepta el acuerdo (el evento queda CONFIRMADO).";
    }

    private String validar(String titulo, String descripcion) {
        String t = titulo == null ? "" : titulo.trim();
        if (t.isEmpty()) return "El título del avance es obligatorio.";
        if (t.length() > TITULO_MAXIMO) return "El título no puede superar los " + TITULO_MAXIMO + " caracteres.";
        if (descripcion != null && descripcion.trim().length() > DESCRIPCION_MAXIMO) {
            return "El detalle no puede superar los " + DESCRIPCION_MAXIMO + " caracteres.";
        }
        return null;
    }

    private String limpiar(String d) {
        if (d == null) return null;
        String t = d.trim();
        return t.isEmpty() ? null : t;
    }

    public List<Seguimiento> listarPorEvento(int idEvento) { return seguimientoDAO.listarPorEvento(idEvento); }

    public Seguimiento buscarPorId(int idSeguimiento) { return seguimientoDAO.buscarPorId(idSeguimiento); }
}
