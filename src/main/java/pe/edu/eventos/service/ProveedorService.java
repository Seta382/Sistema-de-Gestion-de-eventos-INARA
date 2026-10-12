package pe.edu.eventos.service;

import pe.edu.eventos.dao.ProveedorDAO;
import pe.edu.eventos.dao.impl.ProveedorDAOImpl;
import pe.edu.eventos.model.Proveedor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Reglas de negocio de proveedores: validación del alta y consultas.
 */
public class ProveedorService {

    private static final Pattern RUC = Pattern.compile("\\d{11}");
    private static final Pattern CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final ProveedorDAO proveedorDAO;

    public ProveedorService() {
        this.proveedorDAO = new ProveedorDAOImpl();
    }

    /**
     * Valida y registra un proveedor nuevo.
     * @return null si se registró; un mensaje de error si no.
     */
    public String registrar(Proveedor p) {
        if (p == null) {
            return "Los datos del proveedor no pueden estar vacíos.";
        }

        String razon = limpiar(p.getRazonSocial());
        String ruc = limpiar(p.getRuc());
        String correo = limpiar(p.getCorreo());
        String telefono = limpiar(p.getTelefono());
        String direccion = limpiar(p.getDireccion());

        if (razon == null) {
            return "La razón social es obligatoria.";
        }
        if (razon.length() > 150) {
            return "La razón social no puede superar los 150 caracteres.";
        }
        if (ruc == null || !RUC.matcher(ruc).matches()) {
            return "El RUC debe tener exactamente 11 dígitos numéricos.";
        }
        if (correo != null && (correo.length() > 150 || !CORREO.matcher(correo).matches())) {
            return "El correo del proveedor no tiene un formato válido.";
        }
        if (telefono != null && telefono.length() > 20) {
            return "El teléfono no puede superar los 20 caracteres.";
        }
        if (direccion != null && direccion.length() > 150) {
            return "La dirección no puede superar los 150 caracteres.";
        }
        if (proveedorDAO.buscarPorRuc(ruc) != null) {
            return "Ya existe un proveedor registrado con ese RUC.";
        }

        p.setRazonSocial(razon);
        p.setRuc(ruc);
        p.setCorreo(correo);
        p.setTelefono(telefono);
        p.setDireccion(direccion);
        p.setEstado("ACTIVO");

        return proveedorDAO.registrar(p) ? null : "No se pudo registrar el proveedor. Intenta nuevamente.";
    }

    public List<Proveedor> buscar(String texto) {
        return proveedorDAO.buscar(texto);
    }

    public Proveedor buscarPorId(int idProveedor) {
        return proveedorDAO.buscarPorId(idProveedor);
    }

    /** Proveedores que se pueden asignar a un insumo nuevo. */
    public List<Proveedor> listarActivos() {
        List<Proveedor> activos = new ArrayList<>();
        for (Proveedor p : proveedorDAO.listarTodos()) {
            if (!"INACTIVO".equalsIgnoreCase(p.getEstado())) {
                activos.add(p);
            }
        }
        return activos;
    }

    private String limpiar(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
