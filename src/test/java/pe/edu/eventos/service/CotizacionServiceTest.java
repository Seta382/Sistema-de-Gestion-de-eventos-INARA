package pe.edu.eventos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import pe.edu.eventos.dao.CotizacionDAO;
import pe.edu.eventos.dao.EventoDAO;
import pe.edu.eventos.dao.ServicioDAO;
import pe.edu.eventos.model.Cotizacion;
import pe.edu.eventos.model.DetalleCotizacion;
import pe.edu.eventos.model.Evento;
import pe.edu.eventos.model.Notificacion;
import pe.edu.eventos.model.Servicio;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CotizacionServiceTest {

    @Mock CotizacionDAO cotizacionDAO;
    @Mock EventoDAO eventoDAO;
    @Mock ServicioDAO servicioDAO;
    @Mock NotificacionService notificaciones;

    CotizacionService service;

    @BeforeEach
    void preparar() {
        service = new CotizacionService(cotizacionDAO, eventoDAO, servicioDAO);
        service.setNotificaciones(notificaciones);
    }

    // ---------- Auxiliares ----------

    private Evento evento(String estado) {
        Evento e = new Evento();
        e.setIdEvento(1);
        e.setEstado(estado);
        return e;
    }

    private Cotizacion cotizacion(String estado, int idCliente, String estadoEvento) {
        Cotizacion c = new Cotizacion();
        c.setIdCotizacion(5);
        c.setIdEvento(1);
        c.setEstado(estado);
        c.setIdCliente(idCliente);
        c.setEstadoEvento(estadoEvento);
        return c;
    }

    /** Servicio real con id 1 (la clase Servicio es un modelo simple, no hace falta simularlo). */
    private void servicio(String precio, boolean activo) {
        Servicio s = new Servicio(1, "Catering", "Servicio de prueba",
                new BigDecimal(precio), activo ? "ACTIVO" : "INACTIVO");
        when(servicioDAO.buscarPorId(1)).thenReturn(s);
    }

    private void eventoCotizable() {
        when(eventoDAO.buscarPorId(1)).thenReturn(evento("SOLICITADO"));
        when(cotizacionDAO.listarPorEvento(1)).thenReturn(Collections.emptyList());
        when(cotizacionDAO.registrar(any(Cotizacion.class), anyList())).thenReturn(10);
    }

    private Map<Integer, Integer> unServicio(Integer cantidad) {
        Map<Integer, Integer> m = new HashMap<>();
        m.put(1, cantidad);
        return m;
    }

    // ---------- crear: estado del evento ----------

    @Test
    @DisplayName("crear: evento inexistente")
    void crearEventoInexistente() {
        when(eventoDAO.buscarPorId(1)).thenReturn(null);
        assertEquals("El evento indicado no existe.", service.crear(1, unServicio(1), null, null));
    }

    @Test
    @DisplayName("crear: evento cancelado no admite cotizaciones")
    void crearEventoCancelado() {
        when(eventoDAO.buscarPorId(1)).thenReturn(evento("CANCELADO"));
        when(cotizacionDAO.listarPorEvento(1)).thenReturn(Collections.emptyList());
        String r = service.crear(1, unServicio(1), null, null);
        assertTrue(r.contains("cancelado"));
    }

    @Test
    @DisplayName("crear: evento con cotización aceptada")
    void crearConCotizacionAceptada() {
        when(eventoDAO.buscarPorId(1)).thenReturn(evento("COTIZADO"));
        when(cotizacionDAO.listarPorEvento(1))
                .thenReturn(List.of(cotizacion(Cotizacion.ACEPTADA, 1, "COTIZADO")));
        String r = service.crear(1, unServicio(1), null, null);
        assertTrue(r.contains("ya tiene la cotización #5 aceptada"));
    }

    // ---------- crear: servicios y cantidades ----------

    @Test
    @DisplayName("crear: sin servicios")
    void crearSinServicios() {
        eventoCotizable();
        assertEquals("Selecciona al menos un servicio para cotizar.",
                service.crear(1, new HashMap<>(), null, null));
        assertEquals("Selecciona al menos un servicio para cotizar.",
                service.crear(1, null, null, null));
    }

    @ParameterizedTest(name = "cantidad {0} es inválida")
    @ValueSource(ints = {0, -1, 1000})
    void crearCantidadInvalida(int cantidad) {
        eventoCotizable();
        servicio("100.00", true);
        String r = service.crear(1, unServicio(cantidad), null, null);
        assertTrue(r.contains("entre 1 y 999"));
        verify(cotizacionDAO, never()).registrar(any(), anyList());
    }

    @Test
    @DisplayName("crear: cantidad nula")
    void crearCantidadNula() {
        eventoCotizable();
        servicio("100.00", true);
        assertTrue(service.crear(1, unServicio(null), null, null).contains("entre 1 y 999"));
    }

    @ParameterizedTest(name = "cantidad {0} es válida")
    @ValueSource(ints = {1, 999})
    void crearCantidadValida(int cantidad) {
        eventoCotizable();
        servicio("10.00", true);
        assertNull(service.crear(1, unServicio(cantidad), null, null));
    }

    @Test
    @DisplayName("crear: servicio inexistente")
    void crearServicioInexistente() {
        eventoCotizable();
        when(servicioDAO.buscarPorId(1)).thenReturn(null);
        assertEquals("Uno de los servicios seleccionados ya no existe.",
                service.crear(1, unServicio(1), null, null));
    }

    @Test
    @DisplayName("crear: servicio dado de baja")
    void crearServicioInactivo() {
        eventoCotizable();
        servicio("100.00", false);
        String r = service.crear(1, unServicio(1), null, null);
        assertTrue(r.contains("está dado de baja"));
    }

    // ---------- crear: importes ----------

    @Test
    @DisplayName("crear: calcula subtotal y total con precios de la base")
    void crearCalculaTotales() {
        eventoCotizable();
        servicio("100.00", true);
        assertNull(service.crear(1, unServicio(2), new BigDecimal("50"), "  Nota  "));

        ArgumentCaptor<Cotizacion> cap = ArgumentCaptor.forClass(Cotizacion.class);
        ArgumentCaptor<List<DetalleCotizacion>> det = ArgumentCaptor.forClass(List.class);
        verify(cotizacionDAO).registrar(cap.capture(), det.capture());

        Cotizacion c = cap.getValue();
        assertEquals(new BigDecimal("200.00"), c.getSubtotal());
        assertEquals(new BigDecimal("50.00"), c.getDescuento());
        assertEquals(new BigDecimal("150.00"), c.getTotal());
        assertEquals("Nota", c.getObservaciones());
        assertEquals(1, det.getValue().size());
        verify(notificaciones).avisarCliente(anyInt(), eq(Notificacion.COTIZACION), anyString());
    }

    @Test
    @DisplayName("crear: descuento nulo se toma como cero")
    void crearSinDescuento() {
        eventoCotizable();
        servicio("100.00", true);
        assertNull(service.crear(1, unServicio(1), null, null));
        ArgumentCaptor<Cotizacion> cap = ArgumentCaptor.forClass(Cotizacion.class);
        verify(cotizacionDAO).registrar(cap.capture(), anyList());
        assertEquals(new BigDecimal("100.00"), cap.getValue().getTotal());
    }

    @Test
    @DisplayName("crear: descuento negativo")
    void crearDescuentoNegativo() {
        eventoCotizable();
        servicio("100.00", true);
        assertEquals("El descuento no puede ser negativo.",
                service.crear(1, unServicio(1), new BigDecimal("-1"), null));
    }

    @Test
    @DisplayName("crear: descuento igual al subtotal es rechazado (valor límite)")
    void crearDescuentoIgualAlSubtotal() {
        eventoCotizable();
        servicio("100.00", true);
        String r = service.crear(1, unServicio(1), new BigDecimal("100.00"), null);
        assertTrue(r.contains("menor al subtotal"));
    }

    @Test
    @DisplayName("crear: descuento justo debajo del subtotal es válido (valor límite)")
    void crearDescuentoJustoDebajo() {
        eventoCotizable();
        servicio("100.00", true);
        assertNull(service.crear(1, unServicio(1), new BigDecimal("99.99"), null));
    }

    @Test
    @DisplayName("crear: subtotal igual al máximo es válido")
    void crearSubtotalEnElMaximo() {
        eventoCotizable();
        servicio("99999999.99", true);
        assertNull(service.crear(1, unServicio(1), null, null));
    }

    @Test
    @DisplayName("crear: subtotal sobre el máximo es rechazado")
    void crearSubtotalSobreElMaximo() {
        eventoCotizable();
        servicio("100000000.00", true);
        assertEquals("El total de la cotización supera el máximo permitido.",
                service.crear(1, unServicio(1), null, null));
    }

    // ---------- crear: observaciones ----------

    @Test
    @DisplayName("crear: observaciones de 1000 caracteres son válidas")
    void crearObservaciones1000() {
        eventoCotizable();
        servicio("100.00", true);
        assertNull(service.crear(1, unServicio(1), null, "a".repeat(1000)));
    }

    @Test
    @DisplayName("crear: observaciones de 1001 caracteres son rechazadas")
    void crearObservaciones1001() {
        eventoCotizable();
        servicio("100.00", true);
        String r = service.crear(1, unServicio(1), null, "a".repeat(1001));
        assertTrue(r.contains("1000"));
        verify(cotizacionDAO, never()).registrar(any(), anyList());
    }

    @Test
    @DisplayName("crear: observaciones en blanco se guardan como null")
    void crearObservacionesEnBlanco() {
        eventoCotizable();
        servicio("100.00", true);
        service.crear(1, unServicio(1), null, "   ");
        ArgumentCaptor<Cotizacion> cap = ArgumentCaptor.forClass(Cotizacion.class);
        verify(cotizacionDAO).registrar(cap.capture(), anyList());
        assertNull(cap.getValue().getObservaciones());
    }

    @Test
    @DisplayName("crear: si el DAO falla no se avisa al cliente")
    void crearFallaElDao() {
        eventoCotizable();
        servicio("100.00", true);
        when(cotizacionDAO.registrar(any(Cotizacion.class), anyList())).thenReturn(null);
        String r = service.crear(1, unServicio(1), null, null);
        assertTrue(r.startsWith("No se pudo registrar la cotización"));
        verify(notificaciones, never()).avisarCliente(anyInt(), any(), anyString());
    }

    // ---------- responder ----------

    @Test
    @DisplayName("responder: un cliente no puede responder la cotización de otro")
    void responderCotizacionAjena() {
        when(cotizacionDAO.buscarPorId(5)).thenReturn(cotizacion(Cotizacion.PENDIENTE, 1, "SOLICITADO"));
        assertEquals("La cotización indicada no existe.", service.responderComoCliente(5, 99, true));
        verify(cotizacionDAO, never()).cambiarEstado(anyInt(), anyString(), anyString());
    }

    @Test
    @DisplayName("responder: cotización inexistente")
    void responderInexistente() {
        when(cotizacionDAO.buscarPorId(5)).thenReturn(null);
        assertEquals("La cotización indicada no existe.", service.responderComoPersonal(5, true));
    }

    @Test
    @DisplayName("responder: el cliente acepta y se avisa al personal")
    void clienteAcepta() {
        when(cotizacionDAO.buscarPorId(5)).thenReturn(cotizacion(Cotizacion.PENDIENTE, 1, "COTIZADO"));
        when(cotizacionDAO.cambiarEstado(5, Cotizacion.PENDIENTE, Cotizacion.ACEPTADA)).thenReturn(true);
        assertNull(service.responderComoCliente(5, 1, true));
        verify(notificaciones).avisarPersonal(anyInt(), eq(Notificacion.COTIZACION), anyString());
    }

    @Test
    @DisplayName("responder: el cliente rechaza")
    void clienteRechaza() {
        when(cotizacionDAO.buscarPorId(5)).thenReturn(cotizacion(Cotizacion.PENDIENTE, 1, "COTIZADO"));
        when(cotizacionDAO.cambiarEstado(5, Cotizacion.PENDIENTE, Cotizacion.RECHAZADA)).thenReturn(true);
        assertNull(service.responderComoCliente(5, 1, false));
    }

    @Test
    @DisplayName("responder: el personal registra la respuesta y se avisa al cliente")
    void personalRegistraRespuesta() {
        when(cotizacionDAO.buscarPorId(5)).thenReturn(cotizacion(Cotizacion.PENDIENTE, 1, "COTIZADO"));
        when(cotizacionDAO.cambiarEstado(5, Cotizacion.PENDIENTE, Cotizacion.ACEPTADA)).thenReturn(true);
        assertNull(service.responderComoPersonal(5, true));
        verify(notificaciones).avisarCliente(anyInt(), eq(Notificacion.COTIZACION), anyString());
    }

    @Test
    @DisplayName("responder: cotización que ya no está pendiente")
    void responderNoPendiente() {
        when(cotizacionDAO.buscarPorId(5)).thenReturn(cotizacion(Cotizacion.MODIFICADA, 1, "COTIZADO"));
        assertTrue(service.responderComoCliente(5, 1, true).contains("ya no está pendiente"));
    }

    @Test
    @DisplayName("responder: evento cancelado")
    void responderEventoCerrado() {
        when(cotizacionDAO.buscarPorId(5)).thenReturn(cotizacion(Cotizacion.PENDIENTE, 1, "CANCELADO"));
        assertTrue(service.responderComoCliente(5, 1, true).contains("cancelado"));
        verify(cotizacionDAO, never()).cambiarEstado(anyInt(), anyString(), anyString());
    }

    @Test
    @DisplayName("responder: otra persona la cambió antes (DAO devuelve false)")
    void responderConflicto() {
        when(cotizacionDAO.buscarPorId(5)).thenReturn(cotizacion(Cotizacion.PENDIENTE, 1, "COTIZADO"));
        when(cotizacionDAO.cambiarEstado(5, Cotizacion.PENDIENTE, Cotizacion.ACEPTADA)).thenReturn(false);
        assertTrue(service.responderComoCliente(5, 1, true).contains("cambió mientras la revisabas"));
        verify(notificaciones, never()).avisarPersonal(anyInt(), any(), anyString());
    }
}