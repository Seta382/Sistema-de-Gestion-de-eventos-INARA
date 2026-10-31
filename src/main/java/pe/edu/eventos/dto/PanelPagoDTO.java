package pe.edu.eventos.dto;

import pe.edu.eventos.model.Evento;
import pe.edu.eventos.model.Pago;
import pe.edu.eventos.model.ResumenPago;

import java.util.List;

/** Todo lo que necesita la pantalla de pagos de un evento. */
public class PanelPagoDTO {

    private final Evento evento;
    private final ResumenPago resumen;
    private final List<Pago> pagos;
    private final String motivoBloqueo;

    public PanelPagoDTO(Evento evento, ResumenPago resumen, List<Pago> pagos, String motivoBloqueo) {
        this.evento = evento;
        this.resumen = resumen;
        this.pagos = pagos;
        this.motivoBloqueo = motivoBloqueo;
    }

    public Evento getEvento() { return evento; }
    public ResumenPago getResumen() { return resumen; }
    public List<Pago> getPagos() { return pagos; }

    /** null si se pueden registrar pagos; si no, el motivo. */
    public String getMotivoBloqueo() { return motivoBloqueo; }

    public boolean isPuedeRegistrar() {
        return motivoBloqueo == null && resumen.isTieneTotal() && !resumen.isPagadoCompleto();
    }
}
