package pe.edu.eventos.dao;

import pe.edu.eventos.model.Evento;

import java.util.List;

/** Consultas de eventos desde el punto de vista del cliente. */
public interface EventoClienteDAO {

    /** Eventos del cliente: primero los activos (por fecha) y al final los finalizados y cancelados. */
    List<Evento> listarPorCliente(int idCliente);
}
