package pe.edu.eventos.dao;

import pe.edu.eventos.model.Pago;

import java.math.BigDecimal;
import java.util.List;

public interface PagoDAO {

    /**
     * Registra un pago en una transacción: bloquea el evento, comprueba que haya una cotización
     * ACEPTADA y que, si el pago es COMPLETADO, no supere el saldo pendiente.
     *
     * @return el id creado; null si se hizo rollback
     */
    Integer registrar(Pago pago);

    /** Pago con datos del evento y del cliente; null si no existe. */
    Pago buscarPorId(int idPago);

    /** Pagos de un evento, el más reciente primero. */
    List<Pago> listarPorEvento(int idEvento);

    /** Listado del personal. filtro: PENDIENTE, COMPLETADO, RECHAZADO o TODOS. */
    List<Pago> buscar(String texto, String filtro);

    /** Total de la cotización ACEPTADA del evento; null si no hay. */
    BigDecimal totalAcordado(int idEvento);

    /**
     * Confirma un pago PENDIENTE (pasa a COMPLETADO) en una transacción, comprobando que
     * no se supere el total acordado.
     */
    boolean confirmar(int idPago);

    /** Cambia el estado solo si todavía está en estadoActual. */
    boolean cambiarEstado(int idPago, String estadoActual, String estadoNuevo);

    int contarPorEstado(String estado);
}
