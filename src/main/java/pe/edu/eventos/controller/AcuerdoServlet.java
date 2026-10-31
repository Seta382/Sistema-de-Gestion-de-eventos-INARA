package pe.edu.eventos.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pe.edu.eventos.dto.PanelAcuerdoDTO;
import pe.edu.eventos.dto.UsuarioDTO;
import pe.edu.eventos.facade.AcuerdoFacade;
import pe.edu.eventos.model.Acuerdo;
import pe.edu.eventos.util.BusquedaUtil;
import pe.edu.eventos.util.Constantes;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Acuerdos (personal).
 * GET  /acuerdo                              listado con filtro por estado y búsqueda
 * GET  /acuerdo?idEvento=N                   pantalla de acuerdo de un evento
 * GET  /acuerdo?idEvento=N&editar=ID         ídem, con el formulario de edición abierto
 * POST accion = crear | actualizar | aceptar | cancelar
 * Solo para ADMIN y EMPLEADO.
 */
@WebServlet("/acuerdo")
public class AcuerdoServlet extends HttpServlet {

    private static final String VISTA_EVENTO = "/acuerdo/evento.jsp";
    private static final String VISTA_LISTADO = "/acuerdo/listado.jsp";
    private static final String FLASH_EXITO = "flashExito";
    private static final String FLASH_ERROR = "flashError";

    private final AcuerdoFacade facade = new AcuerdoFacade();

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
                response.sendRedirect(request.getContextPath() + "/acuerdo");
                return;
            }
            Integer idEditar = leerId(request.getParameter("editar"));
            if (idEditar != null) {
                Acuerdo a = facade.buscarPorId(idEditar);
                if (a != null && a.isPendiente() && idEvento.equals(a.getIdEvento())) {
                    request.setAttribute("acuerdoEdit", a);
                }
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
            case "crear":
                crear(request, response);
                break;
            case "actualizar":
                actualizar(request, response);
                break;
            case "aceptar":
                responder(request, response, facade.aceptarComoPersonal(idOError(request)),
                        "Acuerdo #" + request.getParameter("id") + " marcado como aceptado. El evento quedó confirmado.");
                break;
            case "cancelar":
                responder(request, response, facade.cancelar(idOError(request)),
                        "Acuerdo #" + request.getParameter("id") + " cancelado.");
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/acuerdo");
                break;
        }
    }

    // ---------------------------------------------------------------- acciones

    private void crear(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer idEvento = leerId(request.getParameter("idEvento"));
        if (idEvento == null) {
            setFlash(request, FLASH_ERROR, "No se indicó el evento del acuerdo.");
            response.sendRedirect(request.getContextPath() + "/acuerdo");
            return;
        }

        String condiciones = request.getParameter("condiciones");
        String error = facade.crear(idEvento, condiciones);
        if (error != null) {
            if (!cargarPanel(request, idEvento)) {
                setFlash(request, FLASH_ERROR, "El evento indicado no existe.");
                response.sendRedirect(request.getContextPath() + "/acuerdo");
                return;
            }
            request.setAttribute("error", error);
            request.setAttribute("condicionesForm", condiciones);
            request.getRequestDispatcher(VISTA_EVENTO).forward(request, response);
            return;
        }

        setFlash(request, FLASH_EXITO, "Acuerdo creado. El cliente podrá revisarlo y aceptarlo desde su portal.");
        response.sendRedirect(request.getContextPath() + "/acuerdo?idEvento=" + idEvento);
    }

    private void actualizar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer id = leerId(request.getParameter("id"));
        Integer idEvento = leerId(request.getParameter("idEvento"));
        if (id == null || idEvento == null) {
            setFlash(request, FLASH_ERROR, "No se indicó el acuerdo a editar.");
            response.sendRedirect(request.getContextPath() + "/acuerdo");
            return;
        }

        String condiciones = request.getParameter("condiciones");
        String error = facade.actualizarCondiciones(id, condiciones);
        if (error != null) {
            Acuerdo actual = facade.buscarPorId(id);
            if (actual == null || !cargarPanel(request, idEvento)) {
                setFlash(request, FLASH_ERROR, error);
                response.sendRedirect(request.getContextPath() + "/acuerdo?idEvento=" + idEvento);
                return;
            }
            request.setAttribute("error", error);
            request.setAttribute("acuerdoEdit", actual);
            request.setAttribute("condicionesForm", condiciones);
            request.getRequestDispatcher(VISTA_EVENTO).forward(request, response);
            return;
        }

        setFlash(request, FLASH_EXITO, "Condiciones del acuerdo #" + id + " actualizadas.");
        response.sendRedirect(request.getContextPath() + "/acuerdo?idEvento=" + idEvento);
    }

    private void responder(HttpServletRequest request, HttpServletResponse response,
                           String error, String mensajeExito) throws IOException {
        if (error != null) {
            setFlash(request, FLASH_ERROR, error);
        } else {
            setFlash(request, FLASH_EXITO, mensajeExito);
        }

        // Se vuelve al mismo sitio desde donde se respondió
        Integer idEvento = leerId(request.getParameter("idEvento"));
        if (idEvento != null) {
            response.sendRedirect(request.getContextPath() + "/acuerdo?idEvento=" + idEvento);
        } else {
            StringBuilder url = new StringBuilder(request.getContextPath() + "/acuerdo?filtro=" + codificar(leerFiltro(request)));
            String q = BusquedaUtil.normalizar(request.getParameter("q"));
            if (!q.isEmpty()) {
                url.append("&q=").append(codificar(q));
            }
            response.sendRedirect(url.toString());
        }
    }

    /** Id del acuerdo del formulario; 0 si falta (el service responde "no existe"). */
    private int idOError(HttpServletRequest request) {
        Integer id = leerId(request.getParameter("id"));
        return id == null ? 0 : id;
    }

    // ------------------------------------------------------------------ vista

    private boolean cargarPanel(HttpServletRequest request, int idEvento) {
        PanelAcuerdoDTO panel = facade.armarPanel(idEvento);
        if (panel == null) return false;
        request.setAttribute("panel", panel);
        return true;
    }

    private void mostrarListado(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String q = BusquedaUtil.normalizar(request.getParameter("q"));
        String filtro = leerFiltro(request);

        request.setAttribute("busqueda", q);
        request.setAttribute("filtro", filtro);
        request.setAttribute("qParamHtml", q.isEmpty() ? "" : "&amp;q=" + codificar(q));
        request.setAttribute("acuerdos", facade.buscar(q, filtro));
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

    /** PENDIENTE (por defecto), ACEPTADO, CANCELADO o TODOS. */
    private String leerFiltro(HttpServletRequest request) {
        String f = request.getParameter("filtro");
        if (f == null) return "PENDIENTE";
        f = f.trim().toUpperCase();
        switch (f) {
            case "PENDIENTE":
            case "ACEPTADO":
            case "CANCELADO":
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
