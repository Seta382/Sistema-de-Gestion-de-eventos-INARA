package pe.edu.eventos.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import pe.edu.eventos.dao.UsuarioDAO;
import pe.edu.eventos.dao.impl.UsuarioDAOImpl;

import java.io.IOException;

@WebServlet("/recuperar-password")
public class RecuperarPasswordServlet extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher(
                "/Login/RecuperarPassword.jsp"
        ).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String accion = request.getParameter("accion");
        HttpSession session = request.getSession();

        if ("verificar".equals(accion)) {

            String nombre = request.getParameter("nombreCompleto");
            String dni = request.getParameter("dni");
            String telefono = request.getParameter("telefono");
            String correo = request.getParameter("correo");

            if (vacio(nombre) || vacio(dni)
                    || vacio(telefono) || vacio(correo)) {

                mostrarError(request, response,
                        "Completa todos los campos.");
                return;
            }

            boolean coincide = usuarioDAO.verificarDatosRecuperacion(
                    nombre.trim(),
                    dni.trim(),
                    telefono.trim(),
                    correo.trim()
            );

            if (coincide) {
                // Guardamos temporalmente el correo verificado.
                session.setAttribute(
                        "correoRecuperacion",
                        correo.trim()
                );

                request.setAttribute("verificado", true);

            } else {
                request.setAttribute(
                        "error",
                        "Los datos ingresados no coinciden con un cliente registrado."
                );
            }

            request.getRequestDispatcher(
                    "/Login/RecuperarPassword.jsp"
            ).forward(request, response);

            return;
        }

        if ("cambiar".equals(accion)) {

            String correo = (String) session.getAttribute(
                    "correoRecuperacion"
            );

            if (correo == null) {
                response.sendRedirect(
                        request.getContextPath() + "/recuperar-password"
                );
                return;
            }

            String password = request.getParameter("password");
            String confirmar = request.getParameter("confirmarPassword");

            if (password == null || password.trim().isEmpty()
                    || confirmar == null || confirmar.isEmpty()) {

                request.setAttribute("verificado", true);
                request.setAttribute(
                        "error",
                        "Debes completar ambos campos de contraseña."
                );

                request.getRequestDispatcher(
                        "/Login/RecuperarPassword.jsp"
                ).forward(request, response);
                return;
            }

            if (password.length() < 6) {
                request.setAttribute("verificado", true);
                request.setAttribute(
                        "error",
                        "La contraseña debe tener al menos 6 caracteres."
                );

                request.getRequestDispatcher(
                        "/Login/RecuperarPassword.jsp"
                ).forward(request, response);
                return;
            }

            if (!password.equals(confirmar)) {
                request.setAttribute("verificado", true);
                request.setAttribute(
                        "error",
                        "Las contraseñas no coinciden."
                );

                request.getRequestDispatcher(
                        "/Login/RecuperarPassword.jsp"
                ).forward(request, response);
                return;
            }

            // Recuperamos el usuario por su correo.
            pe.edu.eventos.model.Usuario usuario =
                    new UsuarioDAOImpl().buscarPorCorreo(correo);

            if (usuario == null) {
                session.removeAttribute("correoRecuperacion");

                mostrarError(request, response,
                        "No se encontró el usuario.");
                return;
            }

            boolean actualizado = usuarioDAO.actualizarPassword(
                    usuario.getId(),
                    password
            );

            if (actualizado) {
                session.removeAttribute("correoRecuperacion");

                response.sendRedirect(
                        request.getContextPath()
                        + "/login?passwordActualizada=1"
                );

            } else {
                request.setAttribute("verificado", true);
                request.setAttribute(
                        "error",
                        "No se pudo actualizar la contraseña. Inténtalo nuevamente."
                );

                request.getRequestDispatcher(
                        "/Login/RecuperarPassword.jsp"
                ).forward(request, response);
            }

            return;
        }

        response.sendRedirect(
                request.getContextPath() + "/recuperar-password"
        );
    }

    private boolean vacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    private void mostrarError(HttpServletRequest request,
                              HttpServletResponse response,
                              String mensaje)
            throws ServletException, IOException {

        request.setAttribute("error", mensaje);

        request.getRequestDispatcher(
                "/Login/RecuperarPassword.jsp"
        ).forward(request, response);
    }
}
