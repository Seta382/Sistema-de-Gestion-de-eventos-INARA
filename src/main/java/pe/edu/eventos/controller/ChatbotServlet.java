package pe.edu.eventos.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pe.edu.eventos.dao.ClienteDAO;
import pe.edu.eventos.dao.impl.ClienteDAOImpl;
import pe.edu.eventos.dto.UsuarioDTO;
import pe.edu.eventos.facade.AcuerdoFacade;
import pe.edu.eventos.facade.CotizacionFacade;
import pe.edu.eventos.model.Cita;
import pe.edu.eventos.service.CitaService;
import pe.edu.eventos.util.Constantes;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Servlet del Chatbot Asistente Virtual "Sofía" para clientes y usuarios de INARA.
 * Procesa consultas en lenguaje natural, analiza la sesión y responde con datos
 * en tiempo real (cotizaciones, acuerdos, citas, servicios, contacto WhatsApp).
 */
@WebServlet("/api/chatbot")
public class ChatbotServlet extends HttpServlet {

    private final CotizacionFacade cotizacionFacade = new CotizacionFacade();
    private final AcuerdoFacade acuerdoFacade = new AcuerdoFacade();
    private final CitaService citaService = new CitaService();
    private final ClienteDAO clienteDAO = new ClienteDAOImpl();

    private static final String WHATSAPP_NUMERO = "51992937943";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        String mensaje = request.getParameter("mensaje");
        if (mensaje == null || mensaje.trim().isEmpty()) {
            mensaje = request.getParameter("pregunta");
        }
        if (mensaje == null) {
            mensaje = "";
        }
        mensaje = mensaje.trim();

        HttpSession session = request.getSession(false);
        UsuarioDTO usuario = (session != null) ? (UsuarioDTO) session.getAttribute(Constantes.SESION_USUARIO) : null;
        String contextPath = request.getContextPath();

        RespuestaChat respuesta = procesarConsulta(mensaje, usuario, contextPath);

