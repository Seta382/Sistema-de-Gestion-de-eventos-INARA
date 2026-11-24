package pe.edu.eventos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import pe.edu.eventos.dao.AcuerdoDAO;
import pe.edu.eventos.dao.CotizacionDAO;
import pe.edu.eventos.dao.EventoDAO;
import pe.edu.eventos.model.Acuerdo;
import pe.edu.eventos.model.Cotizacion;
import pe.edu.eventos.model.DetalleCotizacion;
import pe.edu.eventos.model.Evento;
import pe.edu.eventos.model.Notificacion;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AcuerdoServiceTest {

    @Mock AcuerdoDAO acuerdoDAO;
    @Mock CotizacionDAO cotizacionDAO;
    @Mock EventoDAO eventoDAO;
    @Mock NotificacionService notificaciones;

    AcuerdoService service;

    @BeforeEach
    void preparar() {
        service = new AcuerdoService(acuerdoDAO, cotizacionDAO, eventoDAO);
        service.setNotificaciones(notificaciones);
    }

    // ---------- Auxiliares ----------

    private Evento evento(String estado) {
        Evento e = new Evento();
        e.setIdEvento(1);
        e.setEstado(estado);
        return e;
    }

    private Cotizacion cotizacion(String estado) {
        Cotizacion c = new Cotizacion();
        c.setIdCotizacion(5);
        c.setEstado(estado);
        return c;
    }

    private Acuerdo acuerdo(String estado, int idCliente, String estadoEvento) {
        Acuerdo a = new Acuerdo(1, "Condiciones");
        a.setIdAcuerdo(7);
        a.setEstado(estado);
        a.setIdCliente(idCliente);
        a.setEstadoEvento(estadoEvento);
        return a;
    }

    /** Deja el evento en un estado que permite crear acuerdo. */
    private void eventoListoParaAcuerdo() {
        when(eventoDAO.buscarPorId(1)).thenReturn(evento("COTIZADO"));
        when(cotizacionDAO.listarPorEvento(1)).thenReturn(List.of(cotizacion(Cotizacion.ACEPTADA)));
        when(acuerdoDAO.listarPorEvento(1)).thenReturn(Collections.emptyList());
    }

    // ---------- crear ----------

    @Test
    @DisplayName("crear: evento inexistente")
    void crearEventoInexistente() {
        when(eventoDAO.buscarPorId(1)).thenReturn(null);
        assertEquals("El evento indicado no existe.", service.crear(1, "Texto"));
        verify(acuerdoDAO, never()).registrar(any());
    }

    @Test
    @DisplayName("crear: evento cancelado no admite acuerdos")
    void crearEventoCancelado() {
        when(eventoDAO.buscarPorId(1)).thenReturn(evento("CANCELADO"));
        when(cotizacionDAO.listarPorEvento(1)).thenReturn(List.of(cotizacion(Cotizacion.ACEPTADA)));
        when(acuerdoDAO.listarPorEvento(1)).thenReturn(Collections.emptyList());
        String r = service.crear(1, "Texto");
        assertNotNull(r);
        assertTrue(r.contains("cancelado"));
    }

    @Test
    @DisplayName("crear: sin cotización aceptada")
    void crearSinCotizacionAceptada() {
        when(eventoDAO.buscarPorId(1)).thenReturn(evento("COTIZADO"));
        when(cotizacionDAO.listarPorEvento(1)).thenReturn(List.of(cotizacion(Cotizacion.PENDIENTE)));
        when(acuerdoDAO.listarPorEvento(1)).thenReturn(Collections.emptyList());
        String r = service.crear(1, "Texto");
        assertTrue(r.contains("primero el cliente debe aceptar una cotización"));
        verify(acuerdoDAO, never()).registrar(any());
    }

    @Test
    @DisplayName("crear: ya existe un acuerdo vigente")
    void crearConAcuerdoVigente() {
        when(eventoDAO.buscarPorId(1)).thenReturn(evento("COTIZADO"));
        when(cotizacionDAO.listarPorEvento(1)).thenReturn(List.of(cotizacion(Cotizacion.ACEPTADA)));
        when(acuerdoDAO.listarPorEvento(1)).thenReturn(List.of(acuerdo(Acuerdo.PENDIENTE, 1, "COTIZADO")));
        String r = service.crear(1, "Texto");
        assertTrue(r.contains("ya tiene el acuerdo #7"));
    }

    @Test
    @DisplayName("crear: un acuerdo cancelado no impide crear otro")
    void crearConAcuerdoCanceladoPrevio() {
        eventoListoParaAcuerdo();
        when(acuerdoDAO.listarPorEvento(1)).thenReturn(List.of(acuerdo(Acuerdo.CANCELADO, 1, "COTIZADO")));
        when(acuerdoDAO.registrar(any(Acuerdo.class))).thenReturn(10);
        assertNull(service.crear(1, "Texto"));
    }

    @Test
    @DisplayName("crear: condiciones vacías o nulas")
    void crearCondicionesVacias() {
        eventoListoParaAcuerdo();
        assertEquals("Las condiciones del acuerdo son obligatorias.", service.crear(1, "   "));
        assertEquals("Las condiciones del acuerdo son obligatorias.", service.crear(1, null));
        verify(acuerdoDAO, never()).registrar(any());
    }

    @Test
    @DisplayName("crear: 5000 caracteres es válido (valor límite)")
    void crearCondiciones5000() {
        eventoListoParaAcuerdo();
        when(acuerdoDAO.registrar(any(Acuerdo.class))).thenReturn(10);
        assertNull(service.crear(1, "a".repeat(5000)));
        verify(notificaciones).avisarCliente(anyInt(), eq(Notificacion.ACUERDO), anyString());
    }

    @Test
    @DisplayName("crear: 5001 caracteres es rechazado (valor límite)")
    void crearCondiciones5001() {
        eventoListoParaAcuerdo();
        String r = service.crear(1, "a".repeat(5001));
        assertTrue(r.contains("5000"));
        verify(acuerdoDAO, never()).registrar(any());
    }

    @Test
    @DisplayName("crear: si el DAO devuelve null no se avisa al cliente")
    void crearFallaElDao() {
        eventoListoParaAcuerdo();
        when(acuerdoDAO.registrar(any(Acuerdo.class))).thenReturn(null);
        String r = service.crear(1, "Texto");
        assertTrue(r.startsWith("No se pudo registrar el acuerdo"));
        verify(notificaciones, never()).avisarCliente(anyInt(), any(), anyString());
    }

    // ---------- actualizarCondiciones ----------

    @Test
    @DisplayName("actualizarCondiciones: solo si está pendiente")
    void actualizarNoPendiente() {
        when(acuerdoDAO.buscarPorId(7)).thenReturn(acuerdo(Acuerdo.ACEPTADO, 1, "CONFIRMADO"));
        String r = service.actualizarCondiciones(7, "Nuevo texto");
        assertTrue(r.contains("Solo se pueden editar"));
        verify(acuerdoDAO, never()).actualizarCondiciones(anyInt(), anyString());
    }

    @Test
    @DisplayName("actualizarCondiciones: acuerdo inexistente")
    void actualizarInexistente() {
        when(acuerdoDAO.buscarPorId(7)).thenReturn(null);
        assertEquals("El acuerdo indicado no existe.", service.actualizarCondiciones(7, "Texto"));
    }

    @Test
    @DisplayName("actualizarCondiciones: caso correcto")
    void actualizarCorrecto() {
        when(acuerdoDAO.buscarPorId(7)).thenReturn(acuerdo(Acuerdo.PENDIENTE, 1, "COTIZADO"));
        when(acuerdoDAO.actualizarCondiciones(7, "Nuevo texto")).thenReturn(true);
        assertNull(service.actualizarCondiciones(7, "  Nuevo texto  "));
    }

    // ---------- aceptar ----------

    @Test
    @DisplayName("aceptar: un cliente no puede aceptar el acuerdo de otro")
    void aceptarAcuerdoAjeno() {
        when(acuerdoDAO.buscarPorId(7)).thenReturn(acuerdo(Acuerdo.PENDIENTE, 1, "COTIZADO"));
        String r = service.aceptarComoCliente(7, 99);
        assertEquals("El acuerdo indicado no existe.", r);
        verify(acuerdoDAO, never()).aceptar(anyInt());
    }

    @Test
    @DisplayName("aceptar: el cliente dueño acepta y se avisa al personal")
    void aceptarComoClienteCorrecto() {
        when(acuerdoDAO.buscarPorId(7)).thenReturn(acuerdo(Acuerdo.PENDIENTE, 1, "COTIZADO"));
        when(acuerdoDAO.aceptar(7)).thenReturn(true);
        assertNull(service.aceptarComoCliente(7, 1));
        verify(notificaciones).avisarPersonal(anyInt(), eq(Notificacion.ACUERDO), anyString());
    }

    @Test
    @DisplayName("aceptar: el personal registra la aceptación y se avisa al cliente")
    void aceptarComoPersonalCorrecto() {
        when(acuerdoDAO.buscarPorId(7)).thenReturn(acuerdo(Acuerdo.PENDIENTE, 1, "COTIZADO"));
        when(acuerdoDAO.aceptar(7)).thenReturn(true);
        assertNull(service.aceptarComoPersonal(7));
        verify(notificaciones).avisarCliente(anyInt(), eq(Notificacion.ACUERDO), anyString());
    }

    @Test
    @DisplayName("aceptar: acuerdo inexistente")
    void aceptarInexistente() {
        when(acuerdoDAO.buscarPorId(7)).thenReturn(null);
        assertEquals("El acuerdo indicado no existe.", service.aceptarComoPersonal(7));
    }

    @Test
    @DisplayName("aceptar: acuerdo que ya no está pendiente")
    void aceptarNoPendiente() {
        when(acuerdoDAO.buscarPorId(7)).thenReturn(acuerdo(Acuerdo.CANCELADO, 1, "COTIZADO"));
        String r = service.aceptarComoCliente(7, 1);
        assertTrue(r.contains("ya no está pendiente"));
        verify(acuerdoDAO, never()).aceptar(anyInt());
    }

    @Test
    @DisplayName("aceptar: evento finalizado")
    void aceptarEventoCerrado() {
        when(acuerdoDAO.buscarPorId(7)).thenReturn(acuerdo(Acuerdo.PENDIENTE, 1, "FINALIZADO"));
        String r = service.aceptarComoCliente(7, 1);
        assertTrue(r.contains("finalizado"));
        verify(acuerdoDAO, never()).aceptar(anyInt());
    }

    @Test
    @DisplayName("aceptar: otra persona lo cambió antes (DAO devuelve false)")
    void aceptarConflicto() {
        when(acuerdoDAO.buscarPorId(7)).thenReturn(acuerdo(Acuerdo.PENDIENTE, 1, "COTIZADO"));
        when(acuerdoDAO.aceptar(7)).thenReturn(false);
        String r = service.aceptarComoCliente(7, 1);
        assertTrue(r.contains("cambió mientras lo revisabas"));
        verify(notificaciones, never()).avisarPersonal(anyInt(), any(), anyString());
    }

    // ---------- cancelar ----------

    @Test
    @DisplayName("cancelar: acuerdo aceptado con evento CONFIRMADO se puede cancelar")
    void cancelarAceptadoConfirmado() {
        when(acuerdoDAO.buscarPorId(7)).thenReturn(acuerdo(Acuerdo.ACEPTADO, 1, "CONFIRMADO"));
        when(acuerdoDAO.cancelar(7)).thenReturn(true);
        assertNull(service.cancelar(7));
        verify(notificaciones).avisarCliente(anyInt(), eq(Notificacion.ACUERDO), anyString());
    }

    @Test
    @DisplayName("cancelar: acuerdo aceptado con evento en otra etapa no se puede cancelar")
    void cancelarAceptadoEventoEnProceso() {
        when(acuerdoDAO.buscarPorId(7)).thenReturn(acuerdo(Acuerdo.ACEPTADO, 1, "EN_PROCESO"));
        String r = service.cancelar(7);
        assertTrue(r.contains("no se puede cancelar un acuerdo aceptado"));
        verify(acuerdoDAO, never()).cancelar(anyInt());
    }

    @Test
    @DisplayName("cancelar: acuerdo pendiente se cancela aunque el evento no esté CONFIRMADO")
    void cancelarPendiente() {
        when(acuerdoDAO.buscarPorId(7)).thenReturn(acuerdo(Acuerdo.PENDIENTE, 1, "COTIZADO"));
        when(acuerdoDAO.cancelar(7)).thenReturn(true);
        assertNull(service.cancelar(7));
    }

    @Test
    @DisplayName("cancelar: ya cancelado")
    void cancelarYaCancelado() {
        when(acuerdoDAO.buscarPorId(7)).thenReturn(acuerdo(Acuerdo.CANCELADO, 1, "COTIZADO"));
        assertEquals("Este acuerdo ya está cancelado.", service.cancelar(7));
    }

    @Test
    @DisplayName("cancelar: acuerdo inexistente")
    void cancelarInexistente() {
        when(acuerdoDAO.buscarPorId(7)).thenReturn(null);
        assertEquals("El acuerdo indicado no existe.", service.cancelar(7));
    }

    // ---------- generarBorrador ----------

    @Test
    @DisplayName("generarBorrador: incluye servicios, total y cláusulas fijas")
    void borradorCompleto() {
        Evento e = evento("COTIZADO");
        e.setNombre("Boda de Ana");
        e.setNombreCliente("Ana Pérez");

        DetalleCotizacion d = new DetalleCotizacion(1, 2, new BigDecimal("100.00"), new BigDecimal("200.00"));
        d.setNombreServicio("Catering");
        Cotizacion c = cotizacion(Cotizacion.ACEPTADA);
        c.setDetalles(List.of(d));
        c.setTotal(new BigDecimal("1500.50"));

        String texto = service.generarBorrador(e, c);

        assertTrue(texto.contains("Boda de Ana"));
        assertTrue(texto.contains("Catering x2"));
        assertTrue(texto.contains("Total acordado: S/ 1,500.50"));
        assertTrue(texto.contains("Adelanto del 50%"));
        assertTrue(texto.contains("7 días antes del evento"));
        assertTrue(texto.contains("15 días de anticipación"));
    }

    @Test
    @DisplayName("generarBorrador: sin cotización y con datos vacíos no falla")
    void borradorSinDatos() {
        String texto = service.generarBorrador(new Evento(), null);
        assertTrue(texto.contains("Evento INARA"));
        assertTrue(texto.contains("por definir"));
    }
}

