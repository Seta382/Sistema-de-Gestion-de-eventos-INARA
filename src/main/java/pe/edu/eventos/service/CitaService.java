package pe.edu.eventos.service;

import pe.edu.eventos.dao.CitaDAO;
import pe.edu.eventos.dao.impl.CitaDAOImpl;
import pe.edu.eventos.model.Cita;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

/**
 * Reglas de negocio de Cita. El Servlet nunca llama directo a
 * actualizarEstado(); pasa por confirmar/cancelar/reprogramar para que
 * las transiciones válidas vivan en un solo lugar.
 */
public class CitaService {

    private final CitaDAO citaDAO;

    public CitaService() {
        this.citaDAO = new CitaDAOImpl();
    }

    public String crear(Cita cita) {
        if (cita.getFecha() == null || cita.getHora() == null) {
            return "Debes indicar fecha y hora";
        }
        if (cita.getFecha().toLocalDate().isBefore(java.time.LocalDate.now())) {
            return "La fecha de la cita no puede ser en el pasado";
        }
        if (cita.getModalidad() == null || cita.getModalidad().isBlank()) {
            cita.setModalidad("PRESENCIAL");
        }
        boolean ok = citaDAO.crear(cita);
        return ok ? null : "No se pudo registrar la cita";
    }

    public boolean confirmar(int idCita) {
        return citaDAO.actualizarEstado(idCita, "CONFIRMADA");
    }

    public boolean cancelar(int idCita) {
        return citaDAO.actualizarEstado(idCita, "CANCELADA");
    }

    public boolean finalizar(int idCita) {
        return citaDAO.actualizarEstado(idCita, "FINALIZADA");
    }

    public boolean reprogramar(int idCita, Date nuevaFecha, Time nuevaHora) {
        return citaDAO.reprogramar(idCita, nuevaFecha, nuevaHora);
    }

    public boolean asignarEmpleado(int idCita, int idEmpleado) {
        return citaDAO.asignarEmpleado(idCita, idEmpleado);
    }

    public Cita buscarPorId(int idCita) {
        return citaDAO.buscarPorId(idCita);
    }

    public List<Cita> listarPorCliente(int idCliente) {
        return citaDAO.listarPorCliente(idCliente);
    }

    public List<Cita> listarPorEmpleado(int idEmpleado) {
        return citaDAO.listarPorEmpleado(idEmpleado);
    }

    public List<Cita> listarPorEstado(String estado) {
        return citaDAO.listarPorEstado(estado);
    }
}
