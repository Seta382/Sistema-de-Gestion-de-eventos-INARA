package pe.edu.eventos.model;

/**
 * Modelo que representa la entidad Proveedor del sistema INARA.
 * Mapea directamente con la tabla 'proveedor' en PostgreSQL (Supabase).
 */
public class Proveedor {

    private Integer idProveedor;
    private String razonSocial;
    private String ruc;
    private String correo;
    private String telefono;
    private String direccion;
    private String estado;

    public Proveedor() {
        this.estado = "ACTIVO";
    }

    public Proveedor(Integer idProveedor, String razonSocial, String ruc, String correo, String telefono, String direccion, String estado) {
        this.idProveedor = idProveedor;
        this.razonSocial = razonSocial;
        this.ruc = ruc;
        this.correo = correo;
        this.telefono = telefono;
        this.direccion = direccion;
        this.estado = estado != null ? estado : "ACTIVO";
    }

    public Proveedor(String razonSocial, String ruc, String correo, String telefono, String direccion) {
        this.razonSocial = razonSocial;
        this.ruc = ruc;
        this.correo = correo;
        this.telefono = telefono;
        this.direccion = direccion;
        this.estado = "ACTIVO";
    }

    public Integer getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(Integer idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Proveedor{" +
                "idProveedor=" + idProveedor +
                ", razonSocial='" + razonSocial + '\'' +
                ", ruc='" + ruc + '\'' +
                ", correo='" + correo + '\'' +
                ", telefono='" + telefono + '\'' +
                ", direccion='" + direccion + '\'' +
                ", estado='" + estado + '\'' +
                '}';
    }
}
