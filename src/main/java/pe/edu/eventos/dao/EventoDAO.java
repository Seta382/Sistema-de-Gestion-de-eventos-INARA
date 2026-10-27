package pe.edu.eventos.dao;

import pe.edu.eventos.model.Evento;
import pe.edu.eventos.model.EventoArticulo;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

/**
 * Interfaz de acceso a datos para Eventos e insumos/personal vinculados.
 */
public interface EventoDAO {

    /** Zona horaria del negocio: define qué es "hoy" para separar eventos próximos de pasados. */
    ZoneId ZONA_NEGOCIO = ZoneId.of("America/Lima");

    /** Filtros del cronograma: de hoy en adelante, anteriores a hoy, o todos. */
    String FILTRO_PROXIMOS = "PROXIMOS";
    String FILTRO_PASADOS = "PASADOS";
    String FILTRO_TODOS = "TODOS";

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

    /**
     * Los últimos eventos registrados (más nuevo primero), limitados a "limite".
     * Es lo que muestra el panel "Eventos en Ejecución" del dashboard.
     */
    List<Evento> listarEventosRecientes(int limite);

    /**
     * Búsqueda para la pantalla de cronograma.
     * texto: palabras libres; cada una debe coincidir con evento, cliente, DNI, tipo, lugar,
     *        coordinador, estado o fecha (YYYY-MM-DD o DD/MM/YYYY).
     * filtro: FILTRO_PROXIMOS, FILTRO_PASADOS o FILTRO_TODOS (cualquier otro valor = todos).
     * Orden: primero los próximos (el más cercano arriba), luego los pasados (el más reciente arriba).
     */
    List<Evento> buscarEventos(String texto, String filtro);

    /** Cantidad de eventos por período. Claves: TODOS, PROXIMOS y PASADOS. */
    Map<String, Integer> contarPorPeriodo();

        /**
     * Cambia el estado (etapa) de un evento, pero solo si todavía está en "estadoActual".
     * Así, si otra persona ya lo cambió, no se pisa su cambio.
     *
     * @return true si se actualizó el evento; false si ya cambió, no existe o hubo un error
     */
    boolean cambiarEstado(int idEvento, String estadoActual, String estadoNuevo);
}
