package pe.edu.eventos.service;

import pe.edu.eventos.dao.ArticuloDAO;
import pe.edu.eventos.dao.ProveedorDAO;
import pe.edu.eventos.dao.impl.ArticuloDAOImpl;
import pe.edu.eventos.dao.impl.ProveedorDAOImpl;
import pe.edu.eventos.model.Articulo;
import pe.edu.eventos.model.Proveedor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Reglas de negocio de insumos/artículos: alta, edición, búsqueda y baja lógica.
 */
public class ArticuloService {

    /** Mensaje cuando el stock cambió en la base mientras se editaba (lo descuenta el registro de eventos). */
    public static final String ERROR_STOCK_CAMBIO =
            "El stock de este insumo cambió mientras lo editabas (probablemente se usó en un evento). "
                    + "Revisa el stock actual y vuelve a guardar.";

    // articulo.precio es NUMERIC(10,2): como máximo 99,999,999.99
    private static final BigDecimal PRECIO_MAXIMO = new BigDecimal("99999999.99");
    private static final int STOCK_MAXIMO = 1_000_000;

    private final ArticuloDAO articuloDAO;
    private final ProveedorDAO proveedorDAO;

    public ArticuloService() {
        this(new ArticuloDAOImpl(), new ProveedorDAOImpl());
    }

    public ArticuloService(ArticuloDAO articuloDAO, ProveedorDAO proveedorDAO) {
        this.articuloDAO = articuloDAO;
        this.proveedorDAO = proveedorDAO;
    }

    /**
     * Valida y registra un insumo nuevo.
     * @return null si se registró; un mensaje de error si no.
     */
    public String registrar(Articulo a) {
        if (a == null) {
            return "Los datos del insumo no pueden estar vacíos.";
        }

        String error = validarYNormalizarCampos(a);
        if (error != null) return error;

        // Proveedor opcional; si se indica, debe existir y estar activo
        if (a.getIdProveedor() != null && a.getIdProveedor() > 0) {
            error = validarProveedorAsignable(a.getIdProveedor());
            if (error != null) return error;
        } else {
            a.setIdProveedor(null);
        }

        a.setEstado("ACTIVO");
        return articuloDAO.registrar(a) ? null : "No se pudo registrar el insumo. Intenta nuevamente.";
    }

    /**
     * Valida y actualiza un insumo existente (nombre, descripción, precio, stock y proveedor).
     *
     * @param a             datos nuevos; getIdArticulo() identifica el insumo
     * @param stockAnterior stock que tenía el insumo cuando se abrió el formulario de edición
     * @return null si se actualizó; un mensaje de error si no
     *         (ERROR_STOCK_CAMBIO si el stock cambió en la base mientras se editaba).
     */
    public String actualizar(Articulo a, Integer stockAnterior) {
        if (a == null || a.getIdArticulo() == null) {
            return "No se indicó qué insumo editar.";
        }

        Articulo actual = articuloDAO.buscarPorId(a.getIdArticulo());
        if (actual == null) {
            return "El insumo que intentas editar ya no existe.";
        }

        String error = validarYNormalizarCampos(a);
        if (error != null) return error;

        // Proveedor: solo se valida si se cambió (un insumo puede conservar un proveedor ya inactivo)
        Integer nuevoProv = (a.getIdProveedor() != null && a.getIdProveedor() > 0) ? a.getIdProveedor() : null;
        Integer provActual = (actual.getIdProveedor() != null && actual.getIdProveedor() > 0) ? actual.getIdProveedor() : null;
        if (nuevoProv != null && !nuevoProv.equals(provActual)) {
            error = validarProveedorAsignable(nuevoProv);
            if (error != null) return error;
        }
        a.setIdProveedor(nuevoProv);

        // Si el stock no se tocó, no se escribe (no se pisa lo que descontó un evento entretanto).
        // Si se cambió, solo se guarda si en la base sigue el valor que el usuario vio al abrir el formulario.
        Integer stockEsperado = (stockAnterior != null && !stockAnterior.equals(a.getStock())) ? stockAnterior : null;

        if (articuloDAO.actualizar(a, stockEsperado)) {
            return null;
        }

        // No se actualizó: ¿conflicto de stock u otro problema?
        Articulo ahora = articuloDAO.buscarPorId(a.getIdArticulo());
        if (ahora == null) {
            return "El insumo que intentas editar ya no existe.";
        }
        if (stockEsperado != null && ahora.getStock() != null && !ahora.getStock().equals(stockEsperado)) {
            return ERROR_STOCK_CAMBIO;
        }
        return "No se pudieron guardar los cambios. Intenta nuevamente.";
    }

    public List<Articulo> buscar(String texto, String filtro) {
        return articuloDAO.buscar(texto, filtro);
    }

    public Articulo buscarPorId(int idArticulo) {
        return articuloDAO.buscarPorId(idArticulo);
    }

    /** Baja lógica: deja de ofrecerse al registrar eventos, pero se conserva el historial. */
    public boolean darDeBaja(int idArticulo) {
        return articuloDAO.cambiarEstado(idArticulo, "INACTIVO");
    }

    public boolean reactivar(int idArticulo) {
        return articuloDAO.cambiarEstado(idArticulo, "ACTIVO");
    }

    // ------------------------------------------------------------------ helpers

    /** Valida nombre, precio, stock y descripción, y deja los valores normalizados en el objeto. */
    private String validarYNormalizarCampos(Articulo a) {
        String nombre = a.getNombre() == null ? "" : a.getNombre().trim();
        if (nombre.isEmpty()) {
            return "El nombre del insumo es obligatorio.";
        }
        if (nombre.length() > 150) {
            return "El nombre no puede superar los 150 caracteres.";
        }

        if (a.getPrecio() == null || a.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            return "El precio no puede ser negativo.";
        }
        if (a.getPrecio().compareTo(PRECIO_MAXIMO) > 0) {
            return "El precio es demasiado alto.";
        }

        if (a.getStock() == null || a.getStock() < 0) {
            return "El stock no puede ser negativo.";
        }
        if (a.getStock() > STOCK_MAXIMO) {
            return "El stock indicado es demasiado alto.";
        }

        String descripcion = a.getDescripcion() == null ? null : a.getDescripcion().trim();
        a.setNombre(nombre);
        a.setDescripcion(descripcion == null || descripcion.isEmpty() ? null : descripcion);
        a.setPrecio(a.getPrecio().setScale(2, RoundingMode.HALF_UP));
        return null;
    }

    /** El proveedor debe existir y no estar dado de baja. */
    private String validarProveedorAsignable(int idProveedor) {
        Proveedor prov = proveedorDAO.buscarPorId(idProveedor);
        if (prov == null) {
            return "El proveedor seleccionado no existe.";
        }
        if ("INACTIVO".equalsIgnoreCase(prov.getEstado())) {
            return "El proveedor seleccionado está inactivo.";
        }
        return null;
    }
}
