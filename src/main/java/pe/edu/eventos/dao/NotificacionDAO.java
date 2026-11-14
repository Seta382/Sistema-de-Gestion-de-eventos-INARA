package pe.edu.eventos.dao;

import pe.edu.eventos.model.Notificacion;

import java.util.List;

public interface NotificacionDAO {

    /**
     * Crea un aviso para el cliente titular del evento.
     * La plantilla puede llevar {evento} y {cliente}; la base los reemplaza.
     * @return true si se creó el aviso
     */
    boolean avisarCliente(int idEvento, String tipo, String plantilla);

    /**
     * Crea un aviso para el personal del evento: los administradores y el coordinador asignado.
     * @return cuántos avisos se crearon
     */
    int avisarPersonal(int idEvento, String tipo, String plantilla);

    /** Avisos del usuario, el más reciente primero. */
    List<Notificacion> listarPorUsuario(int idUsuario, boolean soloNoLeidas, int limite);

    /** Aviso de ese usuario; null si no existe o es de otra persona. */
    Notificacion buscarPorId(int idNotificacion, int idUsuario);

    int contarNoLeidas(int idUsuario);

    /** Marca como leído solo si el aviso es de ese usuario. */
    boolean marcarLeida(int idNotificacion, int idUsuario);

    /** @return cuántos avisos se marcaron */
    int marcarTodasLeidas(int idUsuario);
}
