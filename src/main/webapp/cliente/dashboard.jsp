<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="pe.edu.eventos.dto.UsuarioDTO" %>
<%@ page import="pe.edu.eventos.util.Constantes" %>
<%@ page import="pe.edu.eventos.dao.ClienteDAO" %>
<%@ page import="pe.edu.eventos.dao.impl.ClienteDAOImpl" %>
<%@ page import="pe.edu.eventos.model.Cita" %>
<%@ page import="pe.edu.eventos.service.CitaService" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%

    UsuarioDTO usuario = (UsuarioDTO) session.getAttribute(Constantes.SESION_USUARIO);
    if (usuario == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }

    String iniciales = "";
    if (usuario.getNombre() != null && !usuario.getNombre().trim().isEmpty()) {
        iniciales += usuario.getNombre().trim().substring(0, 1).toUpperCase();
    }
    if (usuario.getApellido() != null && !usuario.getApellido().trim().isEmpty()) {
        iniciales += usuario.getApellido().trim().substring(0, 1).toUpperCase();
    }
    if (iniciales.isEmpty()) {
        iniciales = "CL";
    }
    // 3) Ahora sí, el bloque de WhatsApp
    final String WHATSAPP_VENTAS = "51992937943";

    String mensajeWhatsapp = "Hola INARA, soy " + usuario.getNombreCompleto()
            + " cliente VIP -" + String.format("%04d", usuario.getId())
            + ". Quisiera consultar sobre mi evento ☺ \uD83D\uDCDD.";
    String urlWhatsapp = "https://wa.me/" + WHATSAPP_VENTAS + "?text="
            + URLEncoder.encode(mensajeWhatsapp, StandardCharsets.UTF_8).replace("+", "%20");

    // 4) Lo demás que ya tenías (citas del cliente)
    ClienteDAO clienteDAO = new ClienteDAOImpl();
    Integer idCliente = clienteDAO.buscarIdPorUsuario(usuario.getId());
    List<Cita> misCitas = (idCliente != null)
            ? new CitaService().listarPorCliente(idCliente)
            : Collections.emptyList();


%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>INARA — Portal del Cliente</title>
    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&family=Playfair+Display:ital,wght@0,500;0,600;0,700;1,400;1,600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/cliente.css?v=<%= System.currentTimeMillis() %>">

</head>
<body class="page-cliente-dashboard">

<!-- Navegación Superior -->
<header class="portal-navbar">
    <div class="navbar-content">
        <a href="#" class="nav-brand">
            <span class="nav-logo-title">INARA</span>
            <span class="nav-logo-subtitle">Portal del Cliente</span>
        </a>

        <div class="nav-user-area">
            <div class="user-chip">
                <div class="user-avatar"><%= iniciales %></div>
                <div class="user-info-text">
                    <span class="user-name"><%= usuario.getNombreCompleto() %></span>
                    <span class="user-role-badge">Cliente VIP &bull; <%= usuario.getRol() %></span>
                </div>
            </div>

            <a href="${pageContext.request.contextPath}/login?accion=logout" class="btn-logout">
                Cerrar Sesión
            </a>
        </div>
    </div>
</header>

