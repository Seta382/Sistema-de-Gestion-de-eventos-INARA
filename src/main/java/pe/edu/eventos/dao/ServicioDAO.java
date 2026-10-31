package pe.edu.eventos.dao;

import pe.edu.eventos.model.Servicio;

import java.util.List;

public interface ServicioDAO {

    boolean registrar(Servicio servicio);

    Servicio buscarPorId(int idServicio);

    /** Busca un servicio por nombre exacto (sin distinguir mayúsculas ni espacios sobrantes). */
    Servicio buscarPorNombre(String nombre);

    /** Servicios activos, para armar cotizaciones. */
    List<Servicio> listarActivos();

    /** Búsqueda sin tildes por nombre/descripción. filtro: ACTIVO, INACTIVO o TODOS. */
    List<Servicio> buscar(String texto, String filtro);

    boolean cambiarEstado(int idServicio, String nuevoEstado);

    boolean actualizar(Servicio servicio);
}
