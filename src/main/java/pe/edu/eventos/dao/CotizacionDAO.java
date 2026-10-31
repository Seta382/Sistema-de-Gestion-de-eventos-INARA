package pe.edu.eventos.dao;

import pe.edu.eventos.model.Cotizacion;
import pe.edu.eventos.model.DetalleCotizacion;

import java.util.List;

public interface CotizacionDAO {

    /**
     * Registra una cotización nueva de forma atómica (una sola transacción):
     * 1. Bloquea el evento y comprueba que no tenga ya una cotización ACEPTADA.
     * 2. Marca como MODIFICADA la cotización PENDIENTE anterior (si hay).
     * 3. Inserta la cabecera y las líneas.
     * 4. Si el evento estaba SOLICITADO, lo pasa a COTIZADO.
     *
     * @return el id de la cotización creada; null si se hizo rollback
     */
    Integer registrar(Cotizacion cotizacion, List<DetalleCotizacion> detalles);

    /** Cotización con sus líneas y datos del evento/cliente; null si no existe. */
    Cotizacion buscarPorId(int idCotizacion);

    /** Cotizaciones de un evento, la más nueva primero, con sus líneas. */
    List<Cotizacion> listarPorEvento(int idEvento);

    /** Cotizaciones de los eventos de un cliente, la más nueva primero, con sus líneas. */
    List<Cotizacion> listarPorCliente(int idCliente);

    /** Listado del personal (sin líneas). filtro: PENDIENTE, ACEPTADA, RECHAZADA, MODIFICADA o TODAS. */
    List<Cotizacion> buscar(String texto, String filtro);

    /**
     * Cambia el estado solo si todavía está en estadoActual (así no se pisa el cambio de otra persona).
     * @return true si se actualizó
     */
    boolean cambiarEstado(int idCotizacion, String estadoActual, String estadoNuevo);

    int contarPorEstado(String estado);

    int contarPendientesPorCliente(int idCliente);
}
