package pe.edu.eventos.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pe.edu.eventos.dao.EventoDAO;
import pe.edu.eventos.dao.impl.EventoDAOImpl;
import pe.edu.eventos.dto.UsuarioDTO;
import pe.edu.eventos.model.Evento;
import pe.edu.eventos.model.Seguimiento;
import pe.edu.eventos.service.SeguimientoService;
import pe.edu.eventos.util.Constantes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Seguimiento de un evento (personal): avances que luego ve el cliente en "Mi evento".
 * GET  /seguimiento?idEvento=N                 lista y formulario
 * GET  /seguimiento?idEvento=N&editar=ID       ídem, con el formulario de edición abierto
 * POST accion = registrar | actualizar | eliminar
 * Solo para ADMIN y EMPLEADO.
 */
@WebServlet("/seguimiento")
public class SeguimientoServlet extends HttpServlet {

    private static final String VISTA = "/seguimiento/evento.jsp";
    private static final String FLASH_EXITO = "flashExito";
    private static final String FLASH_ERROR = "flashError";

    private final SeguimientoService service = new SeguimientoService();
    private final EventoDAO eventoDAO = new EventoDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!esStaff(request, response)) return;
        pasarMensajesFlashALaVista(request);

        Integer idEvento = leerId(request.getParameter("idEvento"));
        if (idEvento == null || !cargarVista(request, idEvento)) {
            setFlash(request, FLASH_ERROR, "Elige un evento para ver su seguimiento.");
            response.sendRedirect(request.getContextPath() + "/empleado/eventos.jsp");
            return;
        }
        Integer idEditar = leerId(request.getParameter("editar"));
        if (idEditar != null) {
            Seguimiento s = service.buscarPorId(idEditar);
            if (s != null && idEvento.equals(s.getIdEvento())) {
                request.setAttribute("avanceEdit", s);
            }
        }
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!esStaff(request, response)) return;
        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");
        if (accion == null) accion = "";

        Integer idEvento = leerId(request.getParameter("idEvento"));
        if (idEvento == null) {
            setFlash(request, FLASH_ERROR, "No se indicó el evento.");
            response.sendRedirect(request.getContextPath() + "/empleado/eventos.jsp");
            return;
        }
        Integer id = leerId(request.getParameter("id"));
        String titulo = request.getParameter("titulo");
        String descripcion = request.getParameter("descripcion");

        switch (accion) {
            case "registrar": {
                String error = service.registrar(idEvento, titulo, descripcion);
                if (error != null) {
                    volverConError(request, response, idEvento, error, titulo, descripcion, null);
                    return;
                }
                setFlash(request, FLASH_EXITO, "Avance registrado. El cliente ya puede verlo en su portal.");
                break;
            }
            case "actualizar": {
                String error = service.actualizar(id == null ? 0 : id, titulo, descripcion);
                if (error != null) {
                    volverConError(request, response, idEvento, error, titulo, descripcion,
                            id == null ? null : service.buscarPorId(id));
                    return;
                }
                setFlash(request, FLASH_EXITO, "Avance actualizado.");
                break;
            }
            case "eliminar": {
                String error = service.eliminar(id == null ? 0 : id);
                setFlash(request, error == null ? FLASH_EXITO : FLASH_ERROR, error == null ? "Avance eliminado." : error);
                break;
            }
            default:
                break;
        }
        response.sendRedirect(request.getContextPath() + "/seguimiento?idEvento=" + idEvento);
    }

    private void volverConError(HttpServletRequest request, HttpServletResponse response, int idEvento,
                                String error, String titulo, String descripcion, Seguimiento edit)
            throws ServletException, IOException {
        if (!cargarVista(request, idEvento)) {
            setFlash(request, FLASH_ERROR, error);
            response.sendRedirect(request.getContextPath() + "/empleado/eventos.jsp");
            return;
        }
        Map<String, String> form = new HashMap<>();
        form.put("titulo", titulo == null ? "" : titulo);
        form.put("descripcion", descripcion == null ? "" : descripcion);
        request.setAttribute("form", form);
        if (edit != null) request.setAttribute("avanceEdit", edit);
        request.setAttribute("error", error);
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    private boolean cargarVista(HttpServletRequest request, int idEvento) {
        Evento evento = eventoDAO.buscarPorId(idEvento);
        if (evento == null) return false;
        request.setAttribute("evento", evento);
        request.setAttribute("avances", service.listarPorEvento(idEvento));
        request.setAttribute("motivoBloqueo", service.motivoNoAdmiteAvances(evento));
        return true;
    }

    private Integer leerId(String texto) {
        try {
            return Integer.valueOf(texto.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    private void setFlash(HttpServletRequest request, String clave, String mensaje) {
        request.getSession().setAttribute(clave, mensaje);
    }

    private void pasarMensajesFlashALaVista(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return;
        Object exito = session.getAttribute(FLASH_EXITO);
        if (exito != null) {
            request.setAttribute("exito", exito);
            session.removeAttribute(FLASH_EXITO);
        }
        Object error = session.getAttribute(FLASH_ERROR);
        if (error != null) {
            request.setAttribute("error", error);
            session.removeAttribute(FLASH_ERROR);
        }
    }

    private boolean esStaff(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        UsuarioDTO usuario = session != null ? (UsuarioDTO) session.getAttribute(Constantes.SESION_USUARIO) : null;
        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        String rol = usuario.getRol() != null ? usuario.getRol().toUpperCase() : "";
        if (!Constantes.ROL_ADMIN.equals(rol) && !Constantes.ROL_EMPLEADO.equals(rol)) {
            response.sendRedirect(request.getContextPath() + "/cliente/dashboard.jsp");
            return false;
        }
        return true;
    }
}
