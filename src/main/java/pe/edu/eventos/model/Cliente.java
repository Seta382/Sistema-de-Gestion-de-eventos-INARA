package pe.edu.eventos.model;

/**
 * Representa a un cliente en el sistema, heredando los atributos de Usuario.
 */
public class Cliente extends Usuario {
    private Integer idCliente;

    public Cliente() {
        super();
        setRol("CLIENTE");
    }

    public Cliente(Integer id, String nombre, String apellido, String correo, String password, String rol, String telefono, Integer idCliente) {
        super(id, nombre, apellido, correo, password, rol != null ? rol : "CLIENTE", telefono, null);
        this.idCliente = idCliente;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }
}

