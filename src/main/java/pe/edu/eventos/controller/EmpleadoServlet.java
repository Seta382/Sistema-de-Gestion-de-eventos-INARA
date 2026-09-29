package pe.edu.eventos.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pe.edu.eventos.dto.UsuarioDTO;
import pe.edu.eventos.model.Empleado;
import pe.edu.eventos.model.Usuario;
import pe.edu.eventos.service.EmpleadoService;
import pe.edu.eventos.util.Constantes;

import java.io.IOException;

@WebServlet(name = "EmpleadoServlet", urlPatterns = {"/empleado"})
public class EmpleadoServlet extends HttpServlet {

    private final EmpleadoService empleadoService = new EmpleadoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!esAdmin(request, response)) return;

        String accion = request.getParameter("accion");
        if (accion == null) {
            accion = "registro";
        }

        switch (accion.toLowerCase()) {
            case "listar":
                request.setAttribute("listaPersonal", empleadoService.listarTodos());
                request.getRequestDispatcher("/empleado/listado.jsp").forward(request, response);
                break;
            case "registro":
            default:
                request.getRequestDispatcher("/empleado/registro.jsp").forward(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!esAdmin(request, response)) return;

        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");

        if ("registrar".equalsIgnoreCase(accion)) {
            procesarRegistro(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/empleado?accion=registro");
        }
    }

    private void procesarRegistro(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String nombre = request.getParameter("nombre");
        String apellido = request.getParameter("apellido");
        String correo = request.getParameter("correo");
        String password = request.getParameter("password");
        String telefono = request.getParameter("telefono");
        String dni = request.getParameter("dni");
        String cargo = request.getParameter("cargo");
        String area = request.getParameter("area");

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre != null ? nombre.trim() : null);
        usuario.setApellido(apellido != null ? apellido.trim() : null);
        usuario.setCorreo(correo != null ? correo.trim() : null);
        usuario.setPassword(password);
        usuario.setTelefono(telefono != null ? telefono.trim() : null);

        Empleado empleado = new Empleado(null, dni, cargo, area);

        String error = empleadoService.registrar(usuario, empleado);

        if (error != null) {
            request.setAttribute("error", error);
            preservarCampos(request, nombre, apellido, correo, telefono, dni, cargo, area);
            request.getRequestDispatcher("/empleado/registro.jsp").forward(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/empleado/dashboard.jsp?altaExitosa=1");
    }

    /**
     * Solo un ADMIN puede acceder a este Servlet. Devuelve false y ya
     * redirige la respuesta si el usuario no está autorizado.
     */
    private boolean esAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        UsuarioDTO usuario = session != null ? (UsuarioDTO) session.getAttribute(Constantes.SESION_USUARIO) : null;

        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        String rol = usuario.getRol() != null ? usuario.getRol().toUpperCase() : "";
        if (!Constantes.ROL_ADMIN.equals(rol)) {
            response.sendRedirect(request.getContextPath() + "/empleado/dashboard.jsp");
            return false;
        }
        return true;
    }

    private void preservarCampos(HttpServletRequest request, String nombre, String apellido, String correo,
                                 String telefono, String dni, String cargo, String area) {
        request.setAttribute("nombre", nombre);
        request.setAttribute("apellido", apellido);
        request.setAttribute("correo", correo);
        request.setAttribute("telefono", telefono);
        request.setAttribute("dni", dni);
        request.setAttribute("cargo", cargo);
        request.setAttribute("area", area);
    }
}
