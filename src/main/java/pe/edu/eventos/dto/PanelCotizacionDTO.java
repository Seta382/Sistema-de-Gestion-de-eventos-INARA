package pe.edu.eventos.dto;

import pe.edu.eventos.model.Cotizacion;
import pe.edu.eventos.model.Evento;
import pe.edu.eventos.model.Servicio;

import java.util.List;

/** Todo lo que necesita la pantalla de cotización de un evento. */
public class PanelCotizacionDTO {

    private final Evento evento;
    private final List<Servicio> servicios;
    private final List<Cotizacion> cotizaciones;
    private final String motivoBloqueo;

    public PanelCotizacionDTO(Evento evento, List<Servicio> servicios,
                              List<Cotizacion> cotizaciones, String motivoBloqueo) {
        this.evento = evento;
        this.servicios = servicios;
        this.cotizaciones = cotizaciones;
        this.motivoBloqueo = motivoBloqueo;
    }

    public Evento getEvento() { return evento; }
    public List<Servicio> getServicios() { return servicios; }
    public List<Cotizacion> getCotizaciones() { return cotizaciones; }

    /** null si se puede cotizar; si no, el motivo para mostrar al personal. */
    public String getMotivoBloqueo() { return motivoBloqueo; }

    public boolean isPuedeCotizar() { return motivoBloqueo == null; }
}
