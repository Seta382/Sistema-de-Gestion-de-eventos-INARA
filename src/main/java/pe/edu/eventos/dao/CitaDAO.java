package pe.edu.eventos.dao;

import pe.edu.eventos.model.Cita;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

public interface CitaDAO {

    boolean crear(Cita cita); // deja cita.getIdCita() seteado tras el insert

    Cita buscarPorId(int idCita);

    List<Cita> listarPorCliente(int idCliente);

    List<Cita> listarPorEmpleado(int idEmpleado);

    List<Cita> listarPorEstado(String estado);

    boolean actualizarEstado(int idCita, String nuevoEstado);

    boolean reprogramar(int idCita, Date nuevaFecha, Time nuevaHora);

    boolean asignarEmpleado(int idCita, int idEmpleado);

    boolean eliminar(int idCita);
}