<!-- Contenido Principal -->
<main class="portal-main">

    <!-- Banner de Bienvenida -->
    <section class="hero-banner">
        <div class="hero-text">
            <span class="hero-greeting">Bienvenido a tu Espacio Exclusivo</span>
            <h1 class="hero-title"><%= usuario.getNombre() %> <%= usuario.getApellido() %></h1>
            <p class="hero-tagline">"Momentos únicos, eventos inolvidables"</p>
            <div class="status-pill">
                <span class="status-dot"></span>
                <span>Conexión Activa con Supabase Cloud</span>
            </div>
        </div>
    </section>

    <!-- Grilla de Información -->
    <div class="dashboard-grid">

        <!-- Tarjeta 1: Perfil y Datos Personales -->
        <section class="card-panel">
            <div class="card-header-styled">
                <div>
                    <h3>Perfil del Cliente</h3>
                    <p style="font-size: 0.85rem; color: var(--text-secondary); margin-top: 3px;">Tus datos registrados en la plataforma</p>
                </div>
                <div class="card-icon-badge">&#128100;</div>
            </div>

            <div class="profile-details-list">
                <div class="detail-row">
                    <span class="detail-label">&#128278; Código de Cliente (ID)</span>
                    <span class="detail-value" style="color: var(--rose-dark); font-family: monospace; font-size: 1.05rem;">#CLI-<%= String.format("%04d", usuario.getId()) %></span>
                </div>

                <div class="detail-row">
                    <span class="detail-label">&#128100; Nombre Completo</span>
                    <span class="detail-value"><%= usuario.getNombreCompleto() %></span>
                </div>

                <div class="detail-row">
                    <span class="detail-label">&#9993; Correo Electrónico</span>
                    <span class="detail-value"><%= usuario.getCorreo() %></span>
                </div>

                <div class="detail-row">
                    <span class="detail-label">&#128222; Teléfono / WhatsApp</span>
                    <span class="detail-value"><%= usuario.getTelefono() != null && !usuario.getTelefono().trim().isEmpty() ? usuario.getTelefono() : "No registrado" %></span>
                </div>

                <div class="detail-row">
                    <span class="detail-label">&#9874; Rol Asignado</span>
                    <span class="role-tag"><%= usuario.getRol() %></span>
                </div>
            </div>
        </section>

        <!-- Tarjeta 2: Estado de Cotizaciones y Atención -->
        <section class="card-panel">
            <div class="card-header-styled">
                <div>
                    <h3>Estado de tu Evento</h3>
                    <p style="font-size: 0.85rem; color: var(--text-secondary); margin-top: 3px;">Seguimiento personalizado</p>
                </div>
                <div class="card-icon-badge">&#10024;</div>
            </div>

            <div class="events-summary-cards">
                <div class="stat-box">
                    <div>
                        <div class="stat-label">Planificación Activa</div>
                        <div class="stat-main">En Coordinación</div>
                    </div>
                    <a href="<%= urlWhatsapp %>" target="_blank" rel="noopener noreferrer"
                       class="stat-action-btn btn-whatsapp" aria-label="Contactar por WhatsApp">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M17.472 14.382c-.297-.149-1.758-.867-2.03-.967-.273-.099-.471-.148-.67.15-.197.297-.767.966-.94 1.164-.173.199-.347.223-.644.075-.297-.15-1.255-.463-2.39-1.475-.883-.788-1.48-1.761-1.653-2.059-.173-.297-.018-.458.13-.606.134-.133.298-.347.446-.52.149-.174.198-.298.298-.497.099-.198.05-.371-.025-.52-.075-.149-.669-1.612-.916-2.207-.242-.579-.487-.5-.669-.51-.173-.008-.371-.01-.57-.01-.198 0-.52.074-.792.372-.272.297-1.04 1.016-1.04 2.479 0 1.462 1.065 2.875 1.213 3.074.149.198 2.096 3.2 5.077 4.487.709.306 1.262.489 1.694.625.712.227 1.36.195 1.871.118.571-.085 1.758-.719 2.006-1.413.248-.694.248-1.289.173-1.413-.074-.124-.272-.198-.57-.347m-5.421 7.403h-.004a9.87 9.87 0 01-5.031-1.378l-.361-.214-3.741.982.998-3.648-.235-.374a9.86 9.86 0 01-1.51-5.26c.001-5.45 4.436-9.884 9.888-9.884 2.64 0 5.122 1.03 6.988 2.898a9.825 9.825 0 012.893 6.994c-.003 5.45-4.437 9.884-9.885 9.884m8.413-18.297A11.815 11.815 0 0012.05 0C5.495 0 .16 5.335.157 11.892c0 2.096.547 4.142 1.588 5.945L.057 24l6.305-1.654a11.882 11.882 0 005.683 1.448h.005c6.554 0 11.89-5.335 11.893-11.893a11.821 11.821 0 00-3.48-8.413Z"/></svg>
                        Contactar
                    </a>
                </div>

                <div class="stat-box gold-accent">
                    <div>
                        <div class="stat-label">Asesor de Evento</div>
                        <div class="stat-main">Equipo INARA Luxury</div>
                    </div>
                    <span style="color: var(--gold); font-weight: 700; font-size: 1.2rem;">&#9733;&#9733;&#9733;&#9733;&#9733;</span>
                </div>

                <div class="stat-box">
                    <div>
                        <div class="stat-label">Cotizaciones Disponibles</div>
                        <div class="stat-main">Paquetes 2026</div>
                    </div>
                    <span style="font-size: 0.82rem; color: var(--rose-dark); font-weight: 600;">Actualizado</span>
                </div>
            </div>
        </section>

    </div>

    <!-- Sección de Mis Citas -->
    <section class="card-panel" style="margin-bottom: 32px;">
        <div class="card-header-styled">
            <div>
                <h3>Mis Próximas Citas</h3>
                <p style="font-size: 0.85rem; color: var(--text-secondary); margin-top: 3px;">Citas agendadas con el equipo INARA</p>
            </div>
            <div class="card-icon-badge">&#128197;</div>
        </div>

        <% if (misCitas.isEmpty()) { %>
        <div class="citas-empty">Todavía no tienes citas agendadas.</div>
        <% } else {
            SimpleDateFormat fmtDia = new SimpleDateFormat("dd", new Locale("es", "PE"));
            SimpleDateFormat fmtMes = new SimpleDateFormat("MMM", new Locale("es", "PE"));
            for (Cita cita : misCitas) {
        %>
        <div class="cita-item">
            <div class="cita-fecha-box">
                <span class="dia"><%= fmtDia.format(cita.getFecha()) %></span>
                <span class="hora"><%= fmtMes.format(cita.getFecha()).toUpperCase() %></span>
            </div>
            <div class="cita-info">
                <div class="cita-motivo"><%= cita.getMotivo() != null ? cita.getMotivo() : "Cita con INARA" %></div>
                <div class="cita-detalle">
                    <%= cita.getHora() %> &bull; <%= cita.getModalidad() %>
                    <% if (cita.getLugar() != null && !cita.getLugar().isBlank()) { %>
                    &bull; <%= cita.getLugar() %>
                    <% } %>
                </div>
            </div>
            <span class="estado-badge estado-<%= cita.getEstado() %>"><%= cita.getEstado() %></span>
        </div>
        <%  }
        } %>

        <div style="display: flex; gap: 12px; margin-top: 20px;">
            <a href="${pageContext.request.contextPath}/citas?accion=nueva" class="stat-action-btn">+ Agendar cita</a>
            <a href="${pageContext.request.contextPath}/citas" class="stat-action-btn" style="background-color: var(--gold);">Ver todas</a>
        </div>
    </section>

    <!-- Sección de Catálogo de Servicios para Celebraciones -->
    <section class="services-section">
        <div class="card-header-styled" style="border-bottom: none; margin-bottom: 0;">
            <div>
                <h3>Experiencias que Diseñamos para Ti</h3>
                <p style="font-size: 0.88rem; color: var(--text-secondary); margin-top: 4px;">Haz clic en cualquier experiencia para ver fotos reales de nuestros eventos</p>
            </div>
            <div class="card-icon-badge" style="background: rgba(198, 161, 91, 0.2); color: var(--gold);">&#127881;</div>
        </div>

        <div class="services-grid">
            <div class="service-card service-card-interactive" onclick="mostrarGaleria('bodas')" id="card-bodas" role="button" tabindex="0">
                <div class="service-img-cover">
                    <img src="${pageContext.request.contextPath}/images/bodas.svg" alt="Bodas de Ensueño" onerror="this.style.display='none'">
                    <span class="service-badge-pill">💍 Bodas</span>
                </div>
                <div class="service-card-body">
                    <h4 class="service-title">Bodas de Ensueño</h4>
                    <p class="service-desc">Diseño ceremonial, banquetes de gala y coordinación integral minuto a minuto.</p>
                    <span class="service-ver-btn">📸 Ver Galería de Fotos &rarr;</span>
                </div>
            </div>

            <div class="service-card service-card-interactive" onclick="mostrarGaleria('quince')" id="card-quince" role="button" tabindex="0">
                <div class="service-img-cover">
                    <img src="${pageContext.request.contextPath}/images/quince.svg" alt="Quinceañeros & Proms" onerror="this.style.display='none'">
                    <span class="service-badge-pill">🎆 Quinceañeros</span>
                </div>
                <div class="service-card-body">
                    <h4 class="service-title">Quinceañeros & Proms</h4>
                    <p class="service-desc">Ambientaciones temáticas vanguardistas, efectos de iluminación y show en vivo.</p>
                    <span class="service-ver-btn">📸 Ver Galería de Fotos &rarr;</span>
                </div>
            </div>

            <div class="service-card service-card-interactive" onclick="mostrarGaleria('catering')" id="card-catering" role="button" tabindex="0">
                <div class="service-img-cover">
                    <img src="${pageContext.request.contextPath}/images/catering.svg" alt="Catering de Autor" onerror="this.style.display='none'">
                    <span class="service-badge-pill">🍾 Catering</span>
                </div>
                <div class="service-card-body">
                    <h4 class="service-title">Catering de Autor</h4>
                    <p class="service-desc">Experiencias gastronómicas de primer nivel con maridaje y coctelería personalizada.</p>
                    <span class="service-ver-btn">📸 Ver Galería de Fotos &rarr;</span>
                </div>
            </div>

            <div class="service-card service-card-interactive" onclick="mostrarGaleria('mobiliario')" id="card-mobiliario" role="button" tabindex="0">
                <div class="service-img-cover">
                    <img src="${pageContext.request.contextPath}/images/mobiliario.svg" alt="Diseño & Mobiliario" onerror="this.style.display='none'">
                    <span class="service-badge-pill">🎨 Diseño</span>
                </div>
                <div class="service-card-body">
                    <h4 class="service-title">Diseño & Mobiliario</h4>
                    <p class="service-desc">Arreglos florales exclusivos, salas lounge, pistas led y toldos arquitectónicos.</p>
                    <span class="service-ver-btn">📸 Ver Galería de Fotos &rarr;</span>
                </div>
            </div>
        </div>

        <!-- Panel desplegable con las fotos de la experiencia seleccionada -->
        <div id="panelGaleria" class="galeria-desplegable" style="display: none;">
            <div class="galeria-desplegable-header">
                <div>
                    <span id="galeriaBadge" class="galeria-badge"></span>
                    <h3 id="galeriaTitulo" class="galeria-titulo-principal"></h3>
                    <p id="galeriaDesc" class="galeria-subtitulo"></p>
                </div>
                <button type="button" class="btn-cerrar-galeria" onclick="cerrarGaleria()">✕ Cerrar fotos</button>
            </div>

            <div id="galeriaFotos" class="galeria-fotos-grid">
                <!-- Se inyectan las fotos aquí -->
            </div>

            <div class="galeria-desplegable-footer">
                <a href="${pageContext.request.contextPath}/citas?accion=nueva" class="btn-agendar-servicio">✨ Agendar Cita para este Servicio</a>
            </div>
        </div>
    </section>

