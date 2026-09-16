package pe.edu.eventos.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String correo = request.getParameter("correo");
        String password = request.getParameter("password");

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().println("<h1>Datos recibidos</h1>");
        response.getWriter().println("<p>Correo: " + correo + "</p>");
        response.getWriter().println("<p>Contraseña: " + password + "</p>");
    }
}
