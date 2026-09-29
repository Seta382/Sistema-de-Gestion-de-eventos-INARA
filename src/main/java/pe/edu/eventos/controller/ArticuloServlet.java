
package pe.edu.eventos.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pe.edu.eventos.dao.ArticuloDAO;
import pe.edu.eventos.dao.ProveedorDAO;
import pe.edu.eventos.dao.impl.ArticuloDAOImpl;
import pe.edu.eventos.dao.impl.ProveedorDAOImpl;
import pe.edu.eventos.dto.UsuarioDTO;
import pe.edu.eventos.model.Articulo;
import pe.edu.eventos.util.Constantes;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/articulo")
public class ArticuloServlet extends HttpServlet {

    private final ArticuloDAO articuloDAO = new ArticuloDAOImpl();
    private final ProveedorDAO proveedorDAO = new ProveedorDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute(Constantes.SESION_USUARIO) == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute(Constantes.SESION_USUARIO);

        String rol = usuario.getRol() != null
                ? usuario.getRol().toUpperCase()
                : "";

        if (!Constantes.ROL_ADMIN.equals(rol)
                && !Constantes.ROL_EMPLEADO.equals(rol)) {

            response.sendRedirect(
                    request.getContextPath() + "/cliente/dashboard.jsp"
            );
            return;
        }

        request.setAttribute("proveedores", proveedorDAO.listarTodos());

        request.getRequestDispatcher("/articulos/registro.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String nombre = request.getParameter("nombre");
        String descripcion = request.getParameter("descripcion");
        String precioTexto = request.getParameter("precio");
        String stockTexto = request.getParameter("stock");
        String idProveedorTexto = request.getParameter("idProveedor");

        try {

            if (nombre == null || nombre.trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre del artículo es obligatorio.");
            }

            if (precioTexto == null || precioTexto.trim().isEmpty()) {
                throw new IllegalArgumentException("El precio es obligatorio.");
            }

            if (stockTexto == null || stockTexto.trim().isEmpty()) {
                throw new IllegalArgumentException("El stock es obligatorio.");
            }

            BigDecimal precio = new BigDecimal(precioTexto);
            int stock = Integer.parseInt(stockTexto);

            if (precio.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("El precio no puede ser negativo.");
            }

            if (stock < 0) {
                throw new IllegalArgumentException("El stock no puede ser negativo.");
            }

            Integer idProveedor = null;

            if (idProveedorTexto != null
                    && !idProveedorTexto.trim().isEmpty()
                    && !idProveedorTexto.equals("0")) {

                idProveedor = Integer.parseInt(idProveedorTexto);
            }

            Articulo articulo = new Articulo(
                    nombre,
                    descripcion,
                    precio,
                    stock,
                    idProveedor
            );

            boolean registrado = articuloDAO.registrar(articulo);

            if (registrado) {
                response.sendRedirect(
                        request.getContextPath()
                        + "/articulo?accion=registro&exito=1"
                );
            } else {
                response.sendRedirect(
                        request.getContextPath()
                        + "/articulo?accion=registro&error=1"
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.sendRedirect(
                    request.getContextPath()
                    + "/articulo?accion=registro&error="
                    + java.net.URLEncoder.encode(e.getMessage(), "UTF-8")
            );
        }
    }
}