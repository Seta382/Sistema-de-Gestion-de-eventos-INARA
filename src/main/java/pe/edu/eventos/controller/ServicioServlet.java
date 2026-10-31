package pe.edu.eventos.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pe.edu.eventos.dto.UsuarioDTO;
import pe.edu.eventos.model.Servicio;
import pe.edu.eventos.service.ServicioService;
import pe.edu.eventos.util.BusquedaUtil;
import pe.edu.eventos.util.Constantes;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicios y precios de la promotora.
 * GET  /servicio?q=texto&filtro=ACTIVO|INACTIVO|TODOS
 * GET  /servicio?accion=editar&id=N  (abre el panel de edición)
 * POST accion = registrar | actualizar | baja | reactivar
 * Solo para ADMIN y EMPLEADO.
 */
@WebServlet("/servicio")
public class ServicioServlet extends HttpServlet {

    private static final String VISTA = "/servicio/registro.jsp";
    private static final String FLASH_EXITO = "flashExito";
    private static final String FLASH_ERROR = "flashError";

    private final ServicioService servicioService = new ServicioService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!esStaff(request, response)) return;
        pasarMensajesFlashALaVista(request);

        if ("editar".equals(request.getParameter("accion"))) {
            Integer id = leerId(request.getParameter("id"));
            Servicio servicio = id != null ? servicioService.buscarPorId(id) : null;
            if (servicio == null) {
                setFlash(request, FLASH_ERROR, "El servicio que intentas editar no existe.");
                redirigir(request, response, leerFiltro(request), BusquedaUtil.normalizar(request.getParameter("q")));
                return;
            }
            request.setAttribute("servicioEdit", servicio);
        }
        mostrarPagina(request, response);
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
            case "actualizar":
                actualizar(request, response);
                break;
            case "baja":
            case "reactivar":
                cambiarEstado(request, response, accion);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/servicio");
                break;
        }
    }

    // ---------------------------------------------------------------- acciones

    private void registrar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Map<String, String> form = leerForm(request);
        String error;
        String nombre = null;
        try {
            Servicio servicio = leerServicio(request);
            error = servicioService.registrar(servicio);
            nombre = servicio.getNombre();
        } catch (IllegalArgumentException e) {
            error = e.getMessage();
        }

        if (error != null) {
            request.setAttribute("error", error);
            request.setAttribute("form", form);
            request.setAttribute("abrirForm", "servicio");
            mostrarPagina(request, response);
            return;
        }

        setFlash(request, FLASH_EXITO, "Servicio «" + nombre + "» registrado correctamente. "
                + "Ya puede incluirse en las cotizaciones.");
        redirigir(request, response, "ACTIVO", "");
    }

    private void actualizar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer id = leerId(request.getParameter("id"));
        Map<String, String> form = leerForm(request);

        String error;
        String nombre = null;
        try {
            if (id == null) {
                throw new IllegalArgumentException("No se indicó qué servicio editar.");
            }
            Servicio servicio = leerServicio(request);
            servicio.setIdServicio(id);
            error = servicioService.actualizar(servicio);
            nombre = servicio.getNombre();
        } catch (IllegalArgumentException e) {
            error = e.getMessage();
        }

        if (error != null) {
            Servicio actual = id != null ? servicioService.buscarPorId(id) : null;
            if (actual == null) {
                setFlash(request, FLASH_ERROR, "El servicio que intentas editar ya no existe.");
                redirigir(request, response, leerFiltro(request), BusquedaUtil.normalizar(request.getParameter("q")));
                return;
            }
            request.setAttribute("error", error);
            request.setAttribute("formEdit", form);
            request.setAttribute("servicioEdit", actual);
            mostrarPagina(request, response);
            return;
        }

        setFlash(request, FLASH_EXITO, "Servicio «" + nombre + "» actualizado. "
                + "Las cotizaciones ya emitidas conservan el precio con el que se hicieron.");
        redirigir(request, response, leerFiltro(request), BusquedaUtil.normalizar(request.getParameter("q")));
    }

    private void cambiarEstado(HttpServletRequest request, HttpServletResponse response, String accion)
            throws IOException {

        Integer id = leerId(request.getParameter("id"));
        Servicio servicio = id != null ? servicioService.buscarPorId(id) : null;

        if (servicio == null) {
            setFlash(request, FLASH_ERROR, "El servicio indicado no existe.");
        } else if ("baja".equals(accion)) {
            if (servicioService.darDeBaja(id)) {
                setFlash(request, FLASH_EXITO, "«" + servicio.getNombre() + "» se dio de baja. "
                        + "Ya no se ofrecerá en cotizaciones nuevas; puedes reactivarlo cuando quieras.");
            } else {
                setFlash(request, FLASH_ERROR, "No se pudo dar de baja el servicio. Intenta nuevamente.");
            }
        } else {
            if (servicioService.reactivar(id)) {
                setFlash(request, FLASH_EXITO, "«" + servicio.getNombre() + "» fue reactivado.");
            } else {
                setFlash(request, FLASH_ERROR, "No se pudo reactivar el servicio. Intenta nuevamente.");
            }
        }
        redirigir(request, response, leerFiltro(request), BusquedaUtil.normalizar(request.getParameter("q")));
    }

    // ------------------------------------------------------------------ vista

    private void mostrarPagina(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String q = BusquedaUtil.normalizar(request.getParameter("q"));
        String filtro = leerFiltro(request);

        request.setAttribute("busqueda", q);
        request.setAttribute("filtro", filtro);
        request.setAttribute("qParamHtml", q.isEmpty() ? "" : "&amp;q=" + codificar(q));
        request.setAttribute("servicios", servicioService.buscar(q, filtro));

        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    // ---------------------------------------------------------------- helpers

    private Map<String, String> leerForm(HttpServletRequest request) {
        Map<String, String> form = new HashMap<>();
        form.put("nombre", request.getParameter("nombre"));
        form.put("descripcion", request.getParameter("descripcion"));
        form.put("precio", request.getParameter("precio"));
        return form;
    }

    private Servicio leerServicio(HttpServletRequest request) {
        String texto = request.getParameter("precio");
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("El precio es obligatorio.");
        }
        BigDecimal precio;
        try {
            precio = new BigDecimal(texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El precio no es un número válido.");
        }
        return new Servicio(request.getParameter("nombre"), request.getParameter("descripcion"), precio);
    }

    private Integer leerId(String texto) {
        try {
            return Integer.valueOf(texto);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** ACTIVO (por defecto), INACTIVO o TODOS. */
    private String leerFiltro(HttpServletRequest request) {
        String f = request.getParameter("filtro");
        if (f == null) return "ACTIVO";
        f = f.trim().toUpperCase();
        return ("ACTIVO".equals(f) || "INACTIVO".equals(f) || "TODOS".equals(f)) ? f : "ACTIVO";
    }

    private void redirigir(HttpServletRequest request, HttpServletResponse response,
                           String filtro, String q) throws IOException {
        StringBuilder url = new StringBuilder(request.getContextPath() + "/servicio?filtro=" + codificar(filtro));
        if (q != null && !q.isEmpty()) {
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
