package pe.edu.eventos.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pe.edu.eventos.dto.MiEventoDTO;
import pe.edu.eventos.dto.UsuarioDTO;
import pe.edu.eventos.facade.MiEventoFacade;
import pe.edu.eventos.util.Constantes;

import java.io.IOException;

/**
 * "Mi evento" (cliente): estado, próximo paso, resumen de pagos y avances de su evento.
 * GET /mi-evento           el evento más próximo del cliente
 * GET /mi-evento?id=N      un evento concreto (si no es suyo se muestra el primero)
 * Solo para CLIENTE.
 */
@WebServlet("/mi-evento")
public class MiEventoServlet extends HttpServlet {

    private static final String VISTA = "/cliente/mi-evento.jsp";

    private final MiEventoFacade facade = new MiEventoFacade();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        UsuarioDTO usuario = session != null ? (UsuarioDTO) session.getAttribute(Constantes.SESION_USUARIO) : null;
        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        String rol = usuario.getRol() != null ? usuario.getRol().toUpperCase() : "";
        if (!Constantes.ROL_CLIENTE.equals(rol)) {
            response.sendRedirect(request.getContextPath() + "/empleado/dashboard.jsp");
            return;
        }

        Integer id = null;
        try {
            id = Integer.valueOf(request.getParameter("id").trim());
        } catch (NumberFormatException | NullPointerException ignorado) {
            // sin id válido: se muestra el primer evento
        }

        MiEventoDTO mi = facade.armar(usuario.getId(), id);
        request.setAttribute("mi", mi); // null = el cliente aún no tiene eventos
        request.setAttribute("cargado", Boolean.TRUE);
        request.getRequestDispatcher(VISTA).forward(request, response);
    }
}
