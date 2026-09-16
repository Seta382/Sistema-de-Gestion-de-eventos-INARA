package main.java.pe.edu.eventos.model;

/**
 *heredan los datos de la clase usuario
 */
public class Cliente extends Usuario {
    private Integer idCliente;
    private String telefono;

    public Cliente(){
        super();
    }

    public Cliente(Integer id, String nombre, String apellido, String correo, String password, String rol, String telefono, Integer idCliente) {
        super(id, nombre, apellido, correo, password, "CLIENTE");
        this.telefono = telefono;
        this.idCliente = idCliente;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}
