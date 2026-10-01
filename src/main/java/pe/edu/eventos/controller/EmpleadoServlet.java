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

        UsuarioDTO sesion = obtenerUsuarioSiEsAdmin(request, response);
        if (sesion == null) return;

        String accion = request.getParameter("accion");
        if (accion == null) {
            accion = "listar";
        }

        switch (accion.toLowerCase()) {
            case "registro":
                request.getRequestDispatcher("/empleado/registro.jsp").forward(request, response);
                break;

            case "editar": {
                int idEmpleado = Integer.parseInt(request.getParameter("id"));
                Empleado empleado = empleadoService.buscarPorId(idEmpleado);
                if (empleado == null) {
                    response.sendRedirect(request.getContextPath() + "/empleado?accion=listar");
                    return;
                }
                request.setAttribute("empleado", empleado);
                request.getRequestDispatcher("/empleado/editar.jsp").forward(request, response);
                break;
            }

            case "baja": {
                int idUsuario = Integer.parseInt(request.getParameter("idUsuario"));

                // FIX: un admin no puede darse de baja a sí mismo
                if (sesion.getId() != null && sesion.getId() == idUsuario) {
                    request.setAttribute("errorGlobal", "No puedes darte de baja a ti mismo.");
                    request.setAttribute("listaPersonal", empleadoService.listarTodos());
                    request.getRequestDispatcher("/empleado/listado.jsp").forward(request, response);
                    return;
                }

                empleadoService.darDeBaja(idUsuario);
                response.sendRedirect(request.getContextPath() + "/empleado?accion=listar");
                break;
            }

            case "reactivar": {
                int idUsuario = Integer.parseInt(request.getParameter("idUsuario"));
                empleadoService.reactivar(idUsuario);
                response.sendRedirect(request.getContextPath() + "/empleado?accion=listar");
                break;
            }

            case "listar":
            default:
                request.setAttribute("listaPersonal", empleadoService.listarTodos());
                request.getRequestDispatcher("/empleado/listado.jsp").forward(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UsuarioDTO sesion = obtenerUsuarioSiEsAdmin(request, response);
        if (sesion == null) return;

        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");

        if ("registrar".equalsIgnoreCase(accion)) {
            procesarRegistro(request, response);
        } else if ("actualizar".equalsIgnoreCase(accion)) {
            procesarActualizacion(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/empleado?accion=listar");
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
            request.setAttribute("nombre", nombre);
            request.setAttribute("apellido", apellido);
            request.setAttribute("correo", correo);
            request.setAttribute("telefono", telefono);
            request.setAttribute("dni", dni);
            request.setAttribute("cargo", cargo);
            request.setAttribute("area", area);
            request.getRequestDispatcher("/empleado/registro.jsp").forward(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/empleado?accion=listar");
    }

    private void procesarActualizacion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int idUsuario = Integer.parseInt(request.getParameter("idUsuario"));
        int idEmpleado = Integer.parseInt(request.getParameter("idEmpleado"));

        String nombre = request.getParameter("nombre");
        String apellido = request.getParameter("apellido");
        String telefono = request.getParameter("telefono");
        String dni = request.getParameter("dni");
        String cargo = request.getParameter("cargo");
        String area = request.getParameter("area");

        Usuario usuario = new Usuario();
        usuario.setId(idUsuario);
        usuario.setNombre(nombre != null ? nombre.trim() : null);
        usuario.setApellido(apellido != null ? apellido.trim() : null);
        usuario.setTelefono(telefono != null ? telefono.trim() : null);

        Empleado empleado = new Empleado(idEmpleado, idUsuario, dni, cargo, area);

        String error = empleadoService.actualizar(usuario, empleado);

        if (error != null) {
            request.setAttribute("error", error);
            Empleado empleadoConError = empleadoService.buscarPorId(idEmpleado);
            request.setAttribute("empleado", empleadoConError);
            request.getRequestDispatcher("/empleado/editar.jsp").forward(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/empleado?accion=listar");
    }

    /**
     * Devuelve el UsuarioDTO de sesión si es ADMIN; si no, redirige la
     * respuesta (a /login o al dashboard) y devuelve null.
     */
    private UsuarioDTO obtenerUsuarioSiEsAdmin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        UsuarioDTO usuario = session != null ? (UsuarioDTO) session.getAttribute(Constantes.SESION_USUARIO) : null;

        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }

        String rol = usuario.getRol() != null ? usuario.getRol().toUpperCase() : "";
        if (!Constantes.ROL_ADMIN.equals(rol)) {
            response.sendRedirect(request.getContextPath() + "/empleado/dashboard.jsp");
            return null;
        }
        return usuario;
    }
}
