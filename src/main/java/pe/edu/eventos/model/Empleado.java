package pe.edu.eventos.model;

/**
 * Modelo que representa la entidad Empleado del sistema INARA.
 * Mapea directamente con la tabla 'empleado' en PostgreSQL (Supabase)
 * y mantiene relación con 'usuario'.
 */
public class Empleado {

    private Integer idEmpleado;
    private Integer idUsuario;
    private String dni;
    private String cargo;
    private String area;

    // Campos auxiliares obtenidos del JOIN con 'usuario'
    private String nombre;
    private String apellido;
    private String correo;
    private String rol;

    public Empleado() {
    }

    public Empleado(Integer idEmpleado, Integer idUsuario, String dni, String cargo, String area) {
        this.idEmpleado = idEmpleado;
        this.idUsuario = idUsuario;
        this.dni = dni;
        this.cargo = cargo;
        this.area = area;
    }

    public Empleado(Integer idUsuario, String dni, String cargo, String area) {
        this.idUsuario = idUsuario;
        this.dni = dni;
        this.cargo = cargo;
        this.area = area;
    }

    public Integer getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Integer idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getNombreCompleto() {
        if (nombre != null && apellido != null) {
            return nombre + " " + apellido;
        } else if (nombre != null) {
            return nombre;
        }
        return "";
    }

    @Override
    public String toString() {
        return "Empleado{" +
                "idEmpleado=" + idEmpleado +
                ", idUsuario=" + idUsuario +
                ", dni='" + dni + '\'' +
                ", cargo='" + cargo + '\'' +
                ", area='" + area + '\'' +
                ", nombre='" + getNombreCompleto() + '\'' +
                '}';
    }
}
