package pe.edu.eventos.model;

/**
 * Modelo que representa un tipo de celebración/evento del sistema INARA.
 * Mapea directamente con la tabla 'tipo_evento' en PostgreSQL (Supabase).
 */
public class TipoEvento {

    private Integer idTipoEvento;
    private String nombre;
    private String descripcion;

    public TipoEvento() {
    }

    public TipoEvento(Integer idTipoEvento, String nombre, String descripcion) {
        this.idTipoEvento = idTipoEvento;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public Integer getIdTipoEvento() {
        return idTipoEvento;
    }

    public void setIdTipoEvento(Integer idTipoEvento) {
        this.idTipoEvento = idTipoEvento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return "TipoEvento{" +
                "idTipoEvento=" + idTipoEvento +
                ", nombre='" + nombre + '\'' +
                '}';
    }
}