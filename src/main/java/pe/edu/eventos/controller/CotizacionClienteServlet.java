package pe.edu.eventos.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pe.edu.eventos.dto.UsuarioDTO;
import pe.edu.eventos.facade.CotizacionFacade;
import pe.edu.eventos.util.Constantes;

import java.io.IOException;

/**
 * Cotizaciones del cliente: ve las de sus eventos y acepta o rechaza las pendientes.
 * GET  /mis-cotizaciones
 * POST accion = aceptar | rechazar (id = cotización)
 * Solo para CLIENTE.
 */
@WebServlet("/mis-cotizaciones")
public class CotizacionClienteServlet extends HttpServlet {

    private static final String VISTA = "/cliente/cotizaciones.jsp";
    private static final String FLASH_EXITO = "flashExito";
    private static final String FLASH_ERROR = "flashError";

    private final CotizacionFacade facade = new CotizacionFacade();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UsuarioDTO usuario = obtenerCliente(request, response);
        if (usuario == null) return;

        HttpSession session = request.getSession(false);
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

        request.setAttribute("cotizaciones", facade.listarDelCliente(usuario.getId()));
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UsuarioDTO usuario = obtenerCliente(request, response);
        if (usuario == null) return;

        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");
        boolean aceptar = "aceptar".equals(accion);

        if (aceptar || "rechazar".equals(accion)) {
            String error;
            try {
                error = facade.responderComoCliente(usuario.getId(), Integer.parseInt(request.getParameter("id")), aceptar);
            } catch (NumberFormatException e) {
                error = "No se indicó la cotización.";
            }
            HttpSession session = request.getSession();
            if (error != null) {
                session.setAttribute(FLASH_ERROR, error);
            } else {
                session.setAttribute(FLASH_EXITO, aceptar
                        ? "¡Gracias! Aceptaste la cotización. Nuestro equipo se pondrá en contacto contigo para el acuerdo."
                        : "Rechazaste la cotización. Si quieres ajustarla, nuestro equipo puede prepararte una nueva.");
            }
        }
        response.sendRedirect(request.getContextPath() + "/mis-cotizaciones");
    }

    private UsuarioDTO obtenerCliente(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        UsuarioDTO usuario = session != null ? (UsuarioDTO) session.getAttribute(Constantes.SESION_USUARIO) : null;
        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        String rol = usuario.getRol() != null ? usuario.getRol().toUpperCase() : "";
        if (!Constantes.ROL_CLIENTE.equals(rol)) {
            response.sendRedirect(request.getContextPath() + "/empleado/dashboard.jsp");
            return null;
        }
        return usuario;
    }
}
