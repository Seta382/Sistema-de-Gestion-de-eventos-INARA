package pe.edu.eventos.service;

import pe.edu.eventos.dao.ServicioDAO;
import pe.edu.eventos.dao.impl.ServicioDAOImpl;
import pe.edu.eventos.model.Servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Reglas de negocio de servicios y precios: alta, edición, búsqueda y baja lógica.
 * Cambiar el precio no altera cotizaciones anteriores (detalle_cotizacion guarda el precio unitario).
 */
public class ServicioService {

    // servicio.precio es NUMERIC(10,2): como máximo 99,999,999.99
    private static final BigDecimal PRECIO_MAXIMO = new BigDecimal("99999999.99");
    private static final int NOMBRE_MAXIMO = 150;
    private static final int DESCRIPCION_MAXIMO = 1000;

    private final ServicioDAO servicioDAO;

    public ServicioService() {
        this(new ServicioDAOImpl());
    }

    public ServicioService(ServicioDAO servicioDAO) {
        this.servicioDAO = servicioDAO;
    }

    /** @return null si se registró; un mensaje de error si no. */
    public String registrar(Servicio s) {
        if (s == null) {
            return "Los datos del servicio no pueden estar vacíos.";
        }
        String error = validarYNormalizarCampos(s);
        if (error != null) return error;

        error = validarNombreUnico(s.getNombre(), null);
        if (error != null) return error;

        s.setEstado("ACTIVO");
        return servicioDAO.registrar(s) ? null : "No se pudo registrar el servicio. Intenta nuevamente.";
    }

    /** @return null si se actualizó; un mensaje de error si no. */
    public String actualizar(Servicio s) {
        if (s == null || s.getIdServicio() == null) {
            return "No se indicó qué servicio editar.";
        }
        if (servicioDAO.buscarPorId(s.getIdServicio()) == null) {
            return "El servicio que intentas editar ya no existe.";
        }
        String error = validarYNormalizarCampos(s);
        if (error != null) return error;

        error = validarNombreUnico(s.getNombre(), s.getIdServicio());
        if (error != null) return error;

        return servicioDAO.actualizar(s) ? null : "No se pudieron guardar los cambios. Intenta nuevamente.";
    }

    public List<Servicio> buscar(String texto, String filtro) {
        return servicioDAO.buscar(texto, filtro);
    }

    public List<Servicio> listarActivos() {
        return servicioDAO.listarActivos();
    }

    public Servicio buscarPorId(int idServicio) {
        return servicioDAO.buscarPorId(idServicio);
    }

    /** Baja lógica: deja de ofrecerse en cotizaciones nuevas, pero se conserva el historial. */
    public boolean darDeBaja(int idServicio) {
        return servicioDAO.cambiarEstado(idServicio, "INACTIVO");
    }

    public boolean reactivar(int idServicio) {
        return servicioDAO.cambiarEstado(idServicio, "ACTIVO");
    }

    // ------------------------------------------------------------------ helpers

    private String validarYNormalizarCampos(Servicio s) {
        String nombre = s.getNombre() == null ? "" : s.getNombre().trim();
        if (nombre.isEmpty()) {
            return "El nombre del servicio es obligatorio.";
        }
        if (nombre.length() > NOMBRE_MAXIMO) {
            return "El nombre no puede superar los " + NOMBRE_MAXIMO + " caracteres.";
        }

        if (s.getPrecio() == null || s.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            return "El precio debe ser mayor a cero.";
        }
        if (s.getPrecio().compareTo(PRECIO_MAXIMO) > 0) {
            return "El precio es demasiado alto.";
        }
        BigDecimal precio = s.getPrecio().setScale(2, RoundingMode.HALF_UP);
        if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            return "El precio debe ser mayor a cero.";
        }

        String descripcion = s.getDescripcion() == null ? null : s.getDescripcion().trim();
        if (descripcion != null && descripcion.length() > DESCRIPCION_MAXIMO) {
            return "La descripción no puede superar los " + DESCRIPCION_MAXIMO + " caracteres.";
        }

        s.setNombre(nombre);
        s.setDescripcion(descripcion == null || descripcion.isEmpty() ? null : descripcion);
        s.setPrecio(precio);
        return null;
    }

    /** No puede haber dos servicios con el mismo nombre (tampoco con uno dado de baja). */
    private String validarNombreUnico(String nombre, Integer idActual) {
        Servicio existente = servicioDAO.buscarPorNombre(nombre);
        if (existente == null || (idActual != null && idActual.equals(existente.getIdServicio()))) {
            return null;
        }
        if (!existente.isActivo()) {
            return "Ya existe un servicio llamado «" + existente.getNombre()
                    + "» dado de baja. Reactívalo desde el listado (filtro Inactivos).";
        }
        return "Ya existe un servicio con ese nombre.";
    }
}
