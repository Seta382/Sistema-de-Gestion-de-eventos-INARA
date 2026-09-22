<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="pe.edu.eventos.dto.UsuarioDTO" %>
<%@ page import="pe.edu.eventos.util.Constantes" %>
<%
    UsuarioDTO usuario = (UsuarioDTO) session.getAttribute(Constantes.SESION_USUARIO);
    if (usuario == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    // Verificar que solo entren ADMIN o EMPLEADO
    String rol = usuario.getRol() != null ? usuario.getRol().toUpperCase() : "";
    if (!Constantes.ROL_ADMIN.equals(rol) && !Constantes.ROL_EMPLEADO.equals(rol)) {
        response.sendRedirect(request.getContextPath() + "/cliente/dashboard.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>INARA — Centro de Control Administrativo</title>
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
            --shadow-card: 0 14px 34px rgba(61, 51, 51, 0.07), 0 3px 10px rgba(198, 161, 91, 0.04);
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
                radial-gradient(circle at 5% 5%, rgba(216, 167, 167, 0.18) 0%, transparent 35%),
                radial-gradient(circle at 95% 95%, rgba(198, 161, 91, 0.15) 0%, transparent 40%);
            min-height: 100vh;
            color: var(--text-primary);
            display: flex;
            flex-direction: column;
        }

        /* Barra de Navegación */
        .admin-navbar {
            background-color: var(--white);
            border-bottom: 1px solid var(--border-soft);
            box-shadow: 0 4px 15px rgba(61, 51, 51, 0.03);
            position: sticky;
            top: 0;
            z-index: 100;
        }

        .admin-navbar::after {
            content: '';
            display: block;
            height: 3px;
            background: linear-gradient(90deg, var(--gold) 0%, var(--rose-dark) 50%, var(--gold) 100%);
        }

        .navbar-container {
            max-width: 1400px;
            margin: 0 auto;
            padding: 14px 24px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .nav-brand-group {
            display: flex;
            align-items: baseline;
            gap: 12px;
        }

        .brand-name {
            font-family: 'Playfair Display', Georgia, serif;
            font-size: 1.8rem;
            font-weight: 700;
            letter-spacing: 4px;
            color: var(--rose-dark);
            text-transform: uppercase;
        }

        .brand-badge {
            font-size: 0.75rem;
            letter-spacing: 2px;
            text-transform: uppercase;
            color: var(--gold);
            font-weight: 700;
            background: rgba(198, 161, 91, 0.12);
            padding: 4px 10px;
            border-radius: 50px;
            border: 1px solid rgba(198, 161, 91, 0.3);
        }

        .admin-user-profile {
            display: flex;
            align-items: center;
            gap: 18px;
        }

        .admin-chip {
            display: flex;
            align-items: center;
            gap: 12px;
            text-align: right;
        }

        .admin-name {
            font-size: 0.9rem;
            font-weight: 600;
            color: var(--text-primary);
        }

        .admin-role {
            font-size: 0.72rem;
            color: var(--rose-dark);
            font-weight: 700;
            letter-spacing: 1px;
            text-transform: uppercase;
        }

        .btn-exit {
            color: var(--rose-dark);
            text-decoration: none;
            padding: 8px 16px;
            border-radius: 20px;
            font-size: 0.82rem;
            font-weight: 600;
            border: 1.5px solid var(--rose-light);
            transition: all 0.25s ease;
        }

        .btn-exit:hover {
            background-color: var(--rose-dark);
            color: var(--white);
            border-color: var(--rose-dark);
            transform: translateY(-1px);
        }

        /* Contenedor General */
        .admin-main {
            max-width: 1400px;
            margin: 24px auto 40px auto;
            padding: 0 24px;
            flex: 1;
            width: 100%;
        }

        /* Franja superior de Métricas Rápidas */
        .kpi-row {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 18px;
            margin-bottom: 28px;
        }

        .kpi-card {
            background: var(--white);
            border-radius: 16px;
            padding: 20px 24px;
            border: 1px solid var(--border-soft);
            box-shadow: 0 6px 20px rgba(61, 51, 51, 0.04);
            display: flex;
            align-items: center;
            justify-content: space-between;
            transition: all 0.25s ease;
        }

        .kpi-card:hover {
            transform: translateY(-2px);
            border-color: var(--gold);
        }

        .kpi-icon {
            width: 48px;
            height: 48px;
            border-radius: 12px;
            background: rgba(216, 167, 167, 0.2);
            color: var(--rose-dark);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.4rem;
        }

        .kpi-card.gold .kpi-icon {
            background: rgba(198, 161, 91, 0.18);
            color: var(--gold);
        }

        .kpi-label {
            font-size: 0.8rem;
            color: var(--text-secondary);
            font-weight: 500;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 4px;
        }

        .kpi-value {
            font-size: 1.5rem;
            font-weight: 700;
            color: var(--text-primary);
        }

        /* Estructura Dividida 50% / 50% */
        .split-workspace {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 28px;
            align-items: start;
        }

        /* Panel Izquierdo: Eventos Animados */
        .col-events-monitor {
            background: var(--white);
            border-radius: 20px;
            padding: 30px;
            border: 1px solid var(--border-soft);
            box-shadow: var(--shadow-card);
            position: relative;
        }

        .section-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
            padding-bottom: 14px;
            border-bottom: 1px solid var(--border-soft);
        }

        .section-title {
            font-family: 'Playfair Display', Georgia, serif;
            font-size: 1.45rem;
            font-weight: 700;
            color: var(--text-primary);
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .live-pulse {
            width: 10px;
            height: 10px;
            background-color: #38A169;
            border-radius: 50%;
            display: inline-block;
            box-shadow: 0 0 0 0 rgba(56, 161, 105, 0.7);
            animation: pulse-green 1.8s infinite;
        }

        @keyframes pulse-green {
            0% {
                transform: scale(0.95);
                box-shadow: 0 0 0 0 rgba(56, 161, 105, 0.7);
            }
            70% {
                transform: scale(1);
                box-shadow: 0 0 0 8px rgba(56, 161, 105, 0);
            }
            100% {
                transform: scale(0.95);
                box-shadow: 0 0 0 0 rgba(56, 161, 105, 0);
            }
        }

        /* Lista y tarjetas animadas */
        .events-stream {
            display: flex;
            flex-direction: column;
            gap: 16px;
            max-height: 640px;
            overflow-y: auto;
            padding-right: 6px;
        }

        .events-stream::-webkit-scrollbar {
            width: 6px;
        }

        .events-stream::-webkit-scrollbar-thumb {
            background-color: var(--rose-light);
            border-radius: 10px;
        }

        .event-live-card {
            background: #FAF8F6;
            border: 1.5px solid rgba(239, 232, 226, 0.8);
            border-radius: 16px;
            padding: 20px;
            position: relative;
            overflow: hidden;
            transition: all 0.3s ease;
            animation: fadeInUp 0.5s ease-out;
        }

        .event-live-card:hover {
            background: var(--white);
            border-color: var(--gold);
            transform: translateX(4px);
            box-shadow: 0 8px 24px rgba(198, 161, 91, 0.12);
        }

        @keyframes fadeInUp {
            from {
                opacity: 0;
                transform: translateY(12px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        .event-top-info {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            margin-bottom: 10px;
        }

        .event-name {
            font-family: 'Playfair Display', Georgia, serif;
            font-size: 1.15rem;
            font-weight: 700;
            color: var(--text-primary);
        }

        .event-category-badge {
            font-size: 0.72rem;
            font-weight: 700;
            letter-spacing: 1px;
            text-transform: uppercase;
            padding: 4px 10px;
            border-radius: 20px;
            background: rgba(216, 167, 167, 0.25);
            color: var(--rose-deep);
        }

        .event-category-badge.gold {
            background: rgba(198, 161, 91, 0.2);
            color: #8C6A29;
        }

        .event-details-row {
            display: flex;
            flex-wrap: wrap;
            gap: 16px;
            font-size: 0.82rem;
            color: var(--text-secondary);
            margin-bottom: 14px;
        }

        .event-detail-item {
            display: flex;
            align-items: center;
            gap: 5px;
        }

        /* Barra de Progreso Animada */
        .progress-container {
            margin-top: 10px;
        }

        .progress-header {
            display: flex;
            justify-content: space-between;
            font-size: 0.78rem;
            font-weight: 600;
            margin-bottom: 6px;
            color: var(--text-secondary);
        }

        .progress-bar-bg {
            width: 100%;
            height: 7px;
            background-color: #EAE3DE;
            border-radius: 10px;
            overflow: hidden;
        }

        .progress-bar-fill {
            height: 100%;
            background: linear-gradient(90deg, var(--rose-light) 0%, var(--rose-dark) 70%, var(--gold) 100%);
            border-radius: 10px;
            transition: width 1s ease-in-out;
        }

        /* Panel Derecho: Centro de Registro y Operaciones */
        .col-register-center {
            background: var(--white);
            border-radius: 20px;
            padding: 30px;
            border: 1px solid var(--border-soft);
            box-shadow: var(--shadow-card);
        }

        /* Tabs de Selección */
        .operation-tabs {
            display: flex;
            gap: 8px;
            background: #FAF8F6;
            padding: 6px;
            border-radius: 14px;
            border: 1px solid var(--border-soft);
            margin-bottom: 24px;
            overflow-x: auto;
        }

        .tab-btn {
            flex: 1;
            padding: 10px 14px;
            border: none;
            background: transparent;
            border-radius: 10px;
            font-size: 0.82rem;
            font-weight: 600;
            color: var(--text-secondary);
            font-family: 'Montserrat', sans-serif;
            cursor: pointer;
            transition: all 0.25s ease;
            white-space: nowrap;
            text-align: center;
        }

        .tab-btn.active {
            background-color: var(--white);
            color: var(--rose-dark);
            box-shadow: 0 4px 12px rgba(61, 51, 51, 0.08);
            border: 1px solid rgba(216, 167, 167, 0.35);
        }

        /* Formularios dentro de los Tabs */
        .tab-content {
            display: none;
            animation: fadeIn 0.3s ease;
        }

        .tab-content.active {
            display: block;
        }

        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(6px); }
            to { opacity: 1; transform: translateY(0); }
        }

        .admin-form {
            display: flex;
            flex-direction: column;
            gap: 16px;
        }

        .form-row-2 {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 14px;
        }

        .field-group {
            display: flex;
            flex-direction: column;
            text-align: left;
        }

        .field-group label {
            font-size: 0.8rem;
            font-weight: 600;
            color: var(--text-primary);
            margin-bottom: 6px;
            letter-spacing: 0.4px;
            text-transform: uppercase;
        }

        .field-group input,
        .field-group select,
        .field-group textarea {
            width: 100%;
            padding: 12px 14px;
            border: 1.5px solid var(--border-soft);
            border-radius: 10px;
            background-color: #FAF8F6;
            font-size: 0.9rem;
            color: var(--text-primary);
            font-family: 'Montserrat', sans-serif;
            outline: none;
            transition: all 0.25s ease;
        }

        .field-group input:focus,
        .field-group select:focus,
        .field-group textarea:focus {
            background-color: var(--white);
            border-color: var(--rose-dark);
            box-shadow: 0 0 0 3px rgba(216, 167, 167, 0.25);
        }

        .btn-register-action {
            width: 100%;
            background: linear-gradient(135deg, var(--rose-dark) 0%, var(--rose-deep) 100%);
            color: var(--white);
            padding: 14px 20px;
            border: none;
            border-radius: 10px;
            font-size: 0.9rem;
            font-weight: 600;
            letter-spacing: 1.5px;
            text-transform: uppercase;
            font-family: 'Montserrat', sans-serif;
            cursor: pointer;
            box-shadow: 0 6px 18px rgba(116, 62, 62, 0.25);
            transition: all 0.3s ease;
            margin-top: 10px;
        }

        .btn-register-action:hover {
            background: linear-gradient(135deg, #8E5252 0%, #5E2D2D 100%);
            transform: translateY(-2px);
            box-shadow: 0 8px 22px rgba(116, 62, 62, 0.35);
        }

        /* Notificación Toast */
        .toast-notification {
            position: fixed;
            bottom: 30px;
            right: 30px;
            background: #2F6B4F;
            color: var(--white);
            padding: 16px 24px;
            border-radius: 12px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.2);
            font-size: 0.9rem;
            display: none;
            align-items: center;
            gap: 12px;
            z-index: 1000;
            animation: slideInRight 0.35s ease;
        }

        @keyframes slideInRight {
            from { transform: translateX(100%); opacity: 0; }
            to { transform: translateX(0); opacity: 1; }
        }

        /* Footer */
        .admin-footer {
            background: var(--white);
            border-top: 1px solid var(--border-soft);
            padding: 20px 24px;
            text-align: center;
            font-size: 0.82rem;
            color: var(--text-secondary);
            margin-top: auto;
        }

        /* Responsive */
        @media (max-width: 992px) {
            .split-workspace {
                grid-template-columns: 1fr;
            }
            .form-row-2 {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body>

    <!-- Navegación Superior de Administración -->
    <header class="admin-navbar">
        <div class="navbar-container">
            <div class="nav-brand-group">
                <span class="brand-name">INARA</span>
                <span class="brand-badge">Control & Producción</span>
            </div>

            <div class="admin-user-profile">
                <div class="admin-chip">
                    <div>
                        <div class="admin-name"><%= usuario.getNombreCompleto() %></div>
                        <div class="admin-role">👑 <%= usuario.getRol() %> &bull; Supabase Cloud</div>
                    </div>
                </div>

                <a href="${pageContext.request.contextPath}/login?accion=logout" class="btn-exit">
                    Cerrar Sesión
                </a>
            </div>
        </div>
    </header>

    <!-- Contenido Principal -->
    <main class="admin-main">

        <!-- Métricas Rápidas -->
        <section class="kpi-row">
            <div class="kpi-card">
                <div>
                    <div class="kpi-label">Eventos en Cronograma</div>
                    <div class="kpi-value" id="count-eventos">8 Activos</div>
                </div>
                <div class="kpi-icon">&#127878;</div>
            </div>

            <div class="kpi-card gold">
                <div>
                    <div class="kpi-label">En Montaje Hoy</div>
                    <div class="kpi-value">2 Locales</div>
                </div>
                <div class="kpi-icon">&#10024;</div>
            </div>

            <div class="kpi-card">
                <div>
                    <div class="kpi-label">Insumos Registrados</div>
                    <div class="kpi-value">145 Ítems</div>
                </div>
                <div class="kpi-icon">&#128230;</div>
            </div>

            <div class="kpi-card gold">
                <div>
                    <div class="kpi-label">Proveedores Aliados</div>
                    <div class="kpi-value">18 Empresas</div>
                </div>
                <div class="kpi-icon">&#128666;</div>
            </div>
        </section>

        <!-- Espacio de Trabajo Dividido: 50% Eventos Animados / 50% Registro -->
        <div class="split-workspace">

            <!-- MITAD IZQUIERDA: Monitor Animado de Eventos en Tiempo Real -->
            <section class="col-events-monitor">
                <div class="section-header">
                    <h2 class="section-title">
                        <span class="live-pulse"></span>
                        Eventos en Ejecución
                    </h2>
                    <span style="font-size: 0.8rem; color: var(--gold); font-weight: 600;">EN TIEMPO REAL</span>
                </div>

                <div class="events-stream" id="eventsStream">

                    <!-- Evento 1 -->
                    <article class="event-live-card">
                        <div class="event-top-info">
                            <div>
                                <h3 class="event-name">&#128141; Boda Romántica "Hacienda San José"</h3>
                                <p style="font-size: 0.82rem; color: var(--rose-dark); font-weight: 600; margin-top: 2px;">Titulares: Andrea & Sebastián</p>
                            </div>
                            <span class="event-category-badge gold">Boda de Gala</span>
                        </div>

                        <div class="event-details-row">
                            <div class="event-detail-item">&#128197; 26 Sep 2026</div>
                            <div class="event-detail-item">&#128338; 18:00 hrs</div>
                            <div class="event-detail-item">&#128101; 250 Invitados</div>
                            <div class="event-detail-item">&#128176; S/ 48,000</div>
                        </div>

                        <div class="progress-container">
                            <div class="progress-header">
                                <span>🟢 Fase: Montaje Floral & Estructuras</span>
                                <span>85%</span>
                            </div>
                            <div class="progress-bar-bg">
                                <div class="progress-bar-fill" style="width: 85%;"></div>
                            </div>
                        </div>
                    </article>

                    <!-- Evento 2 -->
                    <article class="event-live-card">
                        <div class="event-top-info">
                            <div>
                                <h3 class="event-name">&#127878; Quinceañero "Salón Imperial Palace"</h3>
                                <p style="font-size: 0.82rem; color: var(--rose-dark); font-weight: 600; margin-top: 2px;">Quinceañera: Valeria Rivas</p>
                            </div>
                            <span class="event-category-badge">Quinceañero</span>
                        </div>

                        <div class="event-details-row">
                            <div class="event-detail-item">&#128197; 03 Oct 2026</div>
                            <div class="event-detail-item">&#128338; 20:00 hrs</div>
                            <div class="event-detail-item">&#128101; 180 Invitados</div>
                            <div class="event-detail-item">&#128176; S/ 32,500</div>
                        </div>

                        <div class="progress-container">
                            <div class="progress-header">
                                <span>🟡 Fase: Coordinación de Iluminación & DJ</span>
                                <span>60%</span>
                            </div>
                            <div class="progress-bar-bg">
                                <div class="progress-bar-fill" style="width: 60%;"></div>
                            </div>
                        </div>
                    </article>

                    <!-- Evento 3 -->
                    <article class="event-live-card">
                        <div class="event-top-info">
                            <div>
                                <h3 class="event-name">&#127870; Gala Corporativa Tech "Summit 2026"</h3>
                                <p style="font-size: 0.82rem; color: var(--rose-dark); font-weight: 600; margin-top: 2px;">Empresa: InnovaCorp Latam</p>
                            </div>
                            <span class="event-category-badge gold">Corporativo</span>
                        </div>

                        <div class="event-details-row">
                            <div class="event-detail-item">&#128197; 15 Oct 2026</div>
                            <div class="event-detail-item">&#128338; 19:30 hrs</div>
                            <div class="event-detail-item">&#128101; 320 Asistentes</div>
                            <div class="event-detail-item">&#128176; S/ 65,000</div>
                        </div>

                        <div class="progress-container">
                            <div class="progress-header">
                                <span>🔵 Fase: Contratos de Catering Aprobados</span>
                                <span>40%</span>
                            </div>
                            <div class="progress-bar-bg">
                                <div class="progress-bar-fill" style="width: 40%;"></div>
                            </div>
                        </div>
                    </article>

                    <!-- Evento 4 -->
                    <article class="event-live-card">
                        <div class="event-top-info">
                            <div>
                                <h3 class="event-name">&#127912; Aniversario de Plata "Club de Golf"</h3>
                                <p style="font-size: 0.82rem; color: var(--rose-dark); font-weight: 600; margin-top: 2px;">Familia: Morales & Prado</p>
                            </div>
                            <span class="event-category-badge">Aniversario</span>
                        </div>

                        <div class="event-details-row">
                            <div class="event-detail-item">&#128197; 28 Oct 2026</div>
                            <div class="event-detail-item">&#128338; 13:00 hrs</div>
                            <div class="event-detail-item">&#128101; 120 Invitados</div>
                            <div class="event-detail-item">&#128176; S/ 25,000</div>
                        </div>

                        <div class="progress-container">
                            <div class="progress-header">
                                <span>🟣 Fase: Selección de Cristalería & Menú</span>
                                <span>25%</span>
                            </div>
                            <div class="progress-bar-bg">
                                <div class="progress-bar-fill" style="width: 25%;"></div>
                            </div>
                        </div>
                    </article>

                </div>
            </section>


            <!-- MITAD DERECHA: Centro de Registro & Operaciones -->
            <section class="col-register-center">
                <div class="section-header">
                    <h2 class="section-title">
                        &#9998; Centro de Registro
                    </h2>
                    <span style="font-size: 0.8rem; color: var(--rose-dark); font-weight: 600;">NUEVAS ENTRADAS</span>
                </div>

                <!-- Tabs de Navegación -->
                <div class="operation-tabs">
                    <button class="tab-btn active" onclick="switchTab('tab-evento')">&#127878; Evento</button>
                    <button class="tab-btn" onclick="switchTab('tab-insumo')">&#128230; Insumo</button>
                    <button class="tab-btn" onclick="switchTab('tab-proveedor')">&#128666; Proveedor</button>
                    <button class="tab-btn" onclick="switchTab('tab-staff')">&#128101; Personal</button>
                </div>

                <!-- Tab 1: Registrar Evento -->
                <div id="tab-evento" class="tab-content active">
                    <form class="admin-form" onsubmit="handleRegister(event, 'evento')">
                        <div class="field-group">
                            <label>Nombre del Evento *</label>
                            <input type="text" id="ev-nombre" placeholder="Ej: Boda Dorada Jardines del Sol" required>
                        </div>

                        <div class="form-row-2">
                            <div class="field-group">
                                <label>Tipo de Celebración *</label>
                                <select id="ev-tipo" required>
                                    <option value="Boda">💍 Boda de Gala</option>
                                    <option value="Quinceañero">👑 Quinceañero</option>
                                    <option value="Corporativo">🥂 Evento Corporativo</option>
                                    <option value="Graduación">🎓 Graduación / Prom</option>
                                    <option value="Aniversario">🌸 Aniversario</option>
                                </select>
                            </div>
                            <div class="field-group">
                                <label>Cliente Titular *</label>
                                <input type="text" id="ev-cliente" placeholder="Nombre del cliente" required>
                            </div>
                        </div>

                        <div class="form-row-2">
                            <div class="field-group">
                                <label>Fecha del Evento *</label>
                                <input type="date" id="ev-fecha" required>
                            </div>
                            <div class="field-group">
                                <label>Hora de Inicio *</label>
                                <input type="time" id="ev-hora" value="19:00" required>
                            </div>
                        </div>

                        <div class="form-row-2">
                            <div class="field-group">
                                <label>Aforo Estimado (Invitados) *</label>
                                <input type="number" id="ev-invitados" placeholder="Ej: 200" required>
                            </div>
                            <div class="field-group">
                                <label>Presupuesto Estimado (S/) *</label>
                                <input type="number" id="ev-presupuesto" placeholder="Ej: 35000" required>
                            </div>
                        </div>

                        <button type="submit" class="btn-register-action">Registrar Evento en Vivo</button>
                    </form>
                </div>

                <!-- Tab 2: Registrar Insumo / Mobiliario -->
                <div id="tab-insumo" class="tab-content">
                    <form class="admin-form" onsubmit="handleRegister(event, 'insumo')">
                        <div class="field-group">
                            <label>Nombre del Artículo / Insumo *</label>
                            <input type="text" placeholder="Ej: Sillas Tiffany Doradas" required>
                        </div>

                        <div class="form-row-2">
                            <div class="field-group">
                                <label>Categoría *</label>
                                <select required>
                                    <option>Mobiliario & Toldos</option>
                                    <option>Iluminación & Audio</option>
                                    <option>Vajilla & Cristalería</option>
                                    <option>Decoración Floral</option>
                                    <option>Pistas de Baile LED</option>
                                </select>
                            </div>
                            <div class="field-group">
                                <label>Cantidad en Stock *</label>
                                <input type="number" placeholder="Ej: 150" required>
                            </div>
                        </div>

                        <div class="field-group">
                            <label>Costo de Alquiler por Unidad (S/)</label>
                            <input type="number" placeholder="Ej: 18.50" step="0.50">
                        </div>

                        <button type="submit" class="btn-register-action">Guardar en Inventario</button>
                    </form>
                </div>

                <!-- Tab 3: Registrar Proveedor -->
                <div id="tab-proveedor" class="tab-content">
                    <form class="admin-form" onsubmit="handleRegister(event, 'proveedor')">
                        <div class="field-group">
                            <label>Razón Social / Empresa *</label>
                            <input type="text" placeholder="Ej: Floristería & Diseños Rosé S.A.C." required>
                        </div>

                        <div class="form-row-2">
                            <div class="field-group">
                                <label>Rubro del Proveedor *</label>
                                <select required>
                                    <option>Catering & Bar</option>
                                    <option>Arreglos Florales</option>
                                    <option>Efectos Especiales & Luces</option>
                                    <option>Fotografía & Video 4K</option>
                                    <option>Música en Vivo / DJ</option>
                                </select>
                            </div>
                            <div class="field-group">
                                <label>Teléfono de Contacto *</label>
                                <input type="tel" placeholder="Ej: 987654321" required>
                            </div>
                        </div>

                        <div class="field-group">
                            <label>Correo de Coordinación</label>
                            <input type="email" placeholder="contacto@proveedor.pe">
                        </div>

                        <button type="submit" class="btn-register-action">Vincular Proveedor</button>
                    </form>
                </div>

                <!-- Tab 4: Registrar Personal / Staff -->
                <div id="tab-staff" class="tab-content">
                    <form class="admin-form" onsubmit="handleRegister(event, 'staff')">
                        <div class="form-row-2">
                            <div class="field-group">
                                <label>Nombre del Colaborador *</label>
                                <input type="text" placeholder="Ej: Camila" required>
                            </div>
                            <div class="field-group">
                                <label>Apellido *</label>
                                <input type="text" placeholder="Ej: Paredes" required>
                            </div>
                        </div>

                        <div class="form-row-2">
                            <div class="field-group">
                                <label>Especialidad *</label>
                                <select required>
                                    <option>Coordinador General de Boda</option>
                                    <option>Director Técnico de Luces</option>
                                    <option>Supervisor de Catering</option>
                                    <option>Asistente de Protocolo</option>
                                </select>
                            </div>
                            <div class="field-group">
                                <label>Teléfono Celular *</label>
                                <input type="tel" placeholder="981234567" required>
                            </div>
                        </div>

                        <button type="submit" class="btn-register-action">Asignar al Equipo INARA</button>
                    </form>
                </div>

            </section>

        </div>

    </main>

    <!-- Notificación Toast Dinámica -->
    <div id="toast" class="toast-notification">
        <span>&#10003;</span>
        <span id="toast-text">¡Registro guardado exitosamente en el sistema!</span>
    </div>

    <!-- Pie de Página -->
    <footer class="admin-footer">
        &copy; 2026 <strong>INARA</strong> — Sistema Integral de Gestión & Producción de Eventos.
    </footer>

    <!-- Script de Interactividad y Pestañas -->
    <script>
        function switchTab(tabId) {
            document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
            document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));

            event.target.classList.add('active');
            document.getElementById(tabId).classList.add('active');
        }

        function handleRegister(e, tipo) {
            e.preventDefault();

            if (tipo === 'evento') {
                const nombre = document.getElementById('ev-nombre').value;
                const cliente = document.getElementById('ev-cliente').value;
                const tipoEv = document.getElementById('ev-tipo').value;
                const fecha = document.getElementById('ev-fecha').value;
                const invitados = document.getElementById('ev-invitados').value;
                const presupuesto = document.getElementById('ev-presupuesto').value;

                // Crear nueva tarjeta animada en la columna izquierda
                const stream = document.getElementById('eventsStream');
                const newCard = document.createElement('article');
                newCard.className = 'event-live-card';
                newCard.innerHTML = `
                    <div class="event-top-info">
                        <div>
                            <h3 class="event-name">&#10024; ${nombre}</h3>
                            <p style="font-size: 0.82rem; color: var(--rose-dark); font-weight: 600; margin-top: 2px;">Titular: ${cliente}</p>
                        </div>
                        <span class="event-category-badge gold">${tipoEv}</span>
                    </div>
                    <div class="event-details-row">
                        <div class="event-detail-item">&#128197; ${fecha || 'Próximo'}</div>
                        <div class="event-detail-item">&#128101; ${invitados} Invitados</div>
                        <div class="event-detail-item">&#128176; S/ ${Number(presupuesto).toLocaleString()}</div>
                    </div>
                    <div class="progress-container">
                        <div class="progress-header">
                            <span>🟢 Fase: Evento Recién Registrado en Producción</span>
                            <span>15%</span>
                        </div>
                        <div class="progress-bar-bg">
                            <div class="progress-bar-fill" style="width: 15%;"></div>
                        </div>
                    </div>
                `;
                stream.insertBefore(newCard, stream.firstChild);

                // Incrementar contador
                const countElem = document.getElementById('count-eventos');
                const curr = parseInt(countElem.innerText) || 8;
                countElem.innerText = (curr + 1) + " Activos";

                showToast('¡Evento "' + nombre + '" registrado y añadido a producción!');
                e.target.reset();
            } else if (tipo === 'insumo') {
                showToast('¡Insumo agregado al inventario de eventos!');
                e.target.reset();
            } else if (tipo === 'proveedor') {
                showToast('¡Proveedor registrado y vinculado al catálogo de eventos!');
                e.target.reset();
            } else if (tipo === 'staff') {
                showToast('¡Colaborador asignado al equipo técnico!');
                e.target.reset();
            }
        }

        function showToast(msg) {
            const toast = document.getElementById('toast');
            document.getElementById('toast-text').innerText = msg;
            toast.style.display = 'flex';
            setTimeout(() => {
                toast.style.display = 'none';
            }, 4000);
        }
    </script>
</body>
</html>