</main>

<script>
    const basePath = '${pageContext.request.contextPath}';
    const EXPERIENCIAS = {
        bodas: {
            icono: '💍 Bodas de Ensueño',
            titulo: 'Producción & Diseño Nupcial',
            desc: 'Creamos ambientes llenos de magia y romanticismo con el más alto estándar estético.',
            fotos: [
                { url: basePath + '/images/boda-1.jpg', fallback: basePath + '/images/bodas.svg', titulo: 'Banquete Nupcial de Gala' },
                { url: basePath + '/images/boda-2.jpg', fallback: basePath + '/images/bodas.svg', titulo: 'Altar & Ceremonia Floral' },
                { url: basePath + '/images/boda-3.jpg', fallback: basePath + '/images/bodas.svg', titulo: 'Momentos Inolvidables' },
                { url: basePath + '/images/boda-4.jpg', fallback: basePath + '/images/bodas.svg', titulo: 'Mesa de Honor Exclusiva' }
            ]
        },
        quince: {
            icono: '🎆 Quinceañeros & Proms',
            titulo: 'Fiestas Temáticas & Graduaciones',
            desc: 'Experiencias vibrantes con tecnología de iluminación, shows y ambientaciones personalizadas.',
            fotos: [
                { url: basePath + '/images/quince-1.jpg', fallback: basePath + '/images/quince.svg', titulo: 'Show de Luces & Celebración' },
                { url: basePath + '/images/quince-2.jpg', fallback: basePath + '/images/quince.svg', titulo: 'Pistas LED & Música en Vivo' },
                { url: basePath + '/images/quince-3.jpg', fallback: basePath + '/images/quince.svg', titulo: 'Ambientación Temática' },
                { url: basePath + '/images/quince-4.jpg', fallback: basePath + '/images/quince.svg', titulo: 'Gala & Fiesta Juvenil' }
            ]
        },
        catering: {
            icono: '🍾 Catering de Autor',
            titulo: 'Gastronomía & Coctelería Fina',
            desc: 'Platos de autor, maridaje selecto, estaciones en vivo y servicio de meseros de etiqueta.',
            fotos: [
                { url: basePath + '/images/catering-1.jpg', fallback: basePath + '/images/catering.svg', titulo: 'Bocadillos & Canapés Gourmet' },
                { url: basePath + '/images/catering-2.jpg', fallback: basePath + '/images/catering.svg', titulo: 'Barra Libre & Mixología' },
                { url: basePath + '/images/catering-3.jpg', fallback: basePath + '/images/catering.svg', titulo: 'Cena Servida de 4 Tiempos' },
                { url: basePath + '/images/catering-4.jpg', fallback: basePath + '/images/catering.svg', titulo: 'Estación de Postres Finos' }
            ]
        },
        mobiliario: {
            icono: '🎨 Diseño & Mobiliario',
            titulo: 'Decoración, Estructuras & Confort',
            desc: 'Salas lounge, chandeliers de cristal, pistas de baile y arquitectura de toldos.',
            fotos: [
                { url: basePath + '/images/mobiliario-1.jpg', fallback: basePath + '/images/mobiliario.svg', titulo: 'Centros Florales & Lámparas de Cristal' },
                { url: basePath + '/images/mobiliario-2.jpg', fallback: basePath + '/images/mobiliario.svg', titulo: 'Salas Lounge & Mobiliario Dorado' },
                { url: basePath + '/images/mobiliario-3.jpg', fallback: basePath + '/images/mobiliario.svg', titulo: 'Toldos Arquitectónicos & Velas' },
                { url: basePath + '/images/mobiliario-4.jpg', fallback: basePath + '/images/mobiliario.svg', titulo: 'Ambientación Escénica Integral' }
            ]
        }
    };

    function mostrarGaleria(clave) {
        const exp = EXPERIENCIAS[clave];
        if (!exp) return;

        // Quitar estado activo de otras tarjetas y marcar la actual
        document.querySelectorAll('.service-card-interactive').forEach(c => c.classList.remove('activa'));
        const cardActual = document.getElementById('card-' + clave);
        if (cardActual) cardActual.classList.add('activa');

        // Llenar datos en el panel desplegable
        document.getElementById('galeriaBadge').textContent = exp.icono;
        document.getElementById('galeriaTitulo').textContent = exp.titulo;
        document.getElementById('galeriaDesc').textContent = exp.desc;

        const contenedor = document.getElementById('galeriaFotos');
        const fallbackDefault = basePath + '/images/' + clave + '.svg';
        contenedor.innerHTML = exp.fotos.map(function(f) {
            var imgUrl = f.url || fallbackDefault;
            var fb = f.fallback || fallbackDefault;
            return '<div class="foto-item-card">' +
                '<img src="' + imgUrl + '" alt="' + f.titulo + '" loading="lazy" onerror="this.onerror=null; this.src=\'' + fb + '\';">' +
                '<span class="foto-pie">' + f.titulo + '</span>' +
                '</div>';
        }).join('');

        const panel = document.getElementById('panelGaleria');
        panel.style.display = 'block';

        // Desplazar suavemente la vista hacia el panel de fotos
        panel.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }

    function cerrarGaleria() {
        const panel = document.getElementById('panelGaleria');
        if (panel) panel.style.display = 'none';
        document.querySelectorAll('.service-card-interactive').forEach(c => c.classList.remove('activa'));
    }
</script>

<!-- Pie de página -->
<footer class="portal-footer">
    <p>&copy; 2026 <strong>INARA</strong> — Gestión & Producción de Eventos Exclusivos. Todos los derechos reservados.</p>
</footer>

</body>
</html>
