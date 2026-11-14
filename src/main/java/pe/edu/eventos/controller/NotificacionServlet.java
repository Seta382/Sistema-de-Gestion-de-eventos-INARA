package pe.edu.eventos.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pe.edu.eventos.dto.UsuarioDTO;
import pe.edu.eventos.model.Notificacion;
import pe.edu.eventos.service.NotificacionService;
import pe.edu.eventos.util.Constantes;

import java.io.IOException;

/**
 * Notificaciones del usuario (cliente o personal).
 * GET  /notificaciones                   lista (filtro = TODAS | SIN_LEER)
 * GET  /notificaciones?accion=contar     {"noLeidas":N} para la campana
 * POST accion = abrir (marca leída y va a la pantalla del aviso) | leer | todas
 */
@WebServlet("/notificaciones")
public class NotificacionServlet extends HttpServlet {

    private static final String VISTA = "/notificacion/listado.jsp";

    private final NotificacionService service = new NotificacionService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UsuarioDTO usuario = usuarioActual(request);

        if ("contar".equals(request.getParameter("accion"))) {
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Cache-Control", "no-store");
            if (usuario == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().print("{\"noLeidas\":0}");
            } else {
                response.getWriter().print("{\"noLeidas\":" + service.contarNoLeidas(usuario.getId()) + "}");
            }
            return;
        }

        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String filtro = leerFiltro(request);
        request.setAttribute("filtro", filtro);
        request.setAttribute("notificaciones", service.listar(usuario.getId(), "SIN_LEER".equals(filtro)));
        request.setAttribute("totalNoLeidas", service.contarNoLeidas(usuario.getId()));
        request.setAttribute("esPersonal", esPersonal(usuario));
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UsuarioDTO usuario = usuarioActual(request);
        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");
        if (accion == null) accion = "";
        Integer id = leerId(request.getParameter("id"));
        String volver = request.getContextPath() + "/notificaciones?filtro=" + leerFiltro(request);

        switch (accion) {
            case "abrir": {
                Notificacion n = id == null ? null : service.buscar(id, usuario.getId());
                if (n != null) {
                    service.marcarLeida(n.getIdNotificacion(), usuario.getId());
                    response.sendRedirect(request.getContextPath()
                            + NotificacionService.rutaDestino(n.getTipo(), n.getIdEvento(), esPersonal(usuario)));
                    return;
                }
                break;
            }
            case "leer":
                if (id != null) service.marcarLeida(id, usuario.getId());
                break;
            case "todas":
                service.marcarTodasLeidas(usuario.getId());
                break;
            default:
                break;
        }
        response.sendRedirect(volver);
    }

    private UsuarioDTO usuarioActual(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null ? (UsuarioDTO) session.getAttribute(Constantes.SESION_USUARIO) : null;
    }

    private boolean esPersonal(UsuarioDTO usuario) {
        String rol = usuario.getRol() != null ? usuario.getRol().toUpperCase() : "";
        return Constantes.ROL_ADMIN.equals(rol) || Constantes.ROL_EMPLEADO.equals(rol);
    }

    /** TODAS (por defecto) o SIN_LEER. */
    private String leerFiltro(HttpServletRequest request) {
        String f = request.getParameter("filtro");
        return "SIN_LEER".equalsIgnoreCase(f == null ? "" : f.trim()) ? "SIN_LEER" : "TODAS";
    }

    private Integer leerId(String texto) {
        try {
            return Integer.valueOf(texto.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }
}
