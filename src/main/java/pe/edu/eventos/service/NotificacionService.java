package pe.edu.eventos.service;

import pe.edu.eventos.dao.NotificacionDAO;
import pe.edu.eventos.dao.impl.NotificacionDAOImpl;
import pe.edu.eventos.model.Notificacion;

import java.util.Collections;
import java.util.List;

/**
 * Avisos a clientes y personal.
 * Regla principal: un aviso NUNCA debe romper la operación que lo origina. Por eso los métodos
 * avisarCliente / avisarPersonal capturan cualquier error y solo lo escriben en la consola.
 */
public class NotificacionService {

    public static final int LIMITE_LISTA = 50;

    private final NotificacionDAO dao;

    public NotificacionService() {
        this(new NotificacionDAOImpl());
    }

    public NotificacionService(NotificacionDAO dao) {
        this.dao = dao;
    }

    /** Avisa al cliente titular del evento. Plantilla con {evento} y {cliente}. */
    public void avisarCliente(Integer idEvento, String tipo, String plantilla) {
        if (idEvento == null || plantilla == null || plantilla.isBlank()) return;
        try {
            dao.avisarCliente(idEvento, tipo, plantilla);
        } catch (Throwable e) {
            System.err.println("No se pudo avisar al cliente del evento " + idEvento + ": " + e);
        }
    }

    /** Avisa a los administradores y al coordinador del evento. Plantilla con {evento} y {cliente}. */
    public void avisarPersonal(Integer idEvento, String tipo, String plantilla) {
        if (idEvento == null || plantilla == null || plantilla.isBlank()) return;
        try {
            dao.avisarPersonal(idEvento, tipo, plantilla);
        } catch (Throwable e) {
            System.err.println("No se pudo avisar al personal del evento " + idEvento + ": " + e);
        }
    }

    public List<Notificacion> listar(int idUsuario, boolean soloNoLeidas) {
        return dao.listarPorUsuario(idUsuario, soloNoLeidas, LIMITE_LISTA);
    }

    public int contarNoLeidas(int idUsuario) {
        try {
            return dao.contarNoLeidas(idUsuario);
        } catch (Throwable e) {
            return 0; // la campana no debe tumbar la página
        }
    }

    public Notificacion buscar(int idNotificacion, int idUsuario) {
        return dao.buscarPorId(idNotificacion, idUsuario);
    }

    public boolean marcarLeida(int idNotificacion, int idUsuario) {
        return dao.marcarLeida(idNotificacion, idUsuario);
    }

    public int marcarTodasLeidas(int idUsuario) {
        return dao.marcarTodasLeidas(idUsuario);
    }

    /** Pantalla a la que lleva el aviso (ruta relativa a la aplicación). */
    public static String rutaDestino(String tipo, Integer idEvento, boolean esPersonal) {
        String t = tipo == null ? "" : tipo;
        if (esPersonal) {
            if (idEvento == null) return "/empleado/dashboard.jsp";
            switch (t) {
                case Notificacion.COTIZACION: return "/cotizacion?idEvento=" + idEvento;
                case Notificacion.ACUERDO: return "/acuerdo?idEvento=" + idEvento;
                case Notificacion.PAGO: return "/pago?idEvento=" + idEvento;
                case Notificacion.SEGUIMIENTO: return "/seguimiento?idEvento=" + idEvento;
                default: return "/empleado/dashboard.jsp";
            }
        }
        switch (t) {
            case Notificacion.COTIZACION: return "/mis-cotizaciones";
            case Notificacion.ACUERDO: return "/mis-acuerdos";
            default: return idEvento == null ? "/mi-evento" : "/mi-evento?id=" + idEvento;
        }
    }

    /** Lista vacía cuando falla algo al armar la pantalla. */
    public List<Notificacion> vacia() {
        return Collections.emptyList();
    }
}
