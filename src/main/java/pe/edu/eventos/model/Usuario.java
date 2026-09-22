package pe.edu.eventos.model;

import java.time.LocalDateTime;

/**
 * Representa los usuarios registrados en el sistema (Clientes, Organizadores, Administradores).
 * Mapea directamente con la tabla 'Usuario' en Supabase.
 */
public class Usuario {
    private Integer id;
    private String nombre;
    private String apellido;
    private String correo;
    private String password;
    private String rol;
    private String telefono;
    private LocalDateTime creadoEn;

    public Usuario() {
    }

    public Usuario(Integer id, String nombre, String apellido, String correo, String password, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.password = password;
        this.rol = rol;
    }

    public Usuario(Integer id, String nombre, String apellido, String correo, String password, String rol, String telefono, LocalDateTime creadoEn) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.password = password;
        this.rol = rol;
        this.telefono = telefono;
        this.creadoEn = creadoEn;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", correo='" + correo + '\'' +
                ", rol='" + rol + '\'' +
                ", telefono='" + telefono + '\'' +
                '}';
    }
}
