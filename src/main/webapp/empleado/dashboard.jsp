<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="pe.edu.eventos.dto.UsuarioDTO" %>
<%@ page import="pe.edu.eventos.util.Constantes" %>
<%@ page import="pe.edu.eventos.model.Cliente" %>
<%@ page import="pe.edu.eventos.model.TipoEvento" %>
<%@ page import="pe.edu.eventos.model.Articulo" %>
<%@ page import="pe.edu.eventos.model.Empleado" %>
<%@ page import="pe.edu.eventos.model.Evento" %>
<%@ page import="pe.edu.eventos.model.Cita" %>
<%@ page import="pe.edu.eventos.service.EventoService" %>
<%@ page import="pe.edu.eventos.service.CitaService" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%!
    // Escapa texto escrito por usuarios antes de imprimirlo en HTML
    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
%>
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
    // El panel del dashboard muestra solo los últimos eventos; el resto está en el cronograma
    final int EVENTOS_EN_DASHBOARD = 4;
    List<Evento> listaEventosRecientes = eventoService.listarEventosRecientes(EVENTOS_EN_DASHBOARD);
    java.util.Map<String, Integer> conteoEventos = eventoService.contarEventosPorPeriodo();
    int totalEventos = conteoEventos.get("TODOS") != null ? conteoEventos.get("TODOS") : 0;
    List<Cita> listaCitas = eventoService.listarCitasDisponibles(); // FIX: nuevo
    int citasPendientes = new CitaService().contarPorEstado("PENDIENTE");
    // Llega desde "Convertir en evento" en el panel de citas (?idCita=X)
    String idCitaPreseleccionada = request.getParameter("idCita");
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
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/empleado.css">
</head>
<body class="page-dashboard">

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
                    <div class="admin-name"><%= esc(usuario.getNombreCompleto())%></div>
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
        <a href="<%= request.getContextPath()%>/evento?accion=cronograma&amp;filtro=TODOS"
           style="text-decoration: none; color: inherit; display: block;">

            <div class="kpi-card">
                <div>
                    <div class="kpi-label">Eventos en Cronograma</div>
                    <div class="kpi-value" id="count-eventos"><%= totalEventos%> Activos</div>
                </div>
                <div class="kpi-icon">&#127878;</div>
            </div>

        </a>

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

        <a href="<%= request.getContextPath()%>/citas-gestion"
           style="text-decoration: none; color: inherit; display: block;">

            <div class="kpi-card">
                <div>
                    <div class="kpi-label">Citas por Atender</div>
                    <div class="kpi-value"><%= citasPendientes%> Pendientes</div>
                </div>
                <div class="kpi-icon">&#128197;</div>
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
                <% if (listaEventosRecientes.isEmpty()) { %>
                <div style="text-align: center; padding: 48px 20px; color: var(--text-secondary);">
                    <div style="font-size: 2.8rem; margin-bottom: 12px;">✨</div>
                    <h3 style="font-size: 1.15rem; color: var(--text-primary); margin-bottom: 8px;">No hay eventos en producción aún</h3>
                    <p style="font-size: 0.86rem; line-height: 1.6;">
                        Completa el flujo en el <strong>Centro de Registro</strong> para coordinar insumos, proveedores y personal de Supabase.
                    </p>
                </div>
                <% } else { %>
                <% for (Evento ev : listaEventosRecientes) {
                    // Datos que se muestran en la ventana de detalle al hacer clic en la tarjeta
                    String evNombre = ev.getNombre() != null ? String.valueOf(ev.getNombre()) : "Evento INARA";
                    String evCliente = ev.getNombreCliente() != null ? String.valueOf(ev.getNombreCliente()) : "Cliente General";
                    String evTipo = ev.getTipoCelebracion() != null ? String.valueOf(ev.getTipoCelebracion()) : "Evento";
                    String evFecha = ev.getFechaEvento() != null ? String.valueOf(ev.getFechaEvento()) : "Próximo";
                    String evHora = ev.getHoraEvento() != null ? String.valueOf(ev.getHoraEvento()) : "18:00";
                    String evPresupuesto = ev.getPresupuesto() != null ? nf.format(ev.getPresupuesto()) : "S/ 0.00";
                    String evCoordinador = ev.getNombreCoordinador() != null ? String.valueOf(ev.getNombreCoordinador()) : "Staff Asignado";
                %>
                <article class="event-live-card" role="button" tabindex="0" title="Ver detalle del evento"
                         data-nombre="<%= esc(evNombre) %>"
                         data-cliente="<%= esc(evCliente) %>"
                         data-tipo="<%= esc(evTipo) %>"
                         data-fecha="<%= esc(evFecha) %>"
                         data-hora="<%= esc(evHora) %>"
                         data-invitados="<%= ev.getNumInvitados() %>"
                         data-presupuesto="<%= esc(evPresupuesto) %>"
                         data-coordinador="<%= esc(evCoordinador) %>"
                         data-estado="<%= esc(String.valueOf(ev.getEstado())) %>">
                    <div class="event-top-info">
                        <div>
                            <h3 class="event-name">✨ <%= ev.getNombre() != null ? esc(ev.getNombre()) : "Evento INARA"%></h3>
                            <p style="font-size: 0.82rem; color: var(--rose-dark); font-weight: 600; margin-top: 2px;">
                                Titular: <%= ev.getNombreCliente() != null ? esc(ev.getNombreCliente()) : "Cliente General"%>
                            </p>
                        </div>
                        <span class="event-category-badge gold"><%= ev.getTipoCelebracion() != null ? esc(ev.getTipoCelebracion()) : "Evento"%></span>
                    </div>

                    <div class="event-details-row">
                        <div class="event-detail-item">📅 <%= ev.getFechaEvento() != null ? ev.getFechaEvento() : "Próximo"%></div>
                        <div class="event-detail-item">🕒 <%= ev.getHoraEvento() != null ? ev.getHoraEvento() : "18:00"%></div>
                        <div class="event-detail-item">👥 <%= ev.getNumInvitados()%> Invitados</div>
                        <div class="event-detail-item">💰 <%= ev.getPresupuesto() != null ? nf.format(ev.getPresupuesto()) : "S/ 0.00"%></div>
                    </div>

                    <div style="font-size: 0.8rem; color: var(--text-secondary); margin-bottom: 10px;">
                        👤 Coordinador: <strong><%= ev.getNombreCoordinador() != null ? esc(ev.getNombreCoordinador()) : "Staff Asignado"%></strong>
                    </div>

                    <div class="progress-container">
                        <div class="progress-header">
                            <span>🟢 Fase: <%= esc(ev.getEstado())%></span>
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

            <% if (totalEventos > 0) { %>
            <div class="stream-footer">
                <span>
                    <% if (totalEventos > listaEventosRecientes.size()) { %>
                    Mostrando los <strong><%= listaEventosRecientes.size()%></strong> más recientes de <strong><%= totalEventos%></strong>
                    <% } else { %>
                    <strong><%= totalEventos%></strong> evento(s) registrado(s)
                    <% } %>
                </span>
                <a href="<%= request.getContextPath()%>/evento?accion=cronograma&amp;filtro=TODOS">Ver cronograma completo &rarr;</a>
            </div>
            <% } %>
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
                                <option value="<%= te.getIdTipoEvento()%>"><%= esc(te.getNombre())%></option>
                                <% } %>
                            </select>
                        </div>
                        <div class="field-group">
                            <label>Cliente Titular Real *</label>
                            <select id="ev-cliente" required>
                                <option value="">-- Selecciona el Cliente --</option>
                                <% for (Cliente cli : listaClientes) {%>
                                <option value="<%= cli.getIdCliente()%>">
                                    <%= esc(cli.getNombre())%> <%= esc(cli.getApellido())%> (DNI: <%= cli.getDni() != null ? esc(cli.getDni()) : "S/D"%>)
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

                    <div class="field-group">
                        <label>Cita de Origen (opcional)</label>
                        <select id="ev-cita" onchange="aplicarCitaSeleccionada()">
                            <option value="">-- Ninguna, evento directo --</option>
                            <% for (Cita cit : listaCitas) { %>
                            <option value="<%= cit.getIdCita()%>" data-cliente="<%= cit.getIdCliente()%>"
                                    <%= String.valueOf(cit.getIdCita()).equals(idCitaPreseleccionada) ? "selected" : ""%>>
                                <%= esc(cit.getNombreCliente())%> &middot; <%= cit.getFecha()%> <%= cit.getHora()%>
                                <%= cit.getMotivo() != null && !cit.getMotivo().isBlank() ? "— " + esc(cit.getMotivo()) : ""%>
                            </option>
                            <% } %>
                        </select>
                        <% if (listaCitas.isEmpty()) { %>
                        <small style="color: var(--text-secondary); font-size: 0.78rem;">No hay citas confirmadas pendientes de convertir en evento.</small>
                        <% } %>
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
                                       data-nombre="<%= esc(art.getNombre())%>"
                                       data-precio="<%= art.getPrecio()%>"
                                       data-stock="<%= art.getStock()%>"
                                       data-prov-id="<%= art.getIdProveedor()%>"
                                       data-prov-nombre="<%= art.getNombreProveedor() != null ? esc(art.getNombreProveedor()) : "Proveedor General"%>"
                                       onchange="toggleInsumo(this)">
                            </td>
                            <td>
                                <strong><%= esc(art.getNombre())%></strong>
                            </td>
                            <td>
                                <span class="badge-prov"><%= art.getNombreProveedor() != null ? esc(art.getNombreProveedor()) : "General"%></span>
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
                            <%= esc(emp.getNombreCompleto())%> &bull; <%= emp.getCargo() != null ? esc(emp.getCargo()) : "Staff"%> (<%= emp.getArea() != null ? esc(emp.getArea()) : "Operaciones"%>)
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
                            <div style="font-weight: 600; font-size: 0.88rem; color: var(--text-primary);"><%= esc(emp.getNombreCompleto())%></div>
                            <div style="font-size: 0.76rem; color: var(--text-secondary);"><%= emp.getCargo() != null ? esc(emp.getCargo()) : "Colaborador"%> &bull; <%= emp.getArea() != null ? esc(emp.getArea()) : "Protocolo"%></div>
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

<!-- Ventana flotante: detalle del evento (se abre al hacer clic en una tarjeta) -->
<dialog id="evento-dialog" class="evento-dialog" aria-labelledby="evd-nombre">
    <div class="evd-header">
        <button type="button" class="evd-close" aria-label="Cerrar"
                onclick="document.getElementById('evento-dialog').close()">&times;</button>
        <span class="evd-tipo" id="evd-tipo"></span>
        <h2 class="evd-title" id="evd-nombre"></h2>
        <p class="evd-cliente">Titular: <strong id="evd-cliente"></strong></p>
    </div>

    <div class="evd-body">
        <dl class="evd-grid">
            <div class="evd-item"><dt>Fecha</dt><dd id="evd-fecha"></dd></div>
            <div class="evd-item"><dt>Hora de inicio</dt><dd id="evd-hora"></dd></div>
            <div class="evd-item"><dt>Aforo</dt><dd id="evd-invitados"></dd></div>
            <div class="evd-item"><dt>Presupuesto</dt><dd id="evd-presupuesto"></dd></div>
            <div class="evd-item"><dt>Coordinador</dt><dd id="evd-coordinador"></dd></div>
            <div class="evd-item"><dt>Fase</dt><dd id="evd-estado"></dd></div>
        </dl>
    </div>

    <div class="evd-footer">
        <button type="button" class="btn-step-prev"
                onclick="document.getElementById('evento-dialog').close()">Cerrar</button>
    </div>
</dialog>

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

    // Escapa texto antes de insertarlo con innerHTML (los nombres vienen de la base de datos)
    function escaparHtml(texto) {
        return String(texto === null || texto === undefined ? '' : texto)
            .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
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
                return escaparHtml(item.nombre) + ' (' + escaparHtml(item.cantidad) + ' u.)';
            }).join(', ');

            html += '<div class="prov-card">' +
                '<div>' +
                '<h4>🚚 ' + escaparHtml(prov.nombre) + '</h4>' +
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

    document.addEventListener('DOMContentLoaded', function () {
        var selCita = document.getElementById('ev-cita');
        if (selCita && selCita.value) {
            aplicarCitaSeleccionada();
        }
    });

    function aplicarCitaSeleccionada() {
        var sel = document.getElementById('ev-cita');
        var opt = sel.options[sel.selectedIndex];
        var idClienteCita = opt.getAttribute('data-cliente');
        if (idClienteCita) {
            document.getElementById('ev-cliente').value = idClienteCita;
        }
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
        var idCita = document.getElementById('ev-cita').value;

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
        if (idCita) {
            params.append('idCita', idCita);
        }
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

<!-- Ventana flotante de detalle del evento -->
<script>
(function () {
    var dlg = document.getElementById('evento-dialog');
    if (!dlg || typeof dlg.showModal !== 'function') return;

    function set(id, texto) { document.getElementById(id).textContent = texto; }

    function abrirDetalleEvento(card) {
        var d = card.dataset;
        set('evd-tipo', d.tipo);
        set('evd-nombre', d.nombre);
        set('evd-cliente', d.cliente);
        set('evd-fecha', d.fecha);
        set('evd-hora', d.hora + ' hrs');
        set('evd-invitados', d.invitados + ' invitados');
        set('evd-presupuesto', d.presupuesto);
        set('evd-coordinador', d.coordinador);
        set('evd-estado', d.estado);
        dlg.showModal();
    }

    document.querySelectorAll('.event-live-card[data-nombre]').forEach(function (card) {
        card.addEventListener('click', function () { abrirDetalleEvento(card); });
        card.addEventListener('keydown', function (e) {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                abrirDetalleEvento(card);
            }
        });
    });

    // Clic en el fondo oscuro cierra la ventana
    dlg.addEventListener('click', function (e) {
        if (e.target === dlg) dlg.close();
    });
})();
</script>
</body>
</html>
