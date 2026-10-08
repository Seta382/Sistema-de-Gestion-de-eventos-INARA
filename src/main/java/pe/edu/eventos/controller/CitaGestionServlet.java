package pe.edu.eventos.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pe.edu.eventos.dto.UsuarioDTO;
import pe.edu.eventos.model.Cita;
import pe.edu.eventos.service.CitaService;
import pe.edu.eventos.util.Constantes;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Gestión de citas del lado del staff (ADMIN / EMPLEADO): listar todas,
 * confirmar, cancelar y convertir una cita confirmada en evento.
 * Es independiente de CitaServlet, que atiende solo al cliente logueado.
 */
@WebServlet(name = "CitaGestionServlet", urlPatterns = {"/citas-gestion"})
public class CitaGestionServlet extends HttpServlet {

    private final CitaService citaService = new CitaService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!esStaff(request, response)) return;

        String accion = request.getParameter("accion");
        if (accion == null) {
            accion = "listar";
        }
        String filtro = request.getParameter("estado"); // se conserva al volver al listado
        String busqueda = leerBusqueda(request);        // idem

        switch (accion.toLowerCase()) {
            case "confirmar": {
                Integer id = leerId(request);
                if (id != null) {
                    citaService.confirmar(id);
                }
                volverAlListado(request, response, filtro, busqueda);
                break;
            }

            case "cancelar": {
                Integer id = leerId(request);
                if (id != null) {
                    citaService.cancelar(id);
                }
                volverAlListado(request, response, filtro, busqueda);
                break;
            }

            case "convertir": {
                Integer id = leerId(request);
                Cita cita = id != null ? citaService.buscarPorId(id) : null;
                // Solo una cita CONFIRMADA puede convertirse en evento
                if (cita == null || !"CONFIRMADA".equals(cita.getEstado())) {
                    volverAlListado(request, response, filtro, busqueda);
                    return;
                }
                response.sendRedirect(request.getContextPath() + "/empleado/dashboard.jsp?idCita=" + id);
                break;
            }

            case "listar":
            default:
                request.setAttribute("citas", citaService.listarTodasConCliente(filtro, busqueda));
                request.setAttribute("filtroEstado", filtro != null ? filtro.toUpperCase() : "");
                request.setAttribute("busqueda", busqueda);
                // Fragmento ya codificado para armar los links de filtro/acciones en la vista
                request.setAttribute("qParamHtml", busqueda.isEmpty()
                        ? "" : "&amp;q=" + URLEncoder.encode(busqueda, StandardCharsets.UTF_8));
                request.getRequestDispatcher("/empleado/citas.jsp").forward(request, response);
                break;
        }
    }

    private Integer leerId(HttpServletRequest request) {
        try {
            return Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Texto del buscador: sin espacios sobrantes y con un tope de longitud. Nunca devuelve null. */
    private String leerBusqueda(HttpServletRequest request) {
        String q = request.getParameter("q");
        if (q == null) return "";
        q = q.trim();
        return q.length() > 100 ? q.substring(0, 100) : q;
    }

    private void volverAlListado(HttpServletRequest request, HttpServletResponse response,
                                 String filtro, String busqueda) throws IOException {
        StringBuilder url = new StringBuilder(request.getContextPath() + "/citas-gestion");
        String sep = "?";
        if (filtro != null && !filtro.trim().isEmpty()) {
            url.append(sep).append("estado=").append(URLEncoder.encode(filtro.trim(), StandardCharsets.UTF_8));
            sep = "&";
        }
        if (busqueda != null && !busqueda.isEmpty()) {
            url.append(sep).append("q=").append(URLEncoder.encode(busqueda, StandardCharsets.UTF_8));
        }
        response.sendRedirect(url.toString());
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
