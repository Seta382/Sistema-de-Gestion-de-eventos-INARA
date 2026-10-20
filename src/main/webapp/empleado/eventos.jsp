<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="pe.edu.eventos.dto.UsuarioDTO" %>
<%@ page import="pe.edu.eventos.util.Constantes" %>
<%@ page import="pe.edu.eventos.model.Evento" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.Map" %>
<%!
    // Escapa texto escrito por usuarios antes de imprimirlo en HTML
    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    // Texto escapado, o un valor por defecto si viene vacío
    private static String texto(String s, String porDefecto) {
        return (s == null || s.trim().isEmpty()) ? esc(porDefecto) : esc(s.trim());
    }

    private static int cuenta(Map<String, Integer> m, String clave) {
        Integer v = m == null ? null : m.get(clave);
        return v == null ? 0 : v;
    }
%>
<%
    UsuarioDTO usuario = (UsuarioDTO) session.getAttribute(Constantes.SESION_USUARIO);
    if (usuario == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    String rol = usuario.getRol() != null ? usuario.getRol().toUpperCase() : "";
    if (!Constantes.ROL_ADMIN.equals(rol) && !Constantes.ROL_EMPLEADO.equals(rol)) {
        response.sendRedirect(request.getContextPath() + "/cliente/dashboard.jsp");
        return;
    }

    // Si se abre el JSP directo (sin pasar por EventoServlet) no hay datos cargados
    @SuppressWarnings("unchecked")
    List<Evento> eventos = (List<Evento>) request.getAttribute("eventos");
    if (eventos == null) {
        response.sendRedirect(request.getContextPath() + "/evento?accion=cronograma");
        return;
    }
    @SuppressWarnings("unchecked")
    Map<String, Integer> conteos = (Map<String, Integer>) request.getAttribute("conteos");

    String busqueda = request.getAttribute("busqueda") != null ? (String) request.getAttribute("busqueda") : "";
    String filtro = request.getAttribute("filtro") != null ? (String) request.getAttribute("filtro") : "PROXIMOS";
    String qParamHtml = request.getAttribute("qParamHtml") != null ? (String) request.getAttribute("qParamHtml") : "";
    LocalDate hoy = (LocalDate) request.getAttribute("hoy");
    String ctx = request.getContextPath();
    NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>INARA — Eventos en Cronograma</title>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&family=Playfair+Display:ital,wght@0,600;0,700&display=swap" rel="stylesheet">
    <style>
        :root {
            --bg-primary: #F8F4F1; --rose-dark: #A66A6A; --rose-deep: #743E3E; --rose-light: #D8A7A7;
            --gold: #C6A15B; --text-primary: #3D3333; --text-secondary: #706464;
            --white: #FFFFFF; --border-soft: #EFE8E2;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: 'Montserrat', sans-serif; background: var(--bg-primary); color: var(--text-primary); min-height: 100vh; }

        .navbar { background: var(--white); padding: 18px 40px; border-bottom: 1px solid var(--border-soft);
                  display: flex; justify-content: space-between; align-items: center; }
        .logo { font-size: 1.7rem; font-weight: 700; letter-spacing: 3px; color: var(--rose-dark); }
        .btn-volver { text-decoration: none; color: var(--rose-dark); border: 1px solid var(--rose-light);
                      padding: 8px 16px; border-radius: 20px; font-size: 0.85rem; }

        .contenedor { max-width: 1200px; margin: 36px auto; padding: 0 20px; }
        .card { background: var(--white); padding: 36px 40px; border-radius: 24px; border: 1px solid var(--border-soft); }
        h1 { font-family: 'Playfair Display', Georgia, serif; font-size: 1.6rem; margin-bottom: 4px; }
        .subtitulo { color: var(--text-secondary); font-size: 0.85rem; margin-bottom: 22px; }

        .search-bar { display: flex; gap: 10px; align-items: center; margin-bottom: 14px; flex-wrap: wrap; }
        .search-bar input[type="search"] { flex: 1; min-width: 260px; padding: 12px 16px; border: 1.5px solid var(--border-soft);
            border-radius: 12px; background: #FAF8F6; font-size: 0.9rem; font-family: inherit; color: var(--text-primary); outline: none; }
        .search-bar input[type="search"]:focus { border-color: var(--rose-dark); background: var(--white);
            box-shadow: 0 0 0 3px rgba(216, 167, 167, 0.25); }
        .btn-primary { padding: 12px 22px; border: none; border-radius: 12px; cursor: pointer;
            background: linear-gradient(135deg, var(--gold), var(--rose-dark)); color: var(--white);
            font-size: 0.82rem; font-weight: 700; letter-spacing: 0.5px; text-transform: uppercase; font-family: inherit; }
        .clear { font-size: 0.82rem; color: var(--rose-dark); text-decoration: none; font-weight: 600; }
        .results-info { font-size: 0.82rem; color: var(--text-secondary); margin-bottom: 12px; }

        .filters { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 16px; }
        .filters a { padding: 7px 14px; border-radius: 20px; font-size: 0.78rem; font-weight: 600; text-decoration: none;
                     color: var(--text-secondary); background: #FAF8F6; border: 1px solid var(--border-soft); }
        .filters a.active { background: var(--rose-dark); color: var(--white); border-color: var(--rose-dark); }

        .tabla-wrap { overflow-x: auto; }
        table { width: 100%; border-collapse: collapse; }
        th, td { text-align: left; padding: 12px 10px; border-bottom: 1px solid var(--border-soft); font-size: 0.86rem; vertical-align: middle; }
        th { text-transform: uppercase; font-size: 0.72rem; letter-spacing: 0.5px; color: var(--text-secondary); white-space: nowrap; }
        .nombre { display: block; font-weight: 600; }
        .tipo { display: inline-block; margin-top: 4px; font-size: 0.68rem; font-weight: 700; letter-spacing: 0.6px; text-transform: uppercase;
                padding: 2px 9px; border-radius: 12px; background: rgba(198, 161, 91, 0.2); color: #8C6A29; }
        .hora { display: block; font-size: 0.78rem; color: var(--text-secondary); }
        .fila-pasada td { opacity: 0.62; }
        .nowrap { white-space: nowrap; }

        .badge { padding: 4px 11px; border-radius: 14px; font-size: 0.72rem; font-weight: 700; white-space: nowrap; }
        .badge-hoy { background: #FDF3E3; color: #8C6A29; margin-left: 6px; }
        .estado-CONFIRMADO { background: #F4F8F4; color: #2F6B4F; }
        .estado-SOLICITADO { background: #FDF3E3; color: #8C6A29; }
        .estado-COTIZADO   { background: #EFE8F5; color: #5B3E8C; }
        .estado-EN_PROCESO { background: #E8F1FA; color: #2C5D8C; }
        .estado-FINALIZADO { background: #F0F0F0; color: #555555; }
        .estado-CANCELADO  { background: #FDF3F3; color: #923838; }
        .empty-msg { text-align: center; padding: 30px; color: var(--text-secondary); }

        @media (max-width: 640px) {
            .card { padding: 24px 18px; }
            .navbar { padding: 14px 20px; }
        }
    </style>
</head>
<body>

<header class="navbar">
    <div class="logo">INARA</div>
    <a href="<%= ctx %>/empleado/dashboard.jsp" class="btn-volver">&larr; Volver al Centro de Control</a>
</header>

<main class="contenedor">
    <div class="card">

        <h1>Eventos en Cronograma</h1>
        <p class="subtitulo">Todos los eventos registrados. Los próximos aparecen primero, del más cercano al más lejano.</p>

        <!-- Buscador: conserva el filtro de período activo -->
        <form class="search-bar" action="<%= ctx %>/evento" method="get">
            <input type="hidden" name="accion" value="cronograma">
            <input type="hidden" name="filtro" value="<%= esc(filtro) %>">
            <input type="search" name="q" value="<%= esc(busqueda) %>" maxlength="100"
                   placeholder="Buscar por evento, cliente, DNI, tipo, lugar, coordinador o fecha (ej. 2026-12 o 15/12/2026)">
            <button type="submit" class="btn-primary">Buscar</button>
            <% if (!busqueda.isEmpty()) { %>
            <a class="clear" href="<%= ctx %>/evento?accion=cronograma&amp;filtro=<%= esc(filtro) %>">Limpiar</a>
            <% } %>
        </form>

        <div class="filters">
            <a href="<%= ctx %>/evento?accion=cronograma&amp;filtro=PROXIMOS<%= qParamHtml %>"
               class="<%= "PROXIMOS".equals(filtro) ? "active" : "" %>">Próximos (<%= cuenta(conteos, "PROXIMOS") %>)</a>
            <a href="<%= ctx %>/evento?accion=cronograma&amp;filtro=PASADOS<%= qParamHtml %>"
               class="<%= "PASADOS".equals(filtro) ? "active" : "" %>">Pasados (<%= cuenta(conteos, "PASADOS") %>)</a>
            <a href="<%= ctx %>/evento?accion=cronograma&amp;filtro=TODOS<%= qParamHtml %>"
               class="<%= "TODOS".equals(filtro) ? "active" : "" %>">Todos (<%= cuenta(conteos, "TODOS") %>)</a>
        </div>

        <p class="results-info">
            <%= eventos.size() %> evento(s)
            <% if (!busqueda.isEmpty()) { %>para "<strong><%= esc(busqueda) %></strong>"<% } %>
        </p>

        <div class="tabla-wrap">
            <table>
                <tr>
                    <th>Evento</th>
                    <th>Cliente titular</th>
                    <th>Fecha y hora</th>
                    <th>Lugar</th>
                    <th>Invitados</th>
                    <th>Presupuesto</th>
                    <th>Coordinador</th>
                    <th>Estado</th>
                </tr>
                <% for (Evento ev : eventos) {
                       boolean pasado = hoy != null && ev.getFechaEvento() != null && ev.getFechaEvento().isBefore(hoy);
                       boolean esHoy = hoy != null && ev.getFechaEvento() != null && ev.getFechaEvento().isEqual(hoy);
                %>
                <tr class="<%= pasado ? "fila-pasada" : "" %>">
                    <td>
                        <span class="nombre"><%= texto(ev.getNombre(), "Evento INARA") %></span>
                        <span class="tipo"><%= texto(ev.getTipoCelebracion(), "Evento") %></span>
                    </td>
                    <td><%= texto(ev.getNombreCliente(), "—") %></td>
                    <td class="nowrap">
                        <%= ev.getFechaEvento() != null ? ev.getFechaEvento() : "—" %>
                        <% if (esHoy) { %><span class="badge badge-hoy">Hoy</span><% } %>
                        <% if (ev.getHoraEvento() != null) { %><span class="hora"><%= ev.getHoraEvento() %></span><% } %>
                    </td>
                    <td><%= texto(ev.getLugar(), "—") %></td>
                    <td><%= ev.getNumInvitados() != null ? ev.getNumInvitados() : 0 %></td>
                    <td class="nowrap"><%= ev.getPresupuesto() != null ? nf.format(ev.getPresupuesto()) : "—" %></td>
                    <td><%= texto(ev.getNombreCoordinador(), "Sin asignar") %></td>
                    <td><span class="badge estado-<%= esc(ev.getEstado()) %>"><%= texto(ev.getEstado(), "—") %></span></td>
                </tr>
                <% } %>
                <% if (eventos.isEmpty()) { %>
                <tr>
                    <td colspan="8" class="empty-msg">
                        <%= !busqueda.isEmpty()
                                ? "No se encontraron eventos que coincidan con tu búsqueda."
                                : "No hay eventos para mostrar con este filtro." %>
                    </td>
                </tr>
                <% } %>
            </table>
        </div>

    </div>
</main>

</body>
</html>
