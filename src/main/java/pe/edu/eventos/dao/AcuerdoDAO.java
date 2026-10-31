package pe.edu.eventos.dao;

import pe.edu.eventos.model.Acuerdo;

import java.util.List;

public interface AcuerdoDAO {

    /**
     * Registra un acuerdo PENDIENTE en una transacción: bloquea el evento y comprueba que tenga una
     * cotización ACEPTADA y que no tenga ya un acuerdo PENDIENTE o ACEPTADO.
     *
     * @return el id creado; null si se hizo rollback
     */
    Integer registrar(Acuerdo acuerdo);

    /** Acuerdo con datos del evento y del cliente; null si no existe. */
    Acuerdo buscarPorId(int idAcuerdo);

    /** Acuerdos de un evento, el más nuevo primero. */
    List<Acuerdo> listarPorEvento(int idEvento);

    /** Acuerdos de los eventos de un cliente, el más nuevo primero. */
    List<Acuerdo> listarPorCliente(int idCliente);

    /** Listado del personal. filtro: PENDIENTE, ACEPTADO, CANCELADO o TODOS. */
    List<Acuerdo> buscar(String texto, String filtro);

    /** Cambia las condiciones, solo si el acuerdo sigue PENDIENTE. */
    boolean actualizarCondiciones(int idAcuerdo, String condiciones);

    /**
     * Acepta el acuerdo (si sigue PENDIENTE), guarda la fecha de confirmación y pasa el evento
     * de COTIZADO a CONFIRMADO, todo en una transacción.
     */
    boolean aceptar(int idAcuerdo);

    /**
     * Cancela un acuerdo PENDIENTE o ACEPTADO. Si estaba ACEPTADO y el evento estaba CONFIRMADO,
     * el evento vuelve a COTIZADO (misma transacción).
     */
    boolean cancelar(int idAcuerdo);

    int contarPorEstado(String estado);

    int contarPendientesPorCliente(int idCliente);
}
