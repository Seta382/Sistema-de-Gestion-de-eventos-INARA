<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="pe.edu.eventos.dto.UsuarioDTO" %>
<%@ page import="pe.edu.eventos.util.Constantes" %>
<%@ page import="pe.edu.eventos.model.Cliente" %>
<%@ page import="pe.edu.eventos.model.TipoEvento" %>
<%@ page import="pe.edu.eventos.model.Articulo" %>
<%@ page import="pe.edu.eventos.model.Empleado" %>
<%@ page import="pe.edu.eventos.model.Evento" %>
<%@ page import="pe.edu.eventos.service.EventoService" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
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

    EventoService eventoService = new EventoService();
    List<TipoEvento> listaTipos = eventoService.listarTiposEvento();
    List<Cliente> listaClientes = eventoService.listarClientes();
    List<Articulo> listaInsumos = eventoService.listarInsumosDisponibles();
    List<Empleado> listaPersonal = eventoService.listarPersonalOperativo();
    List<Evento> listaEventosActivos = eventoService.listarEventosActivos();
    NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
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

        /* Métricas Rápidas */
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

        /* Espacio de Trabajo Dividido */
        .split-workspace {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 28px;
            align-items: start;
        }

        /* Panel Izquierdo: Eventos */
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

        .events-stream {
            display: flex;
            flex-direction: column;
            gap: 16px;
            max-height: 680px;
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
            margin-bottom: 12px;
        }

        .event-detail-item {
            display: flex;
            align-items: center;
            gap: 5px;
        }

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

        /* Panel Derecho: Centro de Registro */
        .col-register-center {
            background: var(--white);
            border-radius: 20px;
            padding: 30px;
            border: 1px solid var(--border-soft);
            box-shadow: var(--shadow-card);
        }

        .operation-tabs {
            display: flex;
            gap: 8px;
            background-color: #F4EFEB;
            padding: 6px;
            border-radius: 14px;
            margin-bottom: 22px;
            overflow-x: auto;
        }

        .tab-btn {
            flex: 1;
            padding: 10px 14px;
            border: none;
            background: transparent;
            font-size: 0.82rem;
            font-weight: 600;
            color: var(--text-secondary);
            border-radius: 10px;
            cursor: pointer;
            transition: all 0.25s ease;
            white-space: nowrap;
        }

        .tab-btn.active {
            background-color: var(--white);
            color: var(--rose-dark);
            box-shadow: 0 4px 12px rgba(61, 51, 51, 0.08);
        }

        .tab-content {
            display: none;
            animation: fadeIn 0.3s ease;
        }

        .tab-content.active {
            display: block;
        }

        @keyframes fadeIn {
            from {
                opacity: 0;
                transform: translateY(6px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
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

        /* Tabla de Insumos */
        .insumo-table-wrapper {
            max-height: 380px;
            overflow-y: auto;
            border: 1px solid var(--border-soft);
            border-radius: 12px;
            margin-bottom: 12px;
        }

        .insumo-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 0.85rem;
            text-align: left;
        }

        .insumo-table th {
            background-color: #F8F4F1;
            padding: 12px 10px;
            font-weight: 700;
            color: var(--text-primary);
            position: sticky;
            top: 0;
            z-index: 2;
            border-bottom: 1px solid var(--border-soft);
        }

        .insumo-table td {
            padding: 10px;
            border-bottom: 1px solid #F4EFEB;
            vertical-align: middle;
        }

        .insumo-table tr:hover {
            background-color: #FCF9F7;
        }

        .badge-stock {
            background: #E8F5E9;
            color: #2E7D32;
            padding: 3px 8px;
            border-radius: 12px;
            font-weight: 700;
            font-size: 0.75rem;
        }

        .badge-prov {
            background: #FFF8E1;
            color: #8D6E63;
            padding: 3px 8px;
            border-radius: 12px;
            font-size: 0.75rem;
        }

        /* Proveedores Derivados Cards */
        .prov-card {
            background: #FAF8F6;
            border: 1px solid var(--border-soft);
            border-left: 4px solid var(--gold);
            border-radius: 10px;
            padding: 14px 18px;
            margin-bottom: 12px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .prov-card h4 {
            font-size: 0.92rem;
            color: var(--text-primary);
            margin-bottom: 4px;
        }

        .prov-card p {
            font-size: 0.78rem;
            color: var(--text-secondary);
        }

        /* Personal List */
        .staff-card {
            background: #FAF8F6;
            border: 1.5px solid var(--border-soft);
            border-radius: 12px;
            padding: 12px 16px;
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 10px;
            transition: all 0.2s ease;
        }

        .staff-card:hover {
            border-color: var(--rose-dark);
            background: var(--white);
        }

        /* Resumen Final Box */
        .summary-box {
            background: #FDF9F6;
            border: 1px dashed var(--gold);
            border-radius: 12px;
            padding: 16px 20px;
            margin-top: 14px;
            font-size: 0.85rem;
            color: var(--text-primary);
        }

        .summary-row {
            display: flex;
            justify-content: space-between;
            margin-bottom: 6px;
        }

        .btn-step-next {
            background: linear-gradient(135deg, var(--rose-dark) 0%, var(--rose-deep) 100%);
            color: var(--white);
            padding: 12px 20px;
            border: none;
            border-radius: 10px;
            font-size: 0.85rem;
            font-weight: 600;
            letter-spacing: 1px;
            text-transform: uppercase;
            cursor: pointer;
            transition: all 0.25s ease;
        }

        .btn-step-next:hover {
            transform: translateY(-1px);
            box-shadow: 0 6px 16px rgba(116, 62, 62, 0.25);
        }

        .btn-step-prev {
            background: #EAE3DE;
            color: var(--text-primary);
            padding: 12px 18px;
            border: none;
            border-radius: 10px;
            font-size: 0.85rem;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.25s ease;
        }

        .btn-step-prev:hover {
            background: #DFD7D1;
        }

        .nav-buttons-row {
            display: flex;
            justify-content: space-between;
            margin-top: 16px;
            gap: 10px;
        }

        .btn-register-action {
            width: 100%;
            background: linear-gradient(135deg, var(--gold) 0%, #A66A6A 50%, var(--rose-deep) 100%);
            color: var(--white);
            padding: 16px 24px;
            border: none;
            border-radius: 12px;
            font-size: 0.95rem;
            font-weight: 700;
            letter-spacing: 2px;
            text-transform: uppercase;
            cursor: pointer;
            box-shadow: 0 8px 24px rgba(166, 106, 106, 0.35);
            transition: all 0.3s ease;
            margin-top: 14px;
        }

        .btn-register-action:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 28px rgba(166, 106, 106, 0.45);
        }

        .btn-register-action:disabled {
            opacity: 0.6;
            cursor: not-allowed;
            transform: none;
        }

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
            from {
                transform: translateX(100%);
                opacity: 0;
            }
            to {
                transform: translateX(0);
                opacity: 1;
            }
        }

        .admin-footer {
            background: var(--white);
            border-top: 1px solid var(--border-soft);
            padding: 20px 24px;
            text-align: center;
            font-size: 0.82rem;
            color: var(--text-secondary);
            margin-top: auto;
        }

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

<!-- Navegación Superior -->
<header class="admin-navbar">
    <div class="navbar-container">
        <div class="nav-brand-group">
            <span class="brand-name">INARA</span>
            <span class="brand-badge">Control & Producción</span>
        </div>

        <div class="admin-user-profile">
            <div class="admin-chip">
                <div>
                    <div class="admin-name"><%= usuario.getNombreCompleto()%></div>
                    <div class="admin-role">👑 <%= usuario.getRol()%> &bull; Supabase Cloud</div>
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
                <div class="kpi-value" id="count-eventos"><%= listaEventosActivos.size()%> Activos</div>
            </div>
            <div class="kpi-icon">&#127878;</div>
        </div>

        <a href="<%= request.getContextPath()%>/articulo?accion=registro"
           style="text-decoration: none; color: inherit; display: block;">

            <div class="kpi-card gold">

                <div>
                    <div class="kpi-label">Insumos Disponibles</div>
                    <div class="kpi-value"><%= listaInsumos.size()%> Catálogo</div>
                </div>

                <div class="kpi-icon">&#128230;</div>

            </div>

        </a>

        <div class="kpi-card">
            <div>
                <div class="kpi-label">Clientes Titulares</div>
                <div class="kpi-value"><%= listaClientes.size()%> Registrados</div>
            </div>
            <div class="kpi-icon">&#128101;</div>
        </div>

        <a href="${pageContext.request.contextPath}/empleado?accion=listar"
           style="text-decoration: none; color: inherit; display: block;">

            <div class="kpi-card gold">
                <div>
                    <div class="kpi-label">Personal Administrativo</div>
                    <div class="kpi-value"><%= listaPersonal.size()%> Staff</div>
                </div>
                <div class="kpi-icon">&#10024;</div>
            </div>

        </a>
    </section>

    <!-- Espacio Dividido -->
    <div class="split-workspace">

        <!-- MITAD IZQUIERDA: Monitor de Eventos en Tiempo Real (Desde Supabase) -->
        <section class="col-events-monitor">
            <div class="section-header">
                <h2 class="section-title">
                    <span class="live-pulse"></span>
                    Eventos en Ejecución
                </h2>
                <span style="font-size: 0.8rem; color: var(--gold); font-weight: 600;">EN TIEMPO REAL &bull; SUPABASE</span>
            </div>

            <div class="events-stream" id="eventsStream">
                <% if (listaEventosActivos.isEmpty()) { %>
                <div style="text-align: center; padding: 48px 20px; color: var(--text-secondary);">
                    <div style="font-size: 2.8rem; margin-bottom: 12px;">✨</div>
                    <h3 style="font-size: 1.15rem; color: var(--text-primary); margin-bottom: 8px;">No hay eventos en producción aún</h3>
                    <p style="font-size: 0.86rem; line-height: 1.6;">
                        Completa el flujo en el <strong>Centro de Registro</strong> para coordinar insumos, proveedores y personal de Supabase.
                    </p>
                </div>
                <% } else { %>
                <% for (Evento ev : listaEventosActivos) {%>
                <article class="event-live-card">
                    <div class="event-top-info">
                        <div>
                            <h3 class="event-name">✨ <%= ev.getNombre() != null ? ev.getNombre() : "Evento INARA"%></h3>
                            <p style="font-size: 0.82rem; color: var(--rose-dark); font-weight: 600; margin-top: 2px;">
                                Titular: <%= ev.getNombreCliente() != null ? ev.getNombreCliente() : "Cliente General"%>
                            </p>
                        </div>
                        <span class="event-category-badge gold"><%= ev.getTipoCelebracion() != null ? ev.getTipoCelebracion() : "Evento"%></span>
                    </div>

                    <div class="event-details-row">
                        <div class="event-detail-item">📅 <%= ev.getFechaEvento() != null ? ev.getFechaEvento() : "Próximo"%></div>
                        <div class="event-detail-item">🕒 <%= ev.getHoraEvento() != null ? ev.getHoraEvento() : "18:00"%></div>
                        <div class="event-detail-item">👥 <%= ev.getNumInvitados()%> Invitados</div>
                        <div class="event-detail-item">💰 <%= ev.getPresupuesto() != null ? nf.format(ev.getPresupuesto()) : "S/ 0.00"%></div>
                    </div>

                    <div style="font-size: 0.8rem; color: var(--text-secondary); margin-bottom: 10px;">
                        👤 Coordinador: <strong><%= ev.getNombreCoordinador() != null ? ev.getNombreCoordinador() : "Staff Asignado"%></strong>
                    </div>

                    <div class="progress-container">
                        <div class="progress-header">
                            <span>🟢 Fase: <%= ev.getEstado()%></span>
                            <span>100% Confirmado</span>
                        </div>
                        <div class="progress-bar-bg">
                            <div class="progress-bar-fill" style="width: 100%; background: linear-gradient(90deg, var(--gold), var(--rose-dark));"></div>
                        </div>
                    </div>
                </article>
                <% } %>
                <% } %>
            </div>
        </section>

        <!-- MITAD DERECHA: Centro de Registro en Vivo -->
        <section class="col-register-center">
            <div class="section-header">
                <h2 class="section-title">&#9998; Centro de Registro</h2>
                <span style="font-size: 0.8rem; color: var(--rose-dark); font-weight: 600;">FLUJO INTERACTIVO</span>
            </div>

            <!-- Tabs de Navegación -->
            <div class="operation-tabs">
                <button class="tab-btn active" id="btn-tab-evento" type="button" onclick="switchTab('tab-evento')">&#127878; 1. Evento</button>
                <button class="tab-btn" id="btn-tab-insumo" type="button" onclick="switchTab('tab-insumo')">&#128230; 2. Insumo</button>
                <button class="tab-btn" id="btn-tab-proveedor" type="button" onclick="switchTab('tab-proveedor')">&#128666; 3. Proveedor</button>
                <button class="tab-btn" id="btn-tab-staff" type="button" onclick="switchTab('tab-staff')">&#128101; 4. Personal</button>
            </div>

            <!-- PASO 1: EVENTO -->
            <div id="tab-evento" class="tab-content active">
                <div class="admin-form">
                    <div class="field-group">
                        <label>Nombre del Evento *</label>
                        <input type="text" id="ev-nombre" placeholder="Ej: Boda Romántica Jardines del Sol" required autofocus>
                    </div>

                    <div class="form-row-2">
                        <div class="field-group">
                            <label>Tipo de Celebración *</label>
                            <select id="ev-tipo" required>
                                <% for (TipoEvento te : listaTipos) {%>
                                <option value="<%= te.getIdTipoEvento()%>"><%= te.getNombre()%></option>
                                <% } %>
                            </select>
                        </div>
                        <div class="field-group">
                            <label>Cliente Titular Real *</label>
                            <select id="ev-cliente" required>
                                <option value="">-- Selecciona el Cliente --</option>
                                <% for (Cliente cli : listaClientes) {%>
                                <option value="<%= cli.getIdCliente()%>">
                                    <%= cli.getNombre()%> <%= cli.getApellido()%> (DNI: <%= cli.getDni() != null ? cli.getDni() : "S/D"%>)
                                </option>
                                <% } %>
                            </select>
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
                            <input type="number" id="ev-invitados" placeholder="Ej: 150" min="1" value="100" required>
                        </div>
                        <div class="field-group">
                            <label>Presupuesto Estimado (S/) *</label>
                            <input type="number" id="ev-presupuesto" placeholder="Ej: 35000" min="0" step="100" value="25000" required>
                        </div>
                    </div>

                    <div class="nav-buttons-row" style="justify-content: flex-end;">
                        <button type="button" class="btn-step-next" onclick="irAInsumos()">Siguiente: Seleccionar Insumos &rarr;</button>
                    </div>
                </div>
            </div>

            <!-- PASO 2: INSUMOS -->
            <div id="tab-insumo" class="tab-content">
                <p style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 12px;">
                    Selecciona los insumos reales del inventario para abastecer el evento:
                </p>

                <div class="insumo-table-wrapper">
                    <table class="insumo-table">
                        <thead>
                        <tr>
                            <th style="width: 36px; text-align: center;">Sel.</th>
                            <th>Artículo / Insumo</th>
                            <th>Proveedor</th>
                            <th>Precio</th>
                            <th>Stock</th>
                            <th style="width: 90px;">Cantidad</th>
                        </tr>
                        </thead>
                        <tbody>
                        <% if (listaInsumos.isEmpty()) { %>
                        <tr>
                            <td colspan="6" style="text-align: center; padding: 20px; color: var(--text-secondary);">
                                No hay insumos con stock disponible en la base de datos.
                            </td>
                        </tr>
                        <% } else { %>
                        <% for (Articulo art : listaInsumos) {%>
                        <tr>
                            <td style="text-align: center;">
                                <input type="checkbox" class="chk-insumo"
                                       data-id="<%= art.getIdArticulo()%>"
                                       data-nombre="<%= art.getNombre()%>"
                                       data-precio="<%= art.getPrecio()%>"
                                       data-stock="<%= art.getStock()%>"
                                       data-prov-id="<%= art.getIdProveedor()%>"
                                       data-prov-nombre="<%= art.getNombreProveedor() != null ? art.getNombreProveedor() : "Proveedor General"%>"
                                       onchange="toggleInsumo(this)">
                            </td>
                            <td>
                                <strong><%= art.getNombre()%></strong>
                            </td>
                            <td>
                                <span class="badge-prov"><%= art.getNombreProveedor() != null ? art.getNombreProveedor() : "General"%></span>
                            </td>
                            <td>
                                <%= nf.format(art.getPrecio())%>
                            </td>
                            <td>
                                <span class="badge-stock"><%= art.getStock()%> disp.</span>
                            </td>
                            <td>
                                <input type="number" id="cant-<%= art.getIdArticulo()%>"
                                       min="1" max="<%= art.getStock()%>" value="1"
                                       disabled
                                       style="width: 75px; padding: 6px; border: 1.5px solid var(--border-soft); border-radius: 6px; text-align: center;"
                                       oninput="validarCantidad(this)">
                            </td>
                        </tr>
                        <% } %>
                        <% } %>
                        </tbody>
                    </table>
                </div>

                <div style="display: flex; justify-content: space-between; font-size: 0.85rem; padding: 8px 12px; background: #FAF8F6; border-radius: 8px; margin-bottom: 14px;">
                    <span>Insumos seleccionados: <strong id="resumen-cant-articulos">0</strong></span>
                    <span>Subtotal estimado: <strong id="resumen-subtotal-articulos">S/ 0.00</strong></span>
                </div>

                <div class="nav-buttons-row">
                    <button type="button" class="btn-step-prev" onclick="switchTab('tab-evento')">&larr; Volver a Evento</button>
                    <button type="button" class="btn-step-next" onclick="irAProveedores()">Siguiente: Ver Proveedores &rarr;</button>
                </div>
            </div>

            <!-- PASO 3: PROVEEDORES (Derivados Automáticamente) -->
            <div id="tab-proveedor" class="tab-content">
                <p style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 14px;">
                    Los proveedores son <strong>deducidos automáticamente</strong> a partir de los insumos seleccionados (sin duplicados):
                </p>

                <div id="contenedor-proveedores-derivados">
                    <div style="text-align: center; padding: 30px; background: #FAF8F6; border-radius: 12px; color: var(--text-secondary); font-size: 0.88rem;">
                        ⚠️ Aún no has seleccionado ningún insumo. Por favor regresa a la pestaña <strong>Insumo</strong> y selecciona al menos uno.
                    </div>
                </div>

                <div class="nav-buttons-row">
                    <button type="button" class="btn-step-prev" onclick="switchTab('tab-insumo')">&larr; Volver a Insumos</button>
                    <button type="button" class="btn-step-next" onclick="irAPersonal()">Siguiente: Asignar Personal &rarr;</button>
                </div>
            </div>

            <!-- PASO 4: PERSONAL & CONFIRMACIÓN -->
            <div id="tab-staff" class="tab-content">
                <p style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 14px;">
                    Asigna al personal operativo real de INARA para coordinar y supervisar el evento:
                </p>

                <div class="field-group" style="margin-bottom: 14px;">
                    <label>Coordinador Responsable del Evento *</label>
                    <select id="ev-coordinador" required>
                        <option value="">-- Selecciona el Coordinador General --</option>
                        <% for (Empleado emp : listaPersonal) {%>
                        <option value="<%= emp.getIdEmpleado()%>">
                            <%= emp.getNombreCompleto()%> &bull; <%= emp.getCargo() != null ? emp.getCargo() : "Staff"%> (<%= emp.getArea() != null ? emp.getArea() : "Operaciones"%>)
                        </option>
                        <% } %>
                    </select>
                </div>

                <label style="font-size: 0.8rem; font-weight: 600; color: var(--text-primary); text-transform: uppercase; margin-bottom: 8px; display: block;">
                    Personal Operativo Adicional (Staff de Apoyo):
                </label>

                <div style="max-height: 180px; overflow-y: auto; margin-bottom: 16px;">
                    <% for (Empleado emp : listaPersonal) {%>
                    <label class="staff-card">
                        <input type="checkbox" class="chk-staff" value="<%= emp.getIdEmpleado()%>" onchange="actualizarResumenStaff()">
                        <div>
                            <div style="font-weight: 600; font-size: 0.88rem; color: var(--text-primary);"><%= emp.getNombreCompleto()%></div>
                            <div style="font-size: 0.76rem; color: var(--text-secondary);"><%= emp.getCargo() != null ? emp.getCargo() : "Colaborador"%> &bull; <%= emp.getArea() != null ? emp.getArea() : "Protocolo"%></div>
                        </div>
                    </label>
                    <% }%>
                </div>

                <!-- Resumen del Registro -->
                <div class="summary-box">
                    <div style="font-weight: 700; color: var(--rose-deep); margin-bottom: 8px;">📋 Resumen de la Operación en Vivo</div>
                    <div class="summary-row">
                        <span>Celebración:</span>
                        <strong id="sum-celebracion">-</strong>
                    </div>
                    <div class="summary-row">
                        <span>Cliente Titular:</span>
                        <strong id="sum-cliente">-</strong>
                    </div>
                    <div class="summary-row">
                        <span>Fecha & Hora:</span>
                        <strong id="sum-fecha">-</strong>
                    </div>
                    <div class="summary-row">
                        <span>Insumos Asignados:</span>
                        <strong id="sum-insumos">0 ítems</strong>
                    </div>
                    <div class="summary-row">
                        <span>Proveedores Vinculados:</span>
                        <strong id="sum-proveedores">0 empresas</strong>
                    </div>
                </div>

                <div class="nav-buttons-row">
                    <button type="button" class="btn-step-prev" onclick="switchTab('tab-proveedor')">&larr; Volver a Proveedores</button>
                </div>

                <!-- Botón de Registro Transaccional -->
                <button type="button" id="btn-registrar-vivo" class="btn-register-action" onclick="ejecutarRegistroEventoEnVivo()">
                    🚀 REGISTRAR EVENTO EN VIVO
                </button>
            </div>

        </section>
    </div>

</main>

<!-- Notificación Toast -->
<div id="toast" class="toast-notification">
    <span>&#10003;</span>
    <span id="toast-text">¡Registro guardado exitosamente en Supabase!</span>
</div>

<!-- Pie de Página -->
<footer class="admin-footer">
    &copy; 2026 <strong>INARA</strong> — Sistema Integral de Gestión & Producción de Eventos.
</footer>

<!-- Lógica JavaScript del Flujo de 4 Pestañas y Transacción -->
<script>
    function switchTab(tabId) {
        document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
        document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));

        const btnId = 'btn-' + tabId;
        const btn = document.getElementById(btnId);
        if (btn)
            btn.classList.add('active');

        const content = document.getElementById(tabId);
        if (content)
            content.classList.add('active');

        if (tabId === 'tab-proveedor') {
            generarProveedoresDerivados();
        } else if (tabId === 'tab-staff') {
            actualizarResumenStaff();
        }
    }

    function irAInsumos() {
        const nombre = document.getElementById('ev-nombre').value.trim();
        const cliente = document.getElementById('ev-cliente').value;
        const fecha = document.getElementById('ev-fecha').value;

        if (!nombre) {
            alert('Por favor ingresa el nombre del evento.');
            document.getElementById('ev-nombre').focus();
            return;
        }
        if (!cliente) {
            alert('Por favor selecciona un cliente titular real de la lista.');
            document.getElementById('ev-cliente').focus();
            return;
        }
        if (!fecha) {
            alert('Por favor especifica la fecha del evento.');
            document.getElementById('ev-fecha').focus();
            return;
        }

        switchTab('tab-insumo');
    }

    function toggleInsumo(chk) {
        const id = chk.getAttribute('data-id');
        const cantInput = document.getElementById('cant-' + id);
        if (cantInput) {
            cantInput.disabled = !chk.checked;
            if (chk.checked && (!cantInput.value || parseInt(cantInput.value) <= 0)) {
                cantInput.value = 1;
            }
        }
        recalcularInsumos();
    }

    function validarCantidad(input) {
        const max = parseInt(input.getAttribute('max')) || 999;
        let val = parseInt(input.value);
        if (isNaN(val) || val <= 0) {
            input.value = 1;
        } else if (val > max) {
            alert('La cantidad no puede superar el stock disponible (' + max + ').');
            input.value = max;
        }
        recalcularInsumos();
    }

    function recalcularInsumos() {
        const chks = document.querySelectorAll('.chk-insumo:checked');
        let totalItems = 0;
        let subtotal = 0;

        chks.forEach(chk => {
            const id = chk.getAttribute('data-id');
            const precio = parseFloat(chk.getAttribute('data-precio')) || 0;
            const cantInput = document.getElementById('cant-' + id);
            const cant = cantInput ? parseInt(cantInput.value) || 0 : 0;

            totalItems += cant;
            subtotal += (precio * cant);
        });

        document.getElementById('resumen-cant-articulos').innerText = totalItems + " unidades (" + chks.length + " tipos)";
        document.getElementById('resumen-subtotal-articulos').innerText = "S/ " + subtotal.toFixed(2);
    }

    function irAProveedores() {
        const chks = document.querySelectorAll('.chk-insumo:checked');
        if (chks.length === 0) {
            alert('Debes seleccionar al menos un insumo con stock para este evento.');
            return;
        }
        switchTab('tab-proveedor');
    }

    function generarProveedoresDerivados() {
        var chks = document.querySelectorAll('.chk-insumo:checked');
        var contenedor = document.getElementById('contenedor-proveedores-derivados');

        if (chks.length === 0) {
            contenedor.innerHTML = '<div style="text-align: center; padding: 30px; background: #FAF8F6; border-radius: 12px; color: var(--text-secondary); font-size: 0.88rem;">' +
                '⚠️ Aún no has seleccionado ningún insumo. Por favor regresa a la pestaña <strong>Insumo</strong> y selecciona al menos uno.' +
                '</div>';
            return;
        }

        // Agrupar insumos por proveedor único
        var proveedoresMap = {};
        chks.forEach(function (chk) {
            var provId = chk.getAttribute('data-prov-id') || '0';
            var provNombre = chk.getAttribute('data-prov-nombre') || 'Proveedor Aliado INARA';
            var artNombre = chk.getAttribute('data-nombre');
            var id = chk.getAttribute('data-id');
            var cantInput = document.getElementById('cant-' + id);
            var cant = cantInput ? cantInput.value : 1;

            if (!proveedoresMap[provId]) {
                proveedoresMap[provId] = {
                    nombre: provNombre,
                    insumos: []
                };
            }
            proveedoresMap[provId].insumos.push({nombre: artNombre, cantidad: cant});
        });

        var html = '';
        for (var pId in proveedoresMap) {
            var prov = proveedoresMap[pId];
            var listaInsumosTxt = prov.insumos.map(function (item) {
                return item.nombre + ' (' + item.cantidad + ' u.)';
            }).join(', ');

            html += '<div class="prov-card">' +
                '<div>' +
                '<h4>🚚 ' + prov.nombre + '</h4>' +
                '<p><strong>Insumos abastecidos:</strong> ' + listaInsumosTxt + '</p>' +
                '</div>' +
                '<span class="badge-stock" style="background: rgba(198, 161, 91, 0.2); color: #8C6A29;">VINCULADO AUTOMÁTICO</span>' +
                '</div>';
        }
        contenedor.innerHTML = html;
    }

    function irAPersonal() {
        switchTab('tab-staff');
    }

    function actualizarResumenStaff() {
        var nombre = document.getElementById('ev-nombre').value.trim() || 'Sin Nombre';
        var selectTipo = document.getElementById('ev-tipo');
        var tipo = selectTipo.options[selectTipo.selectedIndex] ? selectTipo.options[selectTipo.selectedIndex].text : '';
        var selectCli = document.getElementById('ev-cliente');
        var cli = selectCli.options[selectCli.selectedIndex] ? selectCli.options[selectCli.selectedIndex].text : '';
        var fecha = document.getElementById('ev-fecha').value || 'Sin fecha';
        var hora = document.getElementById('ev-hora').value || '18:00';

        var chks = document.querySelectorAll('.chk-insumo:checked');
        var cantInsumos = 0;
        var provsSet = new Set();
        chks.forEach(function (c) {
            var id = c.getAttribute('data-id');
            var cantInput = document.getElementById('cant-' + id);
            cantInsumos += (cantInput ? parseInt(cantInput.value) || 0 : 0);
            provsSet.add(c.getAttribute('data-prov-id'));
        });

        document.getElementById('sum-celebracion').innerText = nombre + " (" + tipo + ")";
        document.getElementById('sum-cliente').innerText = cli;
        document.getElementById('sum-fecha').innerText = fecha + " a las " + hora + " hrs";
        document.getElementById('sum-insumos').innerText = cantInsumos + " unidades (" + chks.length + " tipos de insumo)";
        document.getElementById('sum-proveedores').innerText = provsSet.size + " proveedores deducidos";
    }

    function ejecutarRegistroEventoEnVivo() {
        // Validaciones previas
        var nombre = document.getElementById('ev-nombre').value.trim();
        var idTipo = document.getElementById('ev-tipo').value;
        var idCliente = document.getElementById('ev-cliente').value;
        var fecha = document.getElementById('ev-fecha').value;
        var hora = document.getElementById('ev-hora').value;
        var aforo = document.getElementById('ev-invitados').value;
        var presupuesto = document.getElementById('ev-presupuesto').value;
        var idCoordinador = document.getElementById('ev-coordinador').value;

        if (!nombre || !idCliente || !fecha) {
            alert('Faltan datos obligatorios en la pestaña 1 (Evento).');
            switchTab('tab-evento');
            return;
        }

        var chksInsumos = document.querySelectorAll('.chk-insumo:checked');
        if (chksInsumos.length === 0) {
            alert('Debes seleccionar al menos un insumo en la pestaña 2 (Insumo).');
            switchTab('tab-insumo');
            return;
        }

        if (!idCoordinador) {
            alert('Debes asignar al menos un Coordinador Responsable en la pestaña 4 (Personal).');
            document.getElementById('ev-coordinador').focus();
            return;
        }

        // Armar pares idArticulo:cantidad
        var insumosPairs = [];
        for (var i = 0; i < chksInsumos.length; i++) {
            var chk = chksInsumos[i];
            var id = chk.getAttribute('data-id');
            var cantInput = document.getElementById('cant-' + id);
            var cant = cantInput ? parseInt(cantInput.value) || 0 : 0;
            if (cant <= 0) {
                alert('La cantidad del insumo "' + chk.getAttribute('data-nombre') + '" debe ser mayor a 0.');
                switchTab('tab-insumo');
                return;
            }
            insumosPairs.push(id + ':' + cant);
        }

        // Armar personal staff adicional
        var chkStaff = document.querySelectorAll('.chk-staff:checked');
        var staffIds = [];
        chkStaff.forEach(function (s) {
            staffIds.push(s.value);
        });

        // Deshabilitar botón durante el proceso
        var btn = document.getElementById('btn-registrar-vivo');
        btn.disabled = true;
        btn.innerText = 'Guardando transacción en Supabase...';

        // Parámetros POST
        var params = new URLSearchParams();
        params.append('nombre', nombre);
        params.append('idTipoEvento', idTipo);
        params.append('idCliente', idCliente);
        params.append('fecha', fecha);
        params.append('hora', hora);
        params.append('aforo', aforo);
        params.append('presupuesto', presupuesto);
        params.append('idCoordinador', idCoordinador);
        params.append('insumosSeleccionados', insumosPairs.join(','));

        staffIds.forEach(function (id) {
            params.append('idStaff', id);
        });

        fetch('<%= request.getContextPath()%>/evento', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
            },
            body: params.toString()
        })
            .then(function (res) {
                return res.json();
            })
            .then(function (data) {
                if (data.success) {
                    showToast(data.mensaje || '¡Evento registrado con éxito!');
                    setTimeout(function () {
                        window.location.reload();
                    }, 1400);
                } else {
                    alert('Error: ' + (data.mensaje || 'No se pudo guardar el evento.'));
                    btn.disabled = false;
                    btn.innerText = '🚀 REGISTRAR EVENTO EN VIVO';
                }
            })
            .catch(function (err) {
                console.error(err);
                alert('Ocurrió un error de comunicación con el servidor.');
                btn.disabled = false;
                btn.innerText = '🚀 REGISTRAR EVENTO EN VIVO';
            });
    }

    function showToast(msg) {
        const toast = document.getElementById('toast');
        document.getElementById('toast-text').innerText = msg;
        toast.style.display = 'flex';
        setTimeout(() => {
            toast.style.display = 'none';
        }, 3500);
    }
</script>
</body>
</html>
