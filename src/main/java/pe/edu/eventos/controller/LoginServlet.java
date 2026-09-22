package pe.edu.eventos.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pe.edu.eventos.dto.UsuarioDTO;
import pe.edu.eventos.facade.UsuarioFacade;
import pe.edu.eventos.util.Constantes;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UsuarioFacade usuarioFacade = new UsuarioFacade();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String accion = request.getParameter("accion");
        if ("logout".equalsIgnoreCase(accion)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Si ya hay sesión activa, redirigir según rol
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(Constantes.SESION_USUARIO) != null) {
            UsuarioDTO usuario = (UsuarioDTO) session.getAttribute(Constantes.SESION_USUARIO);
            redirigirSegunRol(usuario, request, response);
            return;
        }

        request.getRequestDispatcher("/Login/Login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String correo = request.getParameter("correo");
        String password = request.getParameter("password");

        UsuarioDTO usuario = usuarioFacade.login(correo, password);

        if (usuario != null) {
            HttpSession session = request.getSession(true);
            session.setAttribute(Constantes.SESION_USUARIO, usuario);
            redirigirSegunRol(usuario, request, response);
        } else {
            request.setAttribute("error", Constantes.MSG_LOGIN_ERROR);
            request.setAttribute("correoIngresado", correo);
            request.getRequestDispatcher("/Login/Login.jsp").forward(request, response);
        }
    }

    private void redirigirSegunRol(UsuarioDTO usuario, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String rol = usuario.getRol() != null ? usuario.getRol().toUpperCase() : Constantes.ROL_CLIENTE;
        switch (rol) {
            case Constantes.ROL_ADMIN:
            case Constantes.ROL_EMPLEADO:
                response.sendRedirect(request.getContextPath() + "/empleado/dashboard.jsp");
                break;
            case Constantes.ROL_CLIENTE:
            default:
                response.sendRedirect(request.getContextPath() + "/cliente/dashboard.jsp");
                break;
        }
    }
}
