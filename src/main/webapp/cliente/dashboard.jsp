<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="pe.edu.eventos.dto.UsuarioDTO" %>
<%@ page import="pe.edu.eventos.util.Constantes" %>
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
    <style>
        :root {
            --bg-primary: #F8F4F1;
            --rose-light: #D8A7A7;
            --rose-medium: #C08585;
            --rose-dark: #A66A6A;
            --rose-deep: #743E3E;
            --gold: #C6A15B;
            --gold-light: #E7D5B3;
            --text-primary: #3D3333;
            --text-secondary: #706464;
            --white: #FFFFFF;
            --border-soft: #EFE8E2;
            --shadow-card: 0 12px 30px rgba(61, 51, 51, 0.06), 0 2px 8px rgba(198, 161, 91, 0.04);
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: 'Montserrat', sans-serif;
            background-color: var(--bg-primary);
            background-image: 
                radial-gradient(circle at 10% 10%, rgba(216, 167, 167, 0.16) 0%, transparent 40%),
                radial-gradient(circle at 90% 90%, rgba(198, 161, 91, 0.14) 0%, transparent 45%);
            min-height: 100vh;
            color: var(--text-primary);
            display: flex;
            flex-direction: column;
        }

        /* Barra de navegación superior */
        .portal-navbar {
            background-color: var(--white);
            border-bottom: 1px solid var(--border-soft);
            box-shadow: 0 4px 15px rgba(61, 51, 51, 0.03);
            position: sticky;
            top: 0;
            z-index: 100;
        }

        .portal-navbar::after {
            content: '';
            display: block;
            height: 3px;
            background: linear-gradient(90deg, var(--rose-light) 0%, var(--gold) 50%, var(--rose-light) 100%);
        }

        .navbar-content {
            max-width: 1140px;
            margin: 0 auto;
            padding: 16px 24px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .nav-brand {
            display: flex;
            align-items: baseline;
            gap: 12px;
            text-decoration: none;
        }

        .nav-logo-title {
            font-family: 'Playfair Display', Georgia, serif;
            font-size: 1.8rem;
            font-weight: 700;
            letter-spacing: 4px;
            color: var(--rose-dark);
            text-transform: uppercase;
        }

        .nav-logo-subtitle {
            font-size: 0.8rem;
            letter-spacing: 2px;
            text-transform: uppercase;
            color: var(--gold);
            font-weight: 600;
        }

        .nav-user-area {
            display: flex;
            align-items: center;
            gap: 18px;
        }

        .user-chip {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .user-avatar {
            width: 44px;
            height: 44px;
            border-radius: 50%;
            background: linear-gradient(135deg, var(--rose-dark) 0%, var(--rose-deep) 100%);
            color: var(--white);
            font-weight: 600;
            font-size: 1rem;
            display: flex;
            align-items: center;
            justify-content: center;
            border: 2px solid var(--gold-light);
            box-shadow: 0 4px 10px rgba(166, 106, 106, 0.25);
        }

        .user-info-text {
            display: flex;
            flex-direction: column;
            text-align: left;
        }

        .user-name {
            font-size: 0.92rem;
            font-weight: 600;
            color: var(--text-primary);
        }

        .user-role-badge {
            font-size: 0.72rem;
            color: var(--gold);
            font-weight: 600;
            letter-spacing: 1px;
            text-transform: uppercase;
        }

        .btn-logout {
            color: var(--rose-dark);
            text-decoration: none;
            padding: 8px 16px;
            border-radius: 20px;
            font-size: 0.82rem;
            font-weight: 600;
            border: 1.5px solid var(--rose-light);
            transition: all 0.25s ease;
            letter-spacing: 0.5px;
        }

        .btn-logout:hover {
            background-color: var(--rose-dark);
            color: var(--white);
            border-color: var(--rose-dark);
            box-shadow: 0 4px 12px rgba(166, 106, 106, 0.25);
            transform: translateY(-1px);
        }

        /* Contenido principal */
        .portal-main {
            max-width: 1140px;
            margin: 32px auto;
            padding: 0 24px;
            flex: 1;
            width: 100%;
        }

        /* Banner de bienvenida estilo evento */
        .hero-banner {
            background: linear-gradient(135deg, #743E3E 0%, #A66A6A 55%, #B88566 100%);
            color: var(--white);
            border-radius: 20px;
            padding: 38px 42px;
            position: relative;
            overflow: hidden;
            box-shadow: 0 16px 36px rgba(116, 62, 62, 0.18);
            margin-bottom: 32px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .hero-banner::before {
            content: '';
            position: absolute;
            top: -40px;
            right: -40px;
            width: 180px;
            height: 180px;
            background: radial-gradient(circle, rgba(231, 213, 179, 0.3) 0%, transparent 70%);
            border-radius: 50%;
        }

        .hero-banner::after {
            content: '';
            position: absolute;
            bottom: -50px;
            left: 20%;
            width: 150px;
            height: 150px;
            background: radial-gradient(circle, rgba(255, 255, 255, 0.15) 0%, transparent 70%);
            border-radius: 50%;
        }

        .hero-text {
            position: relative;
            z-index: 2;
        }

        .hero-greeting {
            font-size: 0.82rem;
            letter-spacing: 3px;
            text-transform: uppercase;
            color: var(--gold-light);
            font-weight: 600;
            margin-bottom: 8px;
            display: inline-block;
        }

        .hero-title {
            font-family: 'Playfair Display', Georgia, serif;
            font-size: 2.2rem;
            font-weight: 700;
            margin-bottom: 6px;
            letter-spacing: 0.5px;
        }

        .hero-tagline {
            font-family: 'Playfair Display', Georgia, serif;
            font-style: italic;
            font-size: 1.05rem;
            color: var(--gold-light);
            margin-bottom: 12px;
        }

        .status-pill {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            background: rgba(255, 255, 255, 0.15);
            backdrop-filter: blur(8px);
            padding: 6px 14px;
            border-radius: 50px;
            font-size: 0.78rem;
            color: #FFF6EE;
            border: 1px solid rgba(255, 255, 255, 0.25);
        }

        .status-dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background-color: #48BB78;
            box-shadow: 0 0 8px #48BB78;
        }

        /* Grilla de secciones */
        .dashboard-grid {
            display: grid;
            grid-template-columns: 1.15fr 0.85fr;
            gap: 28px;
            margin-bottom: 32px;
        }

        /* Tarjetas del panel */
        .card-panel {
            background-color: var(--white);
            border-radius: 18px;
            padding: 32px;
            box-shadow: var(--shadow-card);
            border: 1px solid var(--border-soft);
            position: relative;
        }

        .card-header-styled {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 24px;
            padding-bottom: 16px;
            border-bottom: 1px solid var(--border-soft);
        }

        .card-header-styled h3 {
            font-family: 'Playfair Display', Georgia, serif;
            font-size: 1.4rem;
            font-weight: 600;
            color: var(--text-primary);
        }

        .card-icon-badge {
            width: 38px;
            height: 38px;
            border-radius: 10px;
            background: rgba(216, 167, 167, 0.2);
            color: var(--rose-dark);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.1rem;
        }

        /* Lista de detalles de perfil */
        .profile-details-list {
            display: flex;
            flex-direction: column;
            gap: 16px;
        }

        .detail-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 12px 16px;
            background-color: #FAF8F6;
            border-radius: 12px;
            border: 1px solid rgba(239, 232, 226, 0.6);
            transition: all 0.2s ease;
        }

        .detail-row:hover {
            background-color: var(--white);
            border-color: var(--rose-light);
            box-shadow: 0 4px 12px rgba(61, 51, 51, 0.04);
        }

        .detail-label {
            font-size: 0.82rem;
            text-transform: uppercase;
            font-weight: 600;
            letter-spacing: 0.5px;
            color: var(--text-secondary);
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .detail-value {
            font-size: 0.95rem;
            font-weight: 600;
            color: var(--text-primary);
        }

        .role-tag {
            background: linear-gradient(135deg, var(--rose-light) 0%, var(--rose-dark) 100%);
            color: var(--white);
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 0.78rem;
            font-weight: 600;
            letter-spacing: 1px;
        }

        /* Resumen de actividad / Estado de eventos */
        .events-summary-cards {
            display: flex;
            flex-direction: column;
            gap: 16px;
        }

        .stat-box {
            background: #FAF8F6;
            padding: 18px 20px;
            border-radius: 14px;
            border-left: 4px solid var(--rose-dark);
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .stat-box.gold-accent {
            border-left-color: var(--gold);
        }

        .stat-label {
            font-size: 0.85rem;
            color: var(--text-secondary);
            font-weight: 500;
            margin-bottom: 4px;
        }

        .stat-main {
            font-size: 1.1rem;
            font-weight: 700;
            color: var(--text-primary);
        }

        .stat-action-btn {
            background-color: var(--rose-dark);
            color: var(--white);
            border: none;
            padding: 8px 16px;
            border-radius: 8px;
            font-size: 0.8rem;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.25s ease;
            text-decoration: none;
            display: inline-block;
        }

        .stat-action-btn:hover {
            background-color: var(--rose-deep);
            transform: translateY(-1px);
        }

        /* Sección de Servicios INARA */
        .services-section {
            background-color: var(--white);
            border-radius: 18px;
            padding: 32px;
            box-shadow: var(--shadow-card);
            border: 1px solid var(--border-soft);
            margin-bottom: 32px;
        }

        .services-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 20px;
            margin-top: 20px;
        }

        .service-card {
            background-color: #FAF8F6;
            padding: 24px 20px;
            border-radius: 14px;
            text-align: center;
            border: 1px solid transparent;
            transition: all 0.3s ease;
        }

        .service-card:hover {
            background-color: var(--white);
            border-color: var(--gold);
            transform: translateY(-4px);
            box-shadow: 0 10px 24px rgba(198, 161, 91, 0.12);
        }

        .service-icon {
            font-size: 2.2rem;
            margin-bottom: 12px;
            display: inline-block;
        }

        .service-title {
            font-family: 'Playfair Display', Georgia, serif;
            font-size: 1.15rem;
            font-weight: 600;
            color: var(--text-primary);
            margin-bottom: 6px;
        }

        .service-desc {
            font-size: 0.82rem;
            color: var(--text-secondary);
            line-height: 1.5;
        }

        /* Pie de página */
        .portal-footer {
            background-color: var(--white);
            border-top: 1px solid var(--border-soft);
            padding: 24px;
            text-align: center;
            font-size: 0.85rem;
            color: var(--text-secondary);
            margin-top: auto;
        }

        .portal-footer strong {
            color: var(--rose-dark);
            font-family: 'Playfair Display', Georgia, serif;
            font-size: 1rem;
        }

        /* Responsive */
        @media (max-width: 860px) {
            .navbar-content {
                flex-direction: column;
                gap: 14px;
                text-align: center;
            }

            .hero-banner {
                flex-direction: column;
                padding: 30px 24px;
                text-align: center;
                gap: 18px;
            }

            .dashboard-grid {
                grid-template-columns: 1fr;
            }

            .detail-row {
                flex-direction: column;
                align-items: flex-start;
                gap: 4px;
            }
        }
    </style>
</head>
<body>

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
                        <a href="mailto:contacto@inara.pe?subject=Consulta%20Evento%20INARA" class="stat-action-btn">Contactar</a>
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

        <!-- Sección de Catálogo de Servicios para Celebraciones -->
        <section class="services-section">
            <div class="card-header-styled" style="border-bottom: none; margin-bottom: 0;">
                <div>
                    <h3>Experiencias que Diseñamos para Ti</h3>
                    <p style="font-size: 0.88rem; color: var(--text-secondary); margin-top: 4px;">Servicios integrales para que tu celebración sea inolvidable</p>
                </div>
                <div class="card-icon-badge" style="background: rgba(198, 161, 91, 0.2); color: var(--gold);">&#127881;</div>
            </div>

            <div class="services-grid">
                <div class="service-card">
                    <span class="service-icon">&#128141;</span>
                    <h4 class="service-title">Bodas de Ensueño</h4>
                    <p class="service-desc">Diseño ceremonial, banquetes de gala y coordinación integral minuto a minuto.</p>
                </div>

                <div class="service-card">
                    <span class="service-icon">&#127878;</span>
                    <h4 class="service-title">Quinceañeros & Proms</h4>
                    <p class="service-desc">Ambientaciones temáticas vanguardistas, efectos de iluminación y show en vivo.</p>
                </div>

                <div class="service-card">
                    <span class="service-icon">&#127870;</span>
                    <h4 class="service-title">Catering de Autor</h4>
                    <p class="service-desc">Experiencias gastronómicas de primer nivel con maridaje y coctelería personalizada.</p>
                </div>

                <div class="service-card">
                    <span class="service-icon">&#127912;</span>
                    <h4 class="service-title">Diseño & Mobiliario</h4>
                    <p class="service-desc">Arreglos florales exclusivos, salas lounge, pistas led y toldos arquitectónicos.</p>
                </div>
            </div>
        </section>

    </main>

    <!-- Pie de página -->
    <footer class="portal-footer">
        <p>&copy; 2026 <strong>INARA</strong> — Gestión & Producción de Eventos Exclusivos. Todos los derechos reservados.</p>
    </footer>

</body>
</html>
