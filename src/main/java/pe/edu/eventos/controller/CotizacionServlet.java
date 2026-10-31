package pe.edu.eventos.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pe.edu.eventos.dto.PanelCotizacionDTO;
import pe.edu.eventos.dto.UsuarioDTO;
import pe.edu.eventos.facade.CotizacionFacade;
import pe.edu.eventos.util.BusquedaUtil;
import pe.edu.eventos.util.Constantes;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cotizaciones (personal).
 * GET  /cotizacion                      listado con filtro por estado y búsqueda
 * GET  /cotizacion?idEvento=N           pantalla de cotización de un evento
 * POST accion = crear | aceptar | rechazar
 * Solo para ADMIN y EMPLEADO.
 */
@WebServlet("/cotizacion")
public class CotizacionServlet extends HttpServlet {

    private static final String VISTA_EVENTO = "/cotizacion/evento.jsp";
    private static final String VISTA_LISTADO = "/cotizacion/listado.jsp";
    private static final String FLASH_EXITO = "flashExito";
    private static final String FLASH_ERROR = "flashError";
    private static final String PREFIJO_CANTIDAD = "cantidad_";

    private final CotizacionFacade facade = new CotizacionFacade();

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
                response.sendRedirect(request.getContextPath() + "/cotizacion");
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
            case "crear":
                crear(request, response);
                break;
            case "aceptar":
            case "rechazar":
                responder(request, response, "aceptar".equals(accion));
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/cotizacion");
                break;
        }
    }

    // ---------------------------------------------------------------- acciones

    private void crear(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer idEvento = leerId(request.getParameter("idEvento"));
        if (idEvento == null) {
            setFlash(request, FLASH_ERROR, "No se indicó el evento a cotizar.");
            response.sendRedirect(request.getContextPath() + "/cotizacion");
            return;
        }

        // Lo que escribió el usuario, para volver a mostrarlo si hay un error
        Map<String, String> form = new HashMap<>();
        form.put("descuento", request.getParameter("descuento"));
        form.put("observaciones", request.getParameter("observaciones"));
        Map<Integer, Integer> cantidades = new LinkedHashMap<>();
        String error = null;
        try {
            cantidades = leerCantidades(request, form);
        } catch (IllegalArgumentException e) {
            error = e.getMessage();
        }

        if (error == null) {
            try {
                error = facade.crear(idEvento, cantidades, leerDescuento(request.getParameter("descuento")),
                        request.getParameter("observaciones"));
            } catch (IllegalArgumentException e) {
                error = e.getMessage();
            }
        }

        if (error != null) {
            if (!cargarPanel(request, idEvento)) {
                setFlash(request, FLASH_ERROR, "El evento indicado no existe.");
                response.sendRedirect(request.getContextPath() + "/cotizacion");
                return;
            }
            request.setAttribute("error", error);
            request.setAttribute("form", form);
            request.setAttribute("abrirForm", "cotizacion");
            request.getRequestDispatcher(VISTA_EVENTO).forward(request, response);
            return;
        }

        setFlash(request, FLASH_EXITO, "Cotización creada. Quedó pendiente de la respuesta del cliente; "
                + "si había otra pendiente, pasó a «Modificada».");
        response.sendRedirect(request.getContextPath() + "/cotizacion?idEvento=" + idEvento);
    }

    private void responder(HttpServletRequest request, HttpServletResponse response, boolean aceptar)
            throws IOException {

        Integer id = leerId(request.getParameter("id"));
        String error = id == null ? "No se indicó la cotización." : facade.responderComoPersonal(id, aceptar);

        if (error != null) {
            setFlash(request, FLASH_ERROR, error);
        } else {
            setFlash(request, FLASH_EXITO, aceptar
                    ? "Cotización #" + id + " marcada como aceptada. El siguiente paso es el acuerdo con el cliente."
                    : "Cotización #" + id + " marcada como rechazada.");
        }

        // Se vuelve al mismo sitio desde donde se respondió
        Integer idEvento = leerId(request.getParameter("idEvento"));
        if (idEvento != null) {
            response.sendRedirect(request.getContextPath() + "/cotizacion?idEvento=" + idEvento);
        } else {
            redirigirListado(request, response);
        }
    }

    // ------------------------------------------------------------------ vista

    private boolean cargarPanel(HttpServletRequest request, int idEvento) {
        PanelCotizacionDTO panel = facade.armarPanel(idEvento);
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
        request.setAttribute("cotizaciones", facade.buscar(q, filtro));
        request.setAttribute("totalPendientes", facade.contarPendientes());
        request.getRequestDispatcher(VISTA_LISTADO).forward(request, response);
    }

    // ---------------------------------------------------------------- helpers

    /** Lee los campos cantidad_<idServicio>; los vacíos o en 0 no cuentan como incluidos. */
    private Map<Integer, Integer> leerCantidades(HttpServletRequest request, Map<String, String> form) {
        Map<Integer, Integer> cantidades = new LinkedHashMap<>();
        Enumeration<String> nombres = request.getParameterNames();
        while (nombres.hasMoreElements()) {
            String nombre = nombres.nextElement();
            if (!nombre.startsWith(PREFIJO_CANTIDAD)) continue;

            String valor = request.getParameter(nombre);
            form.put(nombre, valor); // se conserva tal cual para volver a mostrarlo
            if (valor == null || valor.trim().isEmpty()) continue;

            Integer idServicio = leerId(nombre.substring(PREFIJO_CANTIDAD.length()));
            if (idServicio == null) {
                throw new IllegalArgumentException("Uno de los servicios enviados no es válido.");
            }
            int cantidad;
            try {
                cantidad = Integer.parseInt(valor.trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Las cantidades deben ser números enteros.");
            }
            if (cantidad == 0) continue;
            cantidades.put(idServicio, cantidad);
        }
        return cantidades;
    }

    private BigDecimal leerDescuento(String texto) {
        if (texto == null || texto.trim().isEmpty()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El descuento no es un número válido.");
        }
    }

    private Integer leerId(String texto) {
        try {
            return Integer.valueOf(texto.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    /** PENDIENTE (por defecto), ACEPTADA, RECHAZADA, MODIFICADA o TODAS. */
    private String leerFiltro(HttpServletRequest request) {
        String f = request.getParameter("filtro");
        if (f == null) return "PENDIENTE";
        f = f.trim().toUpperCase();
        switch (f) {
            case "PENDIENTE":
            case "ACEPTADA":
            case "RECHAZADA":
            case "MODIFICADA":
            case "TODAS":
                return f;
            default:
                return "PENDIENTE";
        }
    }

    private void redirigirListado(HttpServletRequest request, HttpServletResponse response) throws IOException {
        StringBuilder url = new StringBuilder(request.getContextPath() + "/cotizacion?filtro=" + codificar(leerFiltro(request)));
        String q = BusquedaUtil.normalizar(request.getParameter("q"));
        if (!q.isEmpty()) {
            url.append("&q=").append(codificar(q));
        }
        response.sendRedirect(url.toString());
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
