package pe.edu.eventos.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pe.edu.eventos.facade.UsuarioFacade;
import pe.edu.eventos.util.Constantes;

import java.io.IOException;

@WebServlet(name = "UsuarioServlet", urlPatterns = {"/usuario", "/registro"})
public class UsuarioServlet extends HttpServlet {

    private final UsuarioFacade usuarioFacade = new UsuarioFacade();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String accion = request.getParameter("accion");
        if (accion == null) {
            accion = "registro";
        }

        switch (accion.toLowerCase()) {
            case "listar":
                request.setAttribute("listaUsuarios", usuarioFacade.listarUsuarios());
                request.getRequestDispatcher("/cliente/dashboard.jsp").forward(request, response);
                break;
            case "registro":
            default:
                request.getRequestDispatcher("/cliente/registro.jsp").forward(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");
        if (accion == null) {
            accion = "registrar";
        }

        if ("registrar".equalsIgnoreCase(accion)) {
            procesarRegistroCliente(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }

    private void procesarRegistroCliente(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String nombre = request.getParameter("nombre");
        String apellido = request.getParameter("apellido");
        String correo = request.getParameter("correo");
        String password = request.getParameter("password");
        String telefono = request.getParameter("telefono");

        // Validar campos obligatorios
        if (nombre == null || nombre.trim().isEmpty() ||
            apellido == null || apellido.trim().isEmpty() ||
            correo == null || correo.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {

            request.setAttribute("error", Constantes.MSG_CAMPOS_OBLIGATORIOS);
            preservarCampos(request, nombre, apellido, correo, telefono);
            request.getRequestDispatcher("/cliente/registro.jsp").forward(request, response);
            return;
        }

        boolean registrado = usuarioFacade.registrarCliente(
                nombre.trim(),
                apellido.trim(),
                correo.trim(),
                password,
                telefono != null ? telefono.trim() : null
        );

        if (registrado) {
            // Redirigir a login con indicador de éxito
            response.sendRedirect(request.getContextPath() + "/login?registro=exito");
        } else {
            request.setAttribute("error", Constantes.MSG_CORREO_DUPLICADO);
            preservarCampos(request, nombre, apellido, correo, telefono);
            request.getRequestDispatcher("/cliente/registro.jsp").forward(request, response);
        }
    }

    private void preservarCampos(HttpServletRequest request, String nombre, String apellido, String correo, String telefono) {
        request.setAttribute("nombre", nombre);
        request.setAttribute("apellido", apellido);
        request.setAttribute("correo", correo);
        request.setAttribute("telefono", telefono);
    }
}
