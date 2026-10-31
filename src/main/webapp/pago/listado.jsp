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

    // Si se abre el JSP directo (sin pasar por PagoServlet) no hay datos cargados
    if (request.getAttribute("pagos") == null) {
        response.sendRedirect(request.getContextPath() + "/pago");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>INARA — Pagos</title>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&family=Playfair+Display:ital,wght@0,600;0,700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/pago.css">
</head>
<body class="page-pago">

<header class="navbar">
    <div class="logo">INARA</div>
    <a href="${pageContext.request.contextPath}/empleado/dashboard.jsp" class="btn-volver">&larr; Volver al Centro de Control</a>
</header>

<main class="contenedor">
    <div class="card">

        <h1>Pagos</h1>
        <p class="subtitulo">Todos los pagos de los eventos. Para registrar uno nuevo, entra al evento desde el cronograma.</p>

        <c:if test="${not empty exito}">
            <div class="mensaje exito"><c:out value="${exito}"/></div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="mensaje error"><c:out value="${error}"/></div>
        </c:if>

        <form class="search-bar" action="${pageContext.request.contextPath}/pago" method="get">
            <input type="hidden" name="filtro" value="${filtro}">
            <input type="search" name="q" value="<c:out value='${busqueda}'/>"
                   placeholder="Buscar por evento o cliente" maxlength="100">
            <button type="submit" class="btn-primary">Buscar</button>
            <c:if test="${not empty busqueda}">
                <a class="clear" href="${pageContext.request.contextPath}/pago?filtro=${filtro}">Limpiar</a>
            </c:if>
        </form>

        <div class="filters">
            <a href="${pageContext.request.contextPath}/pago?filtro=PENDIENTE${qParamHtml}" class="${filtro == 'PENDIENTE' ? 'active' : ''}">Por confirmar (${totalPendientes})</a>
            <a href="${pageContext.request.contextPath}/pago?filtro=COMPLETADO${qParamHtml}" class="${filtro == 'COMPLETADO' ? 'active' : ''}">Completados</a>
            <a href="${pageContext.request.contextPath}/pago?filtro=RECHAZADO${qParamHtml}" class="${filtro == 'RECHAZADO' ? 'active' : ''}">Rechazados</a>
            <a href="${pageContext.request.contextPath}/pago?filtro=TODOS${qParamHtml}" class="${filtro == 'TODOS' ? 'active' : ''}">Todos</a>
        </div>

        <c:if test="${not empty busqueda}">
            <p class="results-info">
                ${pagos.size()} resultado(s) para "<strong><c:out value="${busqueda}"/></strong>"
            </p>
        </c:if>

        <div class="tabla-wrap">
            <table>
                <tr>
                    <th>Pago</th>
                    <th>Cliente</th>
                    <th>Método</th>
                    <th class="num">Monto</th>
                    <th>Estado</th>
                    <th></th>
                </tr>
                <c:forEach var="p" items="${pagos}">
                    <tr class="${p.estado == 'RECHAZADO' ? 'fila-inactiva' : ''}">
                        <td class="celda-titulo">
                            <span class="nombre">#${p.idPago} · <c:out value="${p.nombreEvento}"/></span>
                            <span class="desc"><c:out value="${p.fechaPagoTxt}"/></span>
                        </td>
                        <td data-label="Cliente"><c:out value="${p.nombreCliente}"/></td>
                        <td data-label="Método"><c:out value="${p.metodoPago}"/></td>
                        <td data-label="Monto" class="num precio">S/ <fmt:formatNumber value="${p.monto}" minFractionDigits="2" maxFractionDigits="2"/></td>
                        <td data-label="Estado"><span class="badge estado-${p.estado}">${p.estado}</span></td>
                        <td class="actions">
                            <a class="btn-editar" href="${pageContext.request.contextPath}/pago?idEvento=${p.idEvento}">Abrir</a>
                            <c:if test="${p.estado == 'PENDIENTE'}">
                                <form action="${pageContext.request.contextPath}/pago" method="post"
                                      onsubmit="return confirm('¿Confirmar que recibiste el pago #${p.idPago}?')">
                                    <input type="hidden" name="accion" value="confirmar">
                                    <input type="hidden" name="id" value="${p.idPago}">
                                    <input type="hidden" name="filtro" value="${filtro}">
                                    <input type="hidden" name="q" value="<c:out value='${busqueda}'/>">
                                    <button type="submit" class="btn-aceptar">Confirmar</button>
                                </form>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty pagos}">
                    <tr>
                        <td colspan="6" class="empty-msg">
                            <c:choose>
                                <c:when test="${not empty busqueda}">No se encontraron pagos que coincidan con tu búsqueda.</c:when>
                                <c:otherwise>No hay pagos para mostrar con este filtro.</c:otherwise>
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
