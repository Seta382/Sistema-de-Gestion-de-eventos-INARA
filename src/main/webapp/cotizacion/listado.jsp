<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="pe.edu.eventos.dto.UsuarioDTO" %>
<%@ page import="pe.edu.eventos.util.Constantes" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
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

    // Si se abre el JSP directo (sin pasar por CotizacionServlet) no hay datos cargados
    if (request.getAttribute("cotizaciones") == null) {
        response.sendRedirect(request.getContextPath() + "/cotizacion");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>INARA — Cotizaciones</title>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&family=Playfair+Display:ital,wght@0,600;0,700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/cotizacion.css">
</head>
<body class="page-cotizacion">

<header class="navbar">
    <div class="logo">INARA</div>
    <a href="${pageContext.request.contextPath}/empleado/dashboard.jsp" class="btn-volver">&larr; Volver al Centro de Control</a>
</header>

<main class="contenedor">
    <div class="card">

        <h1>Cotizaciones</h1>
        <p class="subtitulo">Propuestas enviadas a los clientes. Para crear una nueva, entra al evento desde el cronograma y pulsa «Cotizar».</p>

        <c:if test="${not empty exito}">
            <div class="mensaje exito"><c:out value="${exito}"/></div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="mensaje error"><c:out value="${error}"/></div>
        </c:if>

        <form class="search-bar" action="${pageContext.request.contextPath}/cotizacion" method="get">
            <input type="hidden" name="filtro" value="${filtro}">
            <input type="search" name="q" value="<c:out value='${busqueda}'/>"
                   placeholder="Buscar por evento o cliente" maxlength="100">
            <button type="submit" class="btn-primary">Buscar</button>
            <c:if test="${not empty busqueda}">
                <a class="clear" href="${pageContext.request.contextPath}/cotizacion?filtro=${filtro}">Limpiar</a>
            </c:if>
        </form>

        <div class="filters">
            <a href="${pageContext.request.contextPath}/cotizacion?filtro=PENDIENTE${qParamHtml}" class="${filtro == 'PENDIENTE' ? 'active' : ''}">Pendientes (${totalPendientes})</a>
            <a href="${pageContext.request.contextPath}/cotizacion?filtro=ACEPTADA${qParamHtml}" class="${filtro == 'ACEPTADA' ? 'active' : ''}">Aceptadas</a>
            <a href="${pageContext.request.contextPath}/cotizacion?filtro=RECHAZADA${qParamHtml}" class="${filtro == 'RECHAZADA' ? 'active' : ''}">Rechazadas</a>
            <a href="${pageContext.request.contextPath}/cotizacion?filtro=MODIFICADA${qParamHtml}" class="${filtro == 'MODIFICADA' ? 'active' : ''}">Modificadas</a>
            <a href="${pageContext.request.contextPath}/cotizacion?filtro=TODAS${qParamHtml}" class="${filtro == 'TODAS' ? 'active' : ''}">Todas</a>
        </div>

        <c:if test="${not empty busqueda}">
            <p class="results-info">
                ${cotizaciones.size()} resultado(s) para "<strong><c:out value="${busqueda}"/></strong>"
            </p>
        </c:if>

        <div class="tabla-wrap">
            <table>
                <tr>
                    <th>Cotización</th>
                    <th>Cliente</th>
                    <th>Fecha del evento</th>
                    <th class="num">Total</th>
                    <th>Estado</th>
                    <th></th>
                </tr>
                <c:forEach var="co" items="${cotizaciones}">
                    <tr class="${co.estado == 'MODIFICADA' ? 'fila-inactiva' : ''}">
                        <td class="celda-titulo">
                            <span class="nombre">#${co.idCotizacion} · <c:out value="${co.nombreEvento}"/></span>
                            <span class="desc"><c:out value="${co.fecha}"/></span>
                        </td>
                        <td data-label="Cliente"><c:out value="${co.nombreCliente}"/></td>
                        <td data-label="Fecha del evento"><c:out value="${co.fechaEvento}"/></td>
                        <td class="num" data-label="Total"><span class="precio">S/ <fmt:formatNumber value="${co.total}" minFractionDigits="2" maxFractionDigits="2"/></span></td>
                        <td data-label="Estado"><span class="badge estado-${co.estado}">${co.estado}</span></td>
                        <td class="actions">
                            <a class="btn-editar" href="${pageContext.request.contextPath}/cotizacion?idEvento=${co.idEvento}">Abrir</a>
                            <c:if test="${co.estado == 'PENDIENTE'}">
                                <form action="${pageContext.request.contextPath}/cotizacion" method="post"
                                      onsubmit="return confirm('¿Registrar que el cliente ACEPTÓ la cotización #${co.idCotizacion}?')">
                                    <input type="hidden" name="accion" value="aceptar">
                                    <input type="hidden" name="id" value="${co.idCotizacion}">
                                    <input type="hidden" name="filtro" value="${filtro}">
                                    <input type="hidden" name="q" value="<c:out value='${busqueda}'/>">
                                    <button type="submit" class="btn-link btn-reactivar">Marcar aceptada</button>
                                </form>
                                <form action="${pageContext.request.contextPath}/cotizacion" method="post"
                                      onsubmit="return confirm('¿Registrar que el cliente RECHAZÓ la cotización #${co.idCotizacion}?')">
                                    <input type="hidden" name="accion" value="rechazar">
                                    <input type="hidden" name="id" value="${co.idCotizacion}">
                                    <input type="hidden" name="filtro" value="${filtro}">
                                    <input type="hidden" name="q" value="<c:out value='${busqueda}'/>">
                                    <button type="submit" class="btn-link btn-baja">Marcar rechazada</button>
                                </form>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty cotizaciones}">
                    <tr>
                        <td colspan="6" class="empty-msg">
                            <c:choose>
                                <c:when test="${not empty busqueda}">No se encontraron cotizaciones que coincidan con tu búsqueda.</c:when>
                                <c:otherwise>No hay cotizaciones para mostrar con este filtro.</c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:if>
            </table>
        </div>

    </div>
</main>

</body>
</html>
