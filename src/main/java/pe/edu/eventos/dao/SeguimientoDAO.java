package pe.edu.eventos.dao;

import pe.edu.eventos.model.Seguimiento;

import java.util.List;

public interface SeguimientoDAO {

    /** @return el id creado; null si falló */
    Integer registrar(Seguimiento s);

    Seguimiento buscarPorId(int idSeguimiento);

    /** Avances del evento, el más reciente primero. */
    List<Seguimiento> listarPorEvento(int idEvento);

    boolean actualizar(int idSeguimiento, String titulo, String descripcion);

    boolean eliminar(int idSeguimiento);
}
