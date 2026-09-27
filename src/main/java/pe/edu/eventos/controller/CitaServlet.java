package pe.edu.eventos.controller;

import pe.edu.eventos.dao.ClienteDAO;
import pe.edu.eventos.dao.impl.ClienteDAOImpl;
import pe.edu.eventos.model.Cita;
import pe.edu.eventos.model.Usuario;
import pe.edu.eventos.service.CitaService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.util.List;

@WebServlet("/citas")
public class CitaServlet extends HttpServlet {

    private final CitaService citaService = new CitaService();
    private final ClienteDAO clienteDAO = new ClienteDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Usuario usuario = obtenerUsuarioDeSesion(request, response);
        if (usuario == null) return;

        String action = request.getParameter("action");

        if ("nueva".equals(action)) {
            request.getRequestDispatcher("cliente/registrar-cita.jsp").forward(request, response);
            return;
        }

        if ("cancelar".equals(action)) {
            int idCita = Integer.parseInt(request.getParameter("id"));
            citaService.cancelar(idCita);
            response.sendRedirect("citas");
            return;
        }

        Integer idCliente = clienteDAO.buscarIdPorUsuario(usuario.getId());
        if (idCliente == null) {
            request.setAttribute("error", "Tu perfil de cliente no está completo todavía");
            request.getRequestDispatcher("cliente/mis-citas.jsp").forward(request, response);
            return;
        }

        List<Cita> citas = citaService.listarPorCliente(idCliente);
        request.setAttribute("citas", citas);
        request.getRequestDispatcher("cliente/mis-citas.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Usuario usuario = obtenerUsuarioDeSesion(request, response);
        if (usuario == null) return;

        Integer idCliente = clienteDAO.buscarIdPorUsuario(usuario.getId());
        if (idCliente == null) {
            request.setAttribute("error", "Tu perfil de cliente no está completo todavía");
            request.getRequestDispatcher("cliente/registrar-cita.jsp").forward(request, response);
            return;
        }

        Date fecha = Date.valueOf(request.getParameter("fecha"));   // formato yyyy-MM-dd (input type="date")
        Time hora = Time.valueOf(request.getParameter("hora") + ":00"); // input type="time" da HH:mm
        String modalidad = request.getParameter("modalidad");
        String lugar = request.getParameter("lugar");
        String motivo = request.getParameter("motivo");

        Cita cita = new Cita(idCliente, fecha, hora, modalidad, lugar, motivo);

        String error = citaService.crear(cita);
        if (error != null) {
            request.setAttribute("error", error);
            request.getRequestDispatcher("cliente/registrar-cita.jsp").forward(request, response);
            return;
        }

        response.sendRedirect("citas");
    }

    private Usuario obtenerUsuarioDeSesion(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        Usuario usuario = session != null ? (Usuario) session.getAttribute("usuario") : null;
        if (usuario == null) {
            response.sendRedirect("login.jsp");
            return null;
        }
        return usuario;
    }
}
