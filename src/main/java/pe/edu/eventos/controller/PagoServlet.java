package pe.edu.eventos.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pe.edu.eventos.dto.PanelPagoDTO;
import pe.edu.eventos.dto.UsuarioDTO;
import pe.edu.eventos.facade.PagoFacade;
import pe.edu.eventos.model.Pago;
import pe.edu.eventos.util.BusquedaUtil;
import pe.edu.eventos.util.Constantes;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Pagos (personal).
 * GET  /pago                  listado con filtro por estado y búsqueda
 * GET  /pago?idEvento=N       pagos de un evento
 * POST accion = registrar | confirmar | rechazar
 * Solo para ADMIN y EMPLEADO.
 */
@WebServlet("/pago")
public class PagoServlet extends HttpServlet {

    private static final String VISTA_EVENTO = "/pago/evento.jsp";
    private static final String VISTA_LISTADO = "/pago/listado.jsp";
    private static final String FLASH_EXITO = "flashExito";
    private static final String FLASH_ERROR = "flashError";

    private final PagoFacade facade = new PagoFacade();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!esStaff(request, response)) return;
        pasarMensajesFlashALaVista(request);

        String idEventoTxt = request.getParameter("idEvento");
        if (idEventoTxt != null && !idEventoTxt.trim().isEmpty()) {
            Integer idEvento = leerId(idEventoTxt);
            if (idEvento == null || !cargarPanel(request, idEvento)) {
                setFlash(request, FLASH_ERROR, "El evento indicado no existe.");
                response.sendRedirect(request.getContextPath() + "/pago");
                return;
            }
            request.getRequestDispatcher(VISTA_EVENTO).forward(request, response);
            return;
        }
        mostrarListado(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!esStaff(request, response)) return;
        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");
        if (accion == null) accion = "";

        switch (accion) {
            case "registrar":
                registrar(request, response);
                break;
            case "confirmar":
                responder(request, response, facade.confirmar(idOError(request)),
                        "Pago #" + request.getParameter("id") + " confirmado.");
                break;
            case "rechazar":
                responder(request, response, facade.rechazar(idOError(request)),
                        "Pago #" + request.getParameter("id") + " rechazado.");
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/pago");
                break;
        }
    }

    // ---------------------------------------------------------------- acciones

    private void registrar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer idEvento = leerId(request.getParameter("idEvento"));
        if (idEvento == null) {
            setFlash(request, FLASH_ERROR, "No se indicó el evento del pago.");
            response.sendRedirect(request.getContextPath() + "/pago");
            return;
        }

        String monto = request.getParameter("monto");
        String metodo = request.getParameter("metodo");
        String estado = request.getParameter("estado");
        String comprobante = request.getParameter("comprobante");

        String error = facade.registrar(idEvento, monto, metodo, estado, comprobante);
        if (error != null) {
            if (!cargarPanel(request, idEvento)) {
                setFlash(request, FLASH_ERROR, "El evento indicado no existe.");
                response.sendRedirect(request.getContextPath() + "/pago");
                return;
            }
            Map<String, String> form = new HashMap<>();
            form.put("monto", monto == null ? "" : monto);
            form.put("metodo", metodo == null ? "" : metodo);
            form.put("estado", estado == null ? "" : estado);
            form.put("comprobante", comprobante == null ? "" : comprobante);
            request.setAttribute("form", form);
            request.setAttribute("error", error);
            request.getRequestDispatcher(VISTA_EVENTO).forward(request, response);
            return;
        }

        setFlash(request, FLASH_EXITO, "Pago registrado correctamente.");
        response.sendRedirect(request.getContextPath() + "/pago?idEvento=" + idEvento);
    }

    private void responder(HttpServletRequest request, HttpServletResponse response,
                           String error, String mensajeExito) throws IOException {
        if (error != null) {
            setFlash(request, FLASH_ERROR, error);
        } else {
            setFlash(request, FLASH_EXITO, mensajeExito);
        }

        Integer idEvento = leerId(request.getParameter("idEvento"));
        if (idEvento != null) {
            response.sendRedirect(request.getContextPath() + "/pago?idEvento=" + idEvento);
        } else {
            StringBuilder url = new StringBuilder(request.getContextPath() + "/pago?filtro=" + codificar(leerFiltro(request)));
            String q = BusquedaUtil.normalizar(request.getParameter("q"));
            if (!q.isEmpty()) {
                url.append("&q=").append(codificar(q));
            }
            response.sendRedirect(url.toString());
        }
    }

    private int idOError(HttpServletRequest request) {
        Integer id = leerId(request.getParameter("id"));
        return id == null ? 0 : id;
    }

    // ------------------------------------------------------------------ vista

    private boolean cargarPanel(HttpServletRequest request, int idEvento) {
        PanelPagoDTO panel = facade.armarPanel(idEvento);
        if (panel == null) return false;
        request.setAttribute("panel", panel);
        request.setAttribute("metodos", Pago.METODOS);
        return true;
    }

    private void mostrarListado(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String q = BusquedaUtil.normalizar(request.getParameter("q"));
        String filtro = leerFiltro(request);

        request.setAttribute("busqueda", q);
        request.setAttribute("filtro", filtro);
        request.setAttribute("qParamHtml", q.isEmpty() ? "" : "&amp;q=" + codificar(q));
        request.setAttribute("pagos", facade.buscar(q, filtro));
        request.setAttribute("totalPendientes", facade.contarPendientes());
        request.getRequestDispatcher(VISTA_LISTADO).forward(request, response);
    }

    // ---------------------------------------------------------------- helpers

    private Integer leerId(String texto) {
        try {
            return Integer.valueOf(texto.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    /** PENDIENTE (por defecto), COMPLETADO, RECHAZADO o TODOS. */
    private String leerFiltro(HttpServletRequest request) {
        String f = request.getParameter("filtro");
        if (f == null) return "PENDIENTE";
        f = f.trim().toUpperCase();
        switch (f) {
            case "PENDIENTE":
            case "COMPLETADO":
            case "RECHAZADO":
            case "TODOS":
                return f;
            default:
                return "PENDIENTE";
        }
    }

    private String codificar(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
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
