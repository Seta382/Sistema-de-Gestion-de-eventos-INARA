package pe.edu.eventos.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pe.edu.eventos.dto.UsuarioDTO;
import pe.edu.eventos.facade.AcuerdoFacade;
import pe.edu.eventos.util.Constantes;

import java.io.IOException;

/**
 * Acuerdos del cliente: lee las condiciones y acepta el acuerdo pendiente de sus eventos.
 * GET  /mis-acuerdos
 * POST accion = aceptar (id = acuerdo)
 * Solo para CLIENTE.
 */
@WebServlet("/mis-acuerdos")
public class AcuerdoClienteServlet extends HttpServlet {

    private static final String VISTA = "/cliente/acuerdos.jsp";
    private static final String FLASH_EXITO = "flashExito";
    private static final String FLASH_ERROR = "flashError";

    private final AcuerdoFacade facade = new AcuerdoFacade();

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

        request.setAttribute("acuerdos", facade.listarDelCliente(usuario.getId()));
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UsuarioDTO usuario = obtenerCliente(request, response);
        if (usuario == null) return;

        request.setCharacterEncoding("UTF-8");
        if ("aceptar".equals(request.getParameter("accion"))) {
            String error;
            try {
                error = facade.aceptarComoCliente(usuario.getId(), Integer.parseInt(request.getParameter("id")));
            } catch (NumberFormatException e) {
                error = "No se indicó el acuerdo.";
            }
            HttpSession session = request.getSession();
            if (error != null) {
                session.setAttribute(FLASH_ERROR, error);
            } else {
                session.setAttribute(FLASH_EXITO,
                        "¡Acuerdo aceptado! Tu evento quedó confirmado. Gracias por confiar en INARA.");
            }
        }
        response.sendRedirect(request.getContextPath() + "/mis-acuerdos");
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
