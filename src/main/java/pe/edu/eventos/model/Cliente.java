package pe.edu.eventos.model;

/**
 * Representa a un cliente en el sistema, heredando los atributos de Usuario.
 */
public class Cliente extends Usuario {

    private Integer idCliente; // PK real que referencian cita.id_cliente y evento.id_cliente
    private String dni;
    private String direccion;

    public Cliente() {
        super();
    }

    public Cliente(Integer id, String nombre, String apellido, String correo, String password,
                   String telefono, String dni, String direccion) {
        super(id, nombre, apellido, correo, password, "CLIENTE");
        setTelefono(telefono);
        this.dni = dni;
        this.direccion = direccion;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}
