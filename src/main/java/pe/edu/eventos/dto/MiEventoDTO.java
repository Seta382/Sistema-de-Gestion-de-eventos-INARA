package pe.edu.eventos.dto;

import pe.edu.eventos.model.Acuerdo;
import pe.edu.eventos.model.Cotizacion;
import pe.edu.eventos.model.Evento;
import pe.edu.eventos.model.Pago;
import pe.edu.eventos.model.ResumenPago;
import pe.edu.eventos.model.Seguimiento;

import java.util.List;

/** Todo lo que ve el cliente en la pantalla "Mi evento". */
public class MiEventoDTO {

    /** Un paso de la barra de progreso. estado: HECHO, ACTUAL o PENDIENTE. */
    public static class Etapa {
        private final String nombre;
        private final String estado;

        public Etapa(String nombre, String estado) {
            this.nombre = nombre;
            this.estado = estado;
        }

        public String getNombre() { return nombre; }
        public String getEstado() { return estado; }
    }

    private final List<Evento> eventos;
    private final Evento evento;
    private final List<Etapa> etapas;
    private final Cotizacion cotizacionAceptada;
    private final Acuerdo acuerdoVigente;
    private final ResumenPago resumen;
    private final List<Pago> pagos;
    private final List<Seguimiento> seguimientos;
    private final String proximoPaso;
    private final String proximoPasoUrl;
    private final String proximoPasoBoton;

    public MiEventoDTO(List<Evento> eventos, Evento evento, List<Etapa> etapas, Cotizacion cotizacionAceptada,
                       Acuerdo acuerdoVigente, ResumenPago resumen, List<Pago> pagos,
                       List<Seguimiento> seguimientos, String proximoPaso, String proximoPasoUrl,
                       String proximoPasoBoton) {
        this.eventos = eventos;
        this.evento = evento;
        this.etapas = etapas;
        this.cotizacionAceptada = cotizacionAceptada;
        this.acuerdoVigente = acuerdoVigente;
        this.resumen = resumen;
        this.pagos = pagos;
        this.seguimientos = seguimientos;
        this.proximoPaso = proximoPaso;
        this.proximoPasoUrl = proximoPasoUrl;
        this.proximoPasoBoton = proximoPasoBoton;
    }

    /** Todos los eventos del cliente (para el selector). */
    public List<Evento> getEventos() { return eventos; }
    public Evento getEvento() { return evento; }
    public List<Etapa> getEtapas() { return etapas; }
    public Cotizacion getCotizacionAceptada() { return cotizacionAceptada; }
    public Acuerdo getAcuerdoVigente() { return acuerdoVigente; }
    public ResumenPago getResumen() { return resumen; }

    /** Pagos que el cliente puede ver (sin los rechazados). */
    public List<Pago> getPagos() { return pagos; }
    public List<Seguimiento> getSeguimientos() { return seguimientos; }

    /** Texto de "qué sigue"; nunca null. */
    public String getProximoPaso() { return proximoPaso; }

    /** Enlace del botón del próximo paso (relativo al contexto); null si no hay botón. */
    public String getProximoPasoUrl() { return proximoPasoUrl; }
    public String getProximoPasoBoton() { return proximoPasoBoton; }
    public boolean isTieneBoton() { return proximoPasoUrl != null; }
    public boolean isCancelado() { return "CANCELADO".equals(evento.getEstado()); }
}
