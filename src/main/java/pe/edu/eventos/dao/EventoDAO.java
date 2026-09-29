package pe.edu.eventos.dao;

import pe.edu.eventos.model.Evento;
import pe.edu.eventos.model.EventoArticulo;
import java.util.List;

/**
 * Interfaz de acceso a datos para Eventos e insumos/personal vinculados.
 */
public interface EventoDAO {

    /**
     * Registra un evento completo de forma atómica:
     * 1. Comprueba stock de cada artículo con bloqueo/consulta en BD.
     * 2. Inserta la cabecera del evento.
     * 3. Inserta los insumos en evento_articulo y descuenta el stock.
     * 4. Inserta el personal asignado en evento_empleado.
     * Todo dentro de una única transacción JDBC (commit o rollback).
     *
     * @param evento cabecera del evento
     * @param insumos lista de artículos con cantidad y precio
     * @param idEmpleadosPersonal lista de IDs de empleados participantes
     * @return true si la transacción se confirmó con éxito; false si hubo rollback
     */
    boolean registrarEventoCompleto(Evento evento, List<EventoArticulo> insumos, List<Integer> idEmpleadosPersonal);

    /**
     * Lista los eventos reales registrados para el monitor en vivo del dashboard.
     */
    List<Evento> listarEventosActivos();

    Evento buscarPorId(int idEvento);

    List<EventoArticulo> listarInsumosPorEvento(int idEvento);
}
