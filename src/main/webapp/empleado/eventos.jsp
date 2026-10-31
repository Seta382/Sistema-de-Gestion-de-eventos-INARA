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
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/empleado.css">
</head>
<body class="page-eventos">

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
                    <th></th>
                </tr>
                <% for (Evento ev : eventos) {
                       boolean pasado = hoy != null && ev.getFechaEvento() != null && ev.getFechaEvento().isBefore(hoy);
                       boolean esHoy = hoy != null && ev.getFechaEvento() != null && ev.getFechaEvento().isEqual(hoy);
                %>
                <tr class="<%= pasado ? "fila-pasada" : "" %>">
                    <td class="celda-titulo">
                        <span class="nombre"><%= texto(ev.getNombre(), "Evento INARA") %></span>
                        <span class="tipo"><%= texto(ev.getTipoCelebracion(), "Evento") %></span>
                    </td>
                    <td data-label="Cliente titular"><%= texto(ev.getNombreCliente(), "—") %></td>
                    <td class="nowrap" data-label="Fecha y hora">
                        <div class="valor">
                        <%= ev.getFechaEvento() != null ? ev.getFechaEvento() : "—" %>
                        <% if (esHoy) { %><span class="badge badge-hoy">Hoy</span><% } %>
                        <% if (ev.getHoraEvento() != null) { %><span class="hora"><%= ev.getHoraEvento() %></span><% } %>
                        </div>
                    </td>
                    <td data-label="Lugar"><%= texto(ev.getLugar(), "—") %></td>
                    <td data-label="Invitados"><%= ev.getNumInvitados() != null ? ev.getNumInvitados() : 0 %></td>
                    <td class="nowrap" data-label="Presupuesto"><%= ev.getPresupuesto() != null ? nf.format(ev.getPresupuesto()) : "—" %></td>
                    <td data-label="Coordinador"><%= texto(ev.getNombreCoordinador(), "Sin asignar") %></td>
                    <td data-label="Estado"><span class="badge estado-<%= esc(ev.getEstado()) %>"><%= texto(ev.getEstado(), "—") %></span></td>
                    <td class="actions">
                        <a class="btn-cotizar" href="<%= ctx %>/cotizacion?idEvento=<%= ev.getIdEvento() %>"><%= "SOLICITADO".equals(ev.getEstado()) || "COTIZADO".equals(ev.getEstado()) ? "Cotizar" : "Cotizaciones" %></a>
                        <% if ("COTIZADO".equals(ev.getEstado()) || "CONFIRMADO".equals(ev.getEstado())) { %>
                        <a class="btn-cotizar" href="<%= ctx %>/acuerdo?idEvento=<%= ev.getIdEvento() %>">Acuerdo</a>
                        <% } %>
                        <% if ("CONFIRMADO".equals(ev.getEstado()) || "EN_PROCESO".equals(ev.getEstado()) || "FINALIZADO".equals(ev.getEstado())) { %>
                        <a class="btn-cotizar" href="<%= ctx %>/pago?idEvento=<%= ev.getIdEvento() %>">Pagos</a>
                        <a class="btn-cotizar" href="<%= ctx %>/seguimiento?idEvento=<%= ev.getIdEvento() %>">Seguimiento</a>
                        <% } %>
                    </td>
                </tr>
                <% } %>
                <% if (eventos.isEmpty()) { %>
                <tr>
                    <td colspan="9" class="empty-msg">
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
