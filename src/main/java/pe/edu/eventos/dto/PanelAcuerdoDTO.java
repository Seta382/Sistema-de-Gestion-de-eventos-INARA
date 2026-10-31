package pe.edu.eventos.dto;

import pe.edu.eventos.model.Acuerdo;
import pe.edu.eventos.model.Cotizacion;
import pe.edu.eventos.model.Evento;

import java.util.List;

/** Todo lo que necesita la pantalla de acuerdo de un evento. */
public class PanelAcuerdoDTO {

    private final Evento evento;
    private final Cotizacion cotizacionAceptada;
    private final List<Acuerdo> acuerdos;
    private final String motivoBloqueo;
    private final String borrador;

    public PanelAcuerdoDTO(Evento evento, Cotizacion cotizacionAceptada, List<Acuerdo> acuerdos,
                           String motivoBloqueo, String borrador) {
        this.evento = evento;
        this.cotizacionAceptada = cotizacionAceptada;
        this.acuerdos = acuerdos;
        this.motivoBloqueo = motivoBloqueo;
        this.borrador = borrador;
    }

    public Evento getEvento() { return evento; }

    /** La cotización que respalda el acuerdo; null si todavía no hay una aceptada. */
    public Cotizacion getCotizacionAceptada() { return cotizacionAceptada; }

    public List<Acuerdo> getAcuerdos() { return acuerdos; }

    /** null si se puede crear un acuerdo; si no, el motivo para mostrar al personal. */
    public String getMotivoBloqueo() { return motivoBloqueo; }

    public boolean isPuedeCrear() { return motivoBloqueo == null; }

    /** Texto inicial sugerido para las condiciones. */
    public String getBorrador() { return borrador; }
}