        PrintWriter out = response.getWriter();
        out.print(respuesta.toJson());
        out.flush();
    }

    private RespuestaChat procesarConsulta(String mensajeRaw, UsuarioDTO usuario, String contextPath) {
        String msgNorm = normalizarTexto(mensajeRaw);
        boolean estaAutenticado = (usuario != null);
        String primerNombre = estaAutenticado && usuario.getNombre() != null ? usuario.getNombre().split(" ")[0] : "estimado(a)";
        String codigoCliente = estaAutenticado ? String.format("%04d", usuario.getId()) : "";

        // URL de WhatsApp personalizada
        String textoWa = "Hola INARA, " + (estaAutenticado
                ? "soy " + usuario.getNombreCompleto() + " (Cliente VIP #" + codigoCliente + "). Quisiera consultar con un asesor."
                : "quisiera solicitar información sobre sus servicios y cotizaciones de eventos.");
        String urlWhatsapp = "https://wa.me/" + WHATSAPP_NUMERO + "?text="
                + URLEncoder.encode(textoWa, StandardCharsets.UTF_8).replace("+", "%20");

        // Datos del cliente si está autenticado
        Integer idCliente = null;
        int cotizacionesPendientes = 0;
        int acuerdosPendientes = 0;
        List<Cita> misCitas = Collections.emptyList();

        if (estaAutenticado) {
            try {
                idCliente = clienteDAO.buscarIdPorUsuario(usuario.getId());
                cotizacionesPendientes = cotizacionFacade.contarPendientesDelCliente(usuario.getId());
                acuerdosPendientes = acuerdoFacade.contarPendientesDelCliente(usuario.getId());
                if (idCliente != null) {
                    misCitas = citaService.listarPorCliente(idCliente);
                }
            } catch (Exception e) {
                // Registro tolerante a fallos
            }
        }

        // 1. Mensaje vacío o saludo inicial / apertura del chat
        if (msgNorm.isEmpty() || msgNorm.equals("inicio") || msgNorm.equals("start") || msgNorm.equals("hola sofia")) {
            String saludo = estaAutenticado
                    ? "¡Hola <strong>" + primerNombre + "</strong>! ✨ Soy <strong>Sofía</strong>, tu asistente virtual de <strong>INARA</strong>.<br>Es un placer atenderte hoy en tu Portal Exclusivo. ¿En qué te puedo orientar sobre la planificación de tu evento?"
                    : "¡Hola! ✨ Bienvenido(a) a <strong>INARA</strong>, productora de eventos y experiencias exclusivas. Soy <strong>Sofía</strong>, tu asistente virtual. ¿En qué puedo ayudarte hoy?";

            List<String> sugerencias = estaAutenticado
                    ? Arrays.asList("📅 Agendar Cita", "💍 Paquetes de Bodas", "💰 Mis Cotizaciones", "🍾 Catering Gourmet", "💬 Hablar con Asesor")
                    : Arrays.asList("💍 Paquetes de Bodas", "🎆 Quinceañeros", "🍾 Catering de Autor", "📅 Solicitar Cita", "💬 WhatsApp");

            return new RespuestaChat(saludo, sugerencias, null, null);
        }

        // 2. Saludos genéricos
        if (contieneAlguno(msgNorm, "hola", "buenos dias", "buenas tardes", "buenas noches", "saludos", "que tal", "hey")) {
            String resp = "¡Hola " + (estaAutenticado ? "<strong>" + primerNombre + "</strong>" : "") + "! ✨ ¿Cómo puedo asistirte hoy? Puedo ayudarte a consultar paquetes, cotizaciones, citas con nuestros Wedding & Event Planners o dar seguimiento a tu celebración.";
            return new RespuestaChat(resp,
                    Arrays.asList("📅 Agendar Cita", "💍 Paquetes de Bodas", "💰 Cotizaciones", "💬 WhatsApp Asesor"),
                    null, null);
        }

        // 3. Identidad / Quién eres
        if (contieneAlguno(msgNorm, "quien eres", "como te llamas", "que eres", "presentate", "que puedes hacer", "ayuda", "menu")) {
            String resp = "Soy <strong>Sofía</strong>, la asistente virtual de <strong>INARA Eventos Luxury</strong>. 🌟<br><br>"
                    + "Puedo orientarte con:<br>"
                    + "• 💍 <strong>Bodas y Galas de Ensueño:</strong> Diseño conceptual, producción y logística.<br>"
                    + "• 🎆 <strong>Quinceañeros & Proms:</strong> Fiestas temáticas y tecnología audiovisual.<br>"
                    + "• 🍾 <strong>Catering & Mixología:</strong> Menús de autor y estaciones gourmet.<br>"
                    + "• 📅 <strong>Agendamiento de Citas:</strong> Presenciales en nuestro Showroom o virtuales.<br>"
                    + "• 💰 <strong>Cotizaciones & Acuerdos:</strong> Seguimiento del presupuesto y contratos.";
            return new RespuestaChat(resp,
                    Arrays.asList("💍 Paquetes de Bodas", "📅 Agendar Cita", "🍾 Catering", "💰 Presupuesto"),
                    null, null);
        }

        // 4. Agendar citas / Citas
        if (contieneAlguno(msgNorm, "cita", "citas", "agendar", "reunion", "entrevista", "visita", "showroom", "agendamiento")) {
            if (estaAutenticado) {
                StringBuilder detalleCitas = new StringBuilder();
                if (!misCitas.isEmpty()) {
                    Cita prox = misCitas.get(0);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    detalleCitas.append("<br><br>📌 <em>Actualmente tienes ").append(misCitas.size())
                            .append(" cita(s) registrada(s). Próxima: <strong>")
                            .append(sdf.format(prox.getFecha())).append(" a las ").append(prox.getHora())
                            .append(" (").append(prox.getModalidad()).append(")</strong></em>");
                }
                String resp = "¡Excelente, <strong>" + primerNombre + "</strong>! Puedes agendar una cita personalizada con nuestros directores de eventos, ya sea <strong>presencial en nuestro Showroom</strong> o <strong>100% virtual vía videollamada</strong>."
                        + detalleCitas
                        + "<br><br>Haz clic en el botón de abajo para reservar tu fecha y hora preferida:";
                return new RespuestaChat(resp,
                        Arrays.asList("📅 Ir al formulario de citas", "📋 Ver mis citas", "💬 Consultar por WhatsApp"),
                        contextPath + "/citas?accion=nueva", "📅 Agendar Nueva Cita");
            } else {
                String resp = "Para coordinar los detalles de tu evento de manera personalizada, ofrecemos citas <strong>presenciales en nuestro Showroom</strong> o <strong>videollamadas virtuales</strong>.<br><br>Si ya tienes cuenta, puedes iniciar sesión para agendar directamente, o comunicarte de inmediato con nuestro equipo por WhatsApp:";
                return new RespuestaChat(resp,
                        Arrays.asList("💬 Contactar por WhatsApp", "🔐 Iniciar Sesión", "💍 Ver Servicios"),
                        urlWhatsapp, "📲 Coordinar Cita por WhatsApp");
            }
        }

        // 5. Mis Citas (consulta de citas existentes)
        if (contieneAlguno(msgNorm, "mis citas", "tengo cita", "cuando es mi cita", "ver citas")) {
            if (estaAutenticado) {
                if (misCitas.isEmpty()) {
                    return new RespuestaChat("Actualmente no tienes citas programadas. Te invitamos a agendar una para comenzar a diseñar cada detalle de tu evento.",
                            Arrays.asList("📅 Agendar Cita", "💬 WhatsApp Asesor"),
                            contextPath + "/citas?accion=nueva", "📅 Agendar Cita Ahora");
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    StringBuilder sb = new StringBuilder("Tienes <strong>" + misCitas.size() + " cita(s) registrada(s)</strong>:<br><ul style='margin: 8px 0; padding-left: 18px;'>");
                    for (int i = 0; i < Math.min(misCitas.size(), 3); i++) {
                        Cita c = misCitas.get(i);
                        sb.append("<li><strong>").append(sdf.format(c.getFecha())).append("</strong> a las ").append(c.getHora())
                                .append(" &bull; <em>").append(c.getModalidad()).append("</em> (").append(c.getEstado()).append(")</li>");
                    }
                    sb.append("</ul>Puedes revisar el historial completo en tu panel:");
                    return new RespuestaChat(sb.toString(),
                            Arrays.asList("📅 Agendar Otra Cita", "🏠 Ir a Dashboard", "💬 WhatsApp"),
                            contextPath + "/citas", "📋 Ver Todas Mis Citas");
                }
            } else {
                return new RespuestaChat("Para revisar tus citas programadas debes iniciar sesión en el portal.",
                        Arrays.asList("🔐 Iniciar Sesión", "💬 Contactar WhatsApp"),
                        contextPath + "/login", "🔐 Iniciar Sesión");
            }
        }

        // 6. Bodas / Matrimonios
        if (contieneAlguno(msgNorm, "boda", "bodas", "matrimonio", "casamiento", "novia", "novio", "nupcial")) {
            String resp = "💍 <strong>Bodas Exclusivas INARA:</strong><br>"
                    + "Creamos bodas de ensueño donde el buen gusto y la sofisticación son protagonistas.<br><br>"
                    + "<strong>Nuestros paquetes nupciales integran:</strong><br>"
                    + "• Diseño integral y arquitectura floral personalizada.<br>"
                    + "• Estructuras de toldo arquitectónico & pistas de baile LED.<br>"
                    + "• Catering gourmet de 3 o 4 tiempos con maridaje de autor.<br>"
                    + "• Barra libre con mixología premium e iluminación robótica.<br>"
                    + "• Wedding Planner certificado y coordinación el día 'D'.<br><br>"
                    + "¿Deseas agendar una cita para conocer nuestras propuestas?";
            return new RespuestaChat(resp,
                    Arrays.asList("📅 Agendar Cita de Boda", "🍾 Ver Menús y Catering", "🎨 Diseño y Mobiliario", "💬 Cotizar por WhatsApp"),
                    estaAutenticado ? contextPath + "/citas?accion=nueva" : urlWhatsapp,
                    estaAutenticado ? "📅 Agendar Cita para Boda" : "📲 Cotizar Boda por WhatsApp");
        }

        // 7. Quinceañeros / Proms
        if (contieneAlguno(msgNorm, "quince", "quinceanero", "15 anos", "xv", "quinceanera", "prom", "graduacion", "promo")) {
            String resp = "🎆 <strong>Quinceañeros & Proms de Ensueño:</strong><br>"
                    + "Diseñamos fiestas temáticas inolvidables con la mejor tecnología de entretenimiento.<br><br>"
                    + "<strong>Incluye:</strong><br>"
                    + "• Escenografías temáticas (Gala, Glamour, Vintage, Fantasy, etc.).<br>"
                    + "• Shows de hora loca temáticos, robots LED y cabinas 360°.<br>"
                    + "• Iluminación inteligente y pantallas LED de alta definición.<br>"
                    + "• Mocktails (cócteles sin alcohol) gourmet y estaciones dulces.<br>"
                    + "• DJ en vivo profesional y producción sonora de discoteca.";
            return new RespuestaChat(resp,
                    Arrays.asList("📅 Agendar Cita", "🍾 Ver Catering", "💬 Cotizar Quinceañero"),
                    estaAutenticado ? contextPath + "/citas?accion=nueva" : urlWhatsapp,
                    "✨ Agendar Asesoría para Quinceañero");
        }

        // 8. Catering / Gastronomía / Bebidas
        if (contieneAlguno(msgNorm, "catering", "comida", "menu", "platos", "buffet", "cena", "degustacion", "trago", "bebida", "barra libre", "vino", "mixologia", "postre")) {
            String resp = "🍾 <strong>Catering de Autor & Gastronomía Selecta:</strong><br>"
                    + "Una propuesta culinaria de alta gama adaptada al perfil de tus invitados:<br><br>"
                    + "• <strong>Cenas Servidas:</strong> Menús de 3 o 4 tiempos con presentaciones de autor.<br>"
                    + "• <strong>Estaciones en Vivo:</strong> Sushi bar, tabla de quesos y fiambres ibéricos, ceviches y pastas.<br>"
                    + "• <strong>Mixología Exclusiva:</strong> Cócteles de autor, gin bar, espumantes y mocktails.<br>"
                    + "• <strong>Mesa de Postres Finos:</strong> Petit fours, macarons, bombones belgas y fuente de chocolate.<br>"
                    + "• Se incluye degustación previa antes del evento para los anfitriones.";
            return new RespuestaChat(resp,
                    Arrays.asList("💍 Paquetes de Boda", "📅 Agendar Degustación", "💬 Hablar con Asesor"),
                    estaAutenticado ? contextPath + "/citas?accion=nueva" : urlWhatsapp,
                    "🍷 Solicitar Asesoría de Catering");
        }

        // 9. Mobiliario / Decoración / Ambientación
        if (contieneAlguno(msgNorm, "mobiliario", "decoracion", "toldos", "toldo", "luces", "flores", "sillas", "mesas", "ambientacion", "centros de mesa", "chandelier")) {
            String resp = "🎨 <strong>Diseño, Decoración & Mobiliario de Alta Gama:</strong><br>"
                    + "Convertimos cualquier locación en un espacio mágico:<br><br>"
                    + "• <strong>Mobiliario Luxury:</strong> Sillas Phoenix, Dior, Tiffany y salas lounge en tonos blanco y dorado.<br>"
                    + "• <strong>Arquitectura Floral:</strong> Flores naturales importadas, arcos ceremoniales y hanging flowers.<br>"
                    + "• <strong>Estructuras & Toldos:</strong> Toldos arquitectónicos drapeados, toldos transparentes y estilo carpa europea.<br>"
                    + "• <strong>Iluminación Escénica:</strong> Chandeliers de cristal veneciano, cielo estrellado y luces cálidas vintage.";
            return new RespuestaChat(resp,
                    Arrays.asList("💍 Paquetes de Boda", "🍾 Ver Catering", "📅 Agendar Cita"),
                    estaAutenticado ? contextPath + "/citas?accion=nueva" : urlWhatsapp,
                    "✨ Coordinar Diseño de Decoración");
        }

        // 10. Cotizaciones / Precios / Presupuesto
        if (contieneAlguno(msgNorm, "cotizacion", "cotizaciones", "precio", "precios", "costo", "costos", "presupuesto", "cuanto cuesta", "tarifa", "valor")) {
            if (estaAutenticado) {
                StringBuilder sb = new StringBuilder();
                sb.append("💰 <strong>Cotizaciones y Presupuesto:</strong><br>");
                if (cotizacionesPendientes > 0) {
                    sb.append("Tienes <strong>").append(cotizacionesPendientes).append(" cotización(es) pendiente(s) de respuesta</strong> en tu portal.<br>")
                            .append("Puedes revisarlas en detalle, ver los servicios incluidos y aceptarlas o rechazarlas directamente.");
                    return new RespuestaChat(sb.toString(),
                            Arrays.asList("📋 Ver Mis Cotizaciones", "📅 Agendar Cita", "💬 WhatsApp Asesor"),
                            contextPath + "/mis-cotizaciones", "💰 Ver Mis Cotizaciones Pendientes");
                } else {
                    sb.append("Actualmente estás al día con tus cotizaciones. Cada cotización se diseña 100% a medida según el tipo de celebración, locación y número estimado de invitados.<br><br>")
                            .append("¿Te gustaría que te preparemos una propuesta personalizada?");
                    return new RespuestaChat(sb.toString(),
                            Arrays.asList("📅 Agendar Cita para Cotizar", "💬 Solicitar por WhatsApp"),
                            contextPath + "/citas?accion=nueva", "📅 Solicitar Nueva Cotización");
                }
            } else {
                String resp = "💰 <strong>Presupuestos a Medida:</strong><br>"
                        + "En <strong>INARA</strong> cada evento es único. Nuestras cotizaciones contemplan desde eventos íntimos hasta grandes recepciones de más de 500 personas.<br><br>"
                        + "Para darte un presupuesto exacto sin compromiso, agenda una cita con nosotros o contáctanos por WhatsApp indicándonos tu fecha y cantidad de invitados:";
                return new RespuestaChat(resp,
                        Arrays.asList("💬 Pedir Presupuesto por WhatsApp", "💍 Ver Paquetes de Boda", "🔐 Iniciar Sesión"),
                        urlWhatsapp, "📲 Solicitar Presupuesto Inmediato");
            }
        }

        // 11. Acuerdos / Contratos / Firmas
        if (contieneAlguno(msgNorm, "acuerdo", "acuerdos", "contrato", "contratos", "firma", "firmar", "terminos", "legal", "confirmar evento")) {
            if (estaAutenticado) {
                StringBuilder sb = new StringBuilder();
                sb.append("📑 <strong>Acuerdos y Términos Contractuales:</strong><br>");
                if (acuerdosPendientes > 0) {
                    sb.append("¡Tienes <strong>").append(acuerdosPendientes).append(" acuerdo(s) pendiente(s) de aceptación</strong>!<br>")
                            .append("Al aceptar el acuerdo de tu cotización aprobada, tu fecha queda formalmente reservada en nuestro calendario exclusivo.");
                    return new RespuestaChat(sb.toString(),
                            Arrays.asList("📑 Ver Mis Acuerdos", "💰 Ver Cotizaciones", "💬 WhatsApp"),
                            contextPath + "/mis-acuerdos", "📑 Revisar y Aceptar Acuerdos");
                } else {
                    sb.append("Todos tus acuerdos están al día. Una vez que aceptas una cotización, nuestro departamento legal y operativo genera el acuerdo con todas las garantías del servicio.");
                    return new RespuestaChat(sb.toString(),
                            Arrays.asList("📑 Ir a Mis Acuerdos", "🎉 Mi Evento", "💬 WhatsApp Asesor"),
                            contextPath + "/mis-acuerdos", "📑 Ver Acuerdos");
                }
            } else {
                String resp = "📑 En <strong>INARA</strong> brindamos máxima formalidad y seguridad. Una vez aprobada tu cotización, se suscribe un acuerdo de servicio con cronograma de entregables y garantías.";
                return new RespuestaChat(resp,
                        Arrays.asList("💬 Consultar con Asesor", "💍 Ver Servicios"),
                        urlWhatsapp, "📲 Consultar Términos por WhatsApp");
            }
        }

        // 12. Mi Evento / Estado / Seguimiento / Avances
        if (contieneAlguno(msgNorm, "mi evento", "estado", "avances", "seguimiento", "como va mi evento", "cronograma", "pagos")) {
            if (estaAutenticado) {
                String resp = "🎉 <strong>Seguimiento de tu Evento:</strong><br>"
                        + "En la sección <strong>Mi Evento</strong> puedes verificar el estado de avance, los servicios contratados, el resumen de pagos y abonos realizados, y la bitácora de preparativos.";
                return new RespuestaChat(resp,
                        Arrays.asList("🎉 Ir a Mi Evento", "💰 Mis Cotizaciones", "📅 Mis Citas"),
                        contextPath + "/mi-evento", "🎉 Ver Detalles de Mi Evento");
            } else {
                return new RespuestaChat("Para revisar el estado y seguimiento de tu evento registrado, por favor inicia sesión con tu cuenta de cliente.",
                        Arrays.asList("🔐 Iniciar Sesión", "💬 Contactar Asesor"),
                        contextPath + "/login", "🔐 Iniciar Sesión");
            }
        }

        // 13. Formas de Pago / Financiación / Cuentas bancarias
        if (contieneAlguno(msgNorm, "pago", "pagos", "forma de pago", "transferencia", "tarjeta", "cuotas", "adelanto", "yape", "plin", "banco", "factura", "boleta")) {
            String resp = "💳 <strong>Opciones de Pago & Facilidades:</strong><br>"
                    + "• <strong>Reserva Inicial:</strong> Con el 30% aseguras y bloqueas la fecha exclusiva de tu evento.<br>"
                    + "• <strong>Pagos Fraccionados:</strong> Cronograma de cuotas intermedias flexibles hasta 15 días antes de la fecha del evento.<br>"
                    + "• <strong>Medios Aceptados:</strong> Transferencias bancarias (BCP, BBVA, Interbank), tarjetas de crédito/débito (Visa, Mastercard, Amex), Yape/Plin empresarial o efectivo en oficina.<br>"
                    + "• Emitimos Factura o Boleta electrónica según lo requieras.";
            return new RespuestaChat(resp,
                    Arrays.asList("🎉 Ver Mis Pagos en Mi Evento", "📑 Mis Acuerdos", "💬 Consultar a Finanzas"),
                    estaAutenticado ? contextPath + "/mi-evento" : urlWhatsapp,
                    estaAutenticado ? "💳 Ver Estado de Mis Pagos" : "📲 Contactar Área de Pagos");
        }

        // 14. Asesor humano / WhatsApp / Teléfono
        if (contieneAlguno(msgNorm, "humano", "asesor", "persona", "telefono", "celular", "llamar", "whatsapp", "contactar", "hablar con alguien", "operador")) {
            String resp = "💬 <strong>Atención Personalizada:</strong><br>"
                    + "Nuestro equipo de asesores de eventos está listo para atenderte en tiempo real por WhatsApp al <strong>+51 992 937 943</strong>.<br><br>"
                    + "Haz clic en el enlace para iniciar la conversación directa:";
            return new RespuestaChat(resp,
                    Arrays.asList("💬 Abrir WhatsApp Ahora", "📅 Agendar Cita", "💍 Ver Paquetes"),
                    urlWhatsapp, "📲 Chatear por WhatsApp con Asesor");
        }

        // 15. Ubicación / Showroom / Horarios
        if (contieneAlguno(msgNorm, "donde estan", "ubicacion", "direccion", "lugar", "oficina", "horario", "horarios", "atencion")) {
            String resp = "📍 <strong>Showroom & Horarios de Atención:</strong><br>"
                    + "• <strong>Showroom Exclusivo INARA:</strong> Av. Principal Luxury Event Suites, San Isidro / Miraflores, Lima.<br>"
                    + "• <strong>Horario de Atención:</strong> Lunes a Sábado de 9:00 a.m. a 7:00 p.m.<br>"
                    + "• <em>Atendemos previa cita</em> para brindarte una atención privada con degustación y catálogo de materiales.";
            return new RespuestaChat(resp,
                    Arrays.asList("📅 Agendar Visita al Showroom", "💬 Contactar WhatsApp", "💍 Ver Servicios"),
                    estaAutenticado ? contextPath + "/citas?accion=nueva" : urlWhatsapp,
                    "📅 Agendar Visita en Showroom");
        }

        // 16. Agradecimientos
        if (contieneAlguno(msgNorm, "gracias", "muchas gracias", "excelente", "perfecto", "genial", "ok gracias", "entendido")) {
            String resp = "¡Ha sido un placer ayudarte, " + (estaAutenticado ? "<strong>" + primerNombre + "</strong>" : "") + "! 💖 Recuerda que en <strong>INARA</strong> hacemos de cada detalle un momento inolvidable. Si necesitas algo más, aquí estaré siempre.";
            return new RespuestaChat(resp,
                    Arrays.asList("💍 Ver Bodas", "📅 Agendar Cita", "💬 WhatsApp"),
                    null, null);
        }

        // 17. Despedida
        if (contieneAlguno(msgNorm, "adios", "chau", "hasta luego", "nos vemos", "bye")) {
            String resp = "¡Hasta pronto! Que tengas un maravilloso día. Estaremos listos para cuando desees dar el siguiente paso en tu evento. ✨";
            return new RespuestaChat(resp,
                    Arrays.asList("📅 Agendar Cita", "💬 Contactar WhatsApp"),
                    null, null);
        }

        // 18. Registro / Login (útil en página de login)
        if (contieneAlguno(msgNorm, "registrarse", "registro", "crear cuenta", "cuenta", "iniciar sesion", "login")) {
            if (estaAutenticado) {
                return new RespuestaChat("Ya te encuentras autenticado como <strong>" + usuario.getNombreCompleto() + "</strong> (" + usuario.getRol() + ").",
                        Arrays.asList("🏠 Ir al Dashboard", "🎉 Mi Evento", "📅 Mis Citas"),
                        contextPath + "/cliente/dashboard.jsp", "🏠 Ir a Mi Dashboard");
            } else {
                return new RespuestaChat("Para crear una nueva cuenta y acceder a presupuestos exclusivos, puedes registrarte en minutos:",
                        Arrays.asList("📝 Registrarme", "🔐 Iniciar Sesión", "💬 Pedir Ayuda"),
                        contextPath + "/cliente/registro.jsp", "📝 Crear Cuenta de Cliente");
            }
        }

        // Fallback por defecto: respuesta amable con sugerencias
        String resp = "Entiendo tu consulta sobre <em>\"" + escapeHtml(mensajeRaw) + "\"</em>. ✨<br>"
                + "Para brindarte una orientación exacta, ¿cuál de estos temas describe mejor lo que buscas?<br><br>"
                + "También puedes pulsar el botón inferior para conectarte de inmediato con un asesor humano por WhatsApp.";

        return new RespuestaChat(resp,
                Arrays.asList("💍 Paquetes de Bodas", "🎆 Quinceañeros", "📅 Agendar Cita", "💰 Cotizaciones", "🍾 Catering", "💬 Hablar con Asesor"),
                urlWhatsapp, "📲 Hablar con Asesor en WhatsApp");
    }

    private static String normalizarTexto(String s) {
        if (s == null) return "";
        String norm = Normalizer.normalize(s.toLowerCase(), Normalizer.Form.NFD);
        norm = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(norm).replaceAll("");
        norm = norm.replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
        return norm;
    }

    private static boolean contieneAlguno(String texto, String... palabras) {
        for (String p : palabras) {
            String pNorm = normalizarTexto(p);
            if (texto.contains(pNorm)) {
                return true;
            }
        }
        return false;
    }

    private static String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * Objeto que estructura la respuesta del chatbot para serializar a JSON.
     */
    public static class RespuestaChat {
        private final String respuesta;
        private final List<String> sugerencias;
        private final String accionUrl;
        private final String accionTexto;

        public RespuestaChat(String respuesta, List<String> sugerencias, String accionUrl, String accionTexto) {
            this.respuesta = respuesta != null ? respuesta : "";
            this.sugerencias = sugerencias != null ? sugerencias : Collections.emptyList();
            this.accionUrl = accionUrl;
            this.accionTexto = accionTexto;
        }

        public String toJson() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"status\":\"success\",");
            sb.append("\"respuesta\":\"").append(escapeJson(respuesta)).append("\",");

            sb.append("\"sugerencias\":[");
            for (int i = 0; i < sugerencias.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append("\"").append(escapeJson(sugerencias.get(i))).append("\"");
            }
            sb.append("],");

            if (accionUrl != null && !accionUrl.isEmpty()) {
                sb.append("\"accionUrl\":\"").append(escapeJson(accionUrl)).append("\",");
                sb.append("\"accionTexto\":\"").append(escapeJson(accionTexto != null ? accionTexto : "Ver más")).append("\"");
            } else {
                sb.append("\"accionUrl\":null,");
                sb.append("\"accionTexto\":null");
            }

            sb.append("}");
            return sb.toString();
        }

        private static String escapeJson(String s) {
            if (s == null) return "";
            StringBuilder sb = new StringBuilder(s.length() + 16);
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                switch (c) {
                    case '"':
                        sb.append("\\\"");
                        break;
                    case '\\':
                        sb.append("\\\\");
                        break;
                    case '\b':
                        sb.append("\\b");
                        break;
                    case '\f':
                        sb.append("\\f");
                        break;
                    case '\n':
                        sb.append("\\n");
                        break;
                    case '\r':
                        sb.append("\\r");
                        break;
                    case '\t':
                        sb.append("\\t");
                        break;
                    default:
                        if (c < 32) {
                            sb.append(String.format("\\u%04x", (int) c));
                        } else {
                            sb.append(c);
                        }
                        break;
                }
            }
            return sb.toString();
        }
    }
}
