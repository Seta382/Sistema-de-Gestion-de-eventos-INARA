package pe.edu.eventos.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pe.edu.eventos.dto.UsuarioDTO;
import pe.edu.eventos.model.Evento;
import pe.edu.eventos.model.EventoArticulo;
import pe.edu.eventos.service.EventoService;
import pe.edu.eventos.util.Constantes;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/evento")
public class EventoServlet extends HttpServlet {

    private final EventoService eventoService = new EventoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Validar sesión
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(Constantes.SESION_USUARIO) == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute(Constantes.SESION_USUARIO);
        String rol = usuario.getRol() != null ? usuario.getRol().toUpperCase() : "";
        if (!Constantes.ROL_ADMIN.equals(rol) && !Constantes.ROL_EMPLEADO.equals(rol)) {
            response.sendRedirect(request.getContextPath() + "/cliente/dashboard.jsp");
            return;
        }

        request.setAttribute("tiposEvento", eventoService.listarTiposEvento());
        request.setAttribute("clientes", eventoService.listarClientes());
        request.setAttribute("insumos", eventoService.listarInsumosDisponibles());
        request.setAttribute("personal", eventoService.listarPersonalOperativo());
        request.setAttribute("eventosActivos", eventoService.listarEventosActivos());

        request.getRequestDispatcher("/empleado/dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // Validar autorización
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(Constantes.SESION_USUARIO) == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"success\":false,\"mensaje\":\"Sesión no válida o expirada.\"}");
            return;
        }

        try {
            // 1. Datos del evento
            String nombre = request.getParameter("nombre");
            String idTipoStr = request.getParameter("idTipoEvento");
            String idClienteStr = request.getParameter("idCliente");
            String fechaStr = request.getParameter("fecha");
            String horaStr = request.getParameter("hora");
            String aforoStr = request.getParameter("aforo");
            String presupuestoStr = request.getParameter("presupuesto");
            String idCoordinadorStr = request.getParameter("idCoordinador");

            Evento evento = new Evento();
            evento.setNombre(nombre);
            evento.setIdTipoEvento(idTipoStr != null ? Integer.parseInt(idTipoStr) : null);
            evento.setIdCliente(idClienteStr != null ? Integer.parseInt(idClienteStr) : null);

            if (fechaStr != null && !fechaStr.trim().isEmpty()) {
                evento.setFechaEvento(LocalDate.parse(fechaStr.trim()));
            }
            if (horaStr != null && !horaStr.trim().isEmpty()) {
                String h = horaStr.trim();
                if (h.length() == 5) h = h + ":00"; // format HH:mm:ss
                evento.setHoraEvento(LocalTime.parse(h));
            }
            evento.setNumInvitados(aforoStr != null ? Integer.parseInt(aforoStr) : 0);
            evento.setPresupuesto(presupuestoStr != null && !presupuestoStr.trim().isEmpty()
                    ? new BigDecimal(presupuestoStr.trim()) : BigDecimal.ZERO);

            if (idCoordinadorStr != null && !idCoordinadorStr.trim().isEmpty()) {
                evento.setIdEmpleado(Integer.parseInt(idCoordinadorStr.trim()));
            }

            // FIX: id_cita de origen (opcional) — antes no se leía, por eso siempre llegaba vacío
            String idCitaStr = request.getParameter("idCita");
            if (idCitaStr != null && !idCitaStr.trim().isEmpty()) {
                evento.setIdCita(Integer.parseInt(idCitaStr.trim()));
            }

            // 2. Insumos seleccionados: formato "idArticulo:cantidad,idArticulo:cantidad"
            String insumosRaw = request.getParameter("insumosSeleccionados");
            List<EventoArticulo> insumos = new ArrayList<>();
            if (insumosRaw != null && !insumosRaw.trim().isEmpty()) {
                String[] pares = insumosRaw.split(",");
                for (String par : pares) {
                    String[] tokens = par.trim().split(":");
                    if (tokens.length == 2) {
                        int idArt = Integer.parseInt(tokens[0].trim());
                        int cant = Integer.parseInt(tokens[1].trim());
                        if (cant > 0) {
                            EventoArticulo ea = new EventoArticulo();
                            ea.setIdArticulo(idArt);
                            ea.setCantidad(cant);
                            insumos.add(ea);
                        }
                    }
                }
            }

            // 3. Personal seleccionado: IDs separados por coma o parámetros múltiples
            String[] staffArray = request.getParameterValues("idStaff");
            List<Integer> idEmpleados = new ArrayList<>();
            if (staffArray != null) {
                for (String s : staffArray) {
                    if (s != null && !s.trim().isEmpty()) {
                        idEmpleados.add(Integer.parseInt(s.trim()));
                    }
                }
            }

            // 4. Invocar servicio transaccional
            boolean ok = eventoService.registrarEventoEnVivo(evento, insumos, idEmpleados);

            if (ok) {
                out.print("{\"success\":true,\"mensaje\":\"¡Evento registrado y guardado exitosamente en Supabase!\",\"idEvento\":" + evento.getIdEvento() + "}");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"mensaje\":\"No se pudo completar el registro del evento en Supabase. Se ejecutó Rollback.\"}");
            }

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"mensaje\":\"" + escapeJson(e.getMessage()) + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"mensaje\":\"Error interno al procesar el evento: " + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"").replace("\n", " ").replace("\r", "");
    }
}
