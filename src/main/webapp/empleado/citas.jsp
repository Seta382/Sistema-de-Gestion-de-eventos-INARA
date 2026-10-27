<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestión de Citas - INARA</title>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&family=Playfair+Display:ital,wght@0,600;0,700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/empleado.css">
</head>
<body class="page-citas">

    <div class="wrapper">
        <h1>Gestión de Citas</h1>
        <p class="subtitle">Confirma las citas que llegan de los clientes y conviértelas en evento cuando estén listas.</p>

        <!-- Buscador: conserva el filtro de estado activo -->
        <form class="search-bar" action="${pageContext.request.contextPath}/citas-gestion" method="get">
            <input type="hidden" name="estado" value="${filtroEstado}">
            <input type="search" name="q" value="<c:out value='${busqueda}'/>"
                   placeholder="Buscar por cliente, DNI, motivo, lugar o fecha (ej. 2026-11 o 15/11/2026)" maxlength="100">
            <button type="submit">Buscar</button>
            <c:if test="${not empty busqueda}">
                <a class="clear" href="${pageContext.request.contextPath}/citas-gestion?estado=${filtroEstado}">Limpiar</a>
            </c:if>
        </form>

        <c:if test="${not empty busqueda}">
            <p class="results-info">
                ${citas.size()} resultado(s) para "<strong><c:out value="${busqueda}"/></strong>"
            </p>
        </c:if>

        <div class="filters">
            <a href="${pageContext.request.contextPath}/citas-gestion?estado=${qParamHtml}" class="${empty filtroEstado ? 'active' : ''}">Todas</a>
            <a href="${pageContext.request.contextPath}/citas-gestion?estado=PENDIENTE${qParamHtml}" class="${filtroEstado == 'PENDIENTE' ? 'active' : ''}">Pendientes</a>
            <a href="${pageContext.request.contextPath}/citas-gestion?estado=CONFIRMADA${qParamHtml}" class="${filtroEstado == 'CONFIRMADA' ? 'active' : ''}">Confirmadas</a>
            <a href="${pageContext.request.contextPath}/citas-gestion?estado=REPROGRAMADA${qParamHtml}" class="${filtroEstado == 'REPROGRAMADA' ? 'active' : ''}">Reprogramadas</a>
            <a href="${pageContext.request.contextPath}/citas-gestion?estado=FINALIZADA${qParamHtml}" class="${filtroEstado == 'FINALIZADA' ? 'active' : ''}">Finalizadas</a>
            <a href="${pageContext.request.contextPath}/citas-gestion?estado=CANCELADA${qParamHtml}" class="${filtroEstado == 'CANCELADA' ? 'active' : ''}">Canceladas</a>
        </div>

        <table>
            <tr>
                <th>Cliente</th>
                <th>Fecha</th>
                <th>Hora</th>
                <th>Modalidad</th>
                <th>Lugar</th>
                <th>Motivo</th>
                <th>Estado</th>
                <th></th>
            </tr>
            <c:forEach var="cita" items="${citas}">
                <tr>
                    <td data-label="Cliente"><c:out value="${cita.nombreCliente}"/></td>
                    <td data-label="Fecha">${cita.fecha}</td>
                    <td data-label="Hora">${cita.hora}</td>
                    <td data-label="Modalidad"><c:out value="${cita.modalidad}"/></td>
                    <td data-label="Lugar"><c:out value="${cita.lugar}"/></td>
                    <td data-label="Motivo"><c:out value="${cita.motivo}"/></td>
                    <td data-label="Estado"><span class="badge estado-${cita.estado}">${cita.estado}</span></td>
                    <td class="actions" data-label="Acciones">
                        <c:if test="${cita.estado == 'PENDIENTE' || cita.estado == 'REPROGRAMADA'}">
                            <a class="a-confirmar"
                               href="${pageContext.request.contextPath}/citas-gestion?accion=confirmar&amp;id=${cita.idCita}&amp;estado=${filtroEstado}${qParamHtml}">Confirmar</a>
                        </c:if>
                        <c:if test="${cita.estado == 'CONFIRMADA'}">
                            <a class="a-convertir"
                               href="${pageContext.request.contextPath}/citas-gestion?accion=convertir&amp;id=${cita.idCita}">Convertir en evento</a>
                        </c:if>
                        <c:if test="${cita.estado == 'PENDIENTE' || cita.estado == 'REPROGRAMADA' || cita.estado == 'CONFIRMADA'}">
                            <a class="a-cancelar"
                               href="${pageContext.request.contextPath}/citas-gestion?accion=cancelar&amp;id=${cita.idCita}&amp;estado=${filtroEstado}${qParamHtml}"
                               onclick="return confirm('¿Cancelar esta cita?')">Cancelar</a>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty citas}">
                <tr>
                    <td colspan="8" class="empty-msg">
                        <c:choose>
                            <c:when test="${not empty busqueda}">No se encontraron citas que coincidan con tu búsqueda.</c:when>
                            <c:otherwise>No hay citas para mostrar con este filtro.</c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </c:if>
        </table>

        <a href="${pageContext.request.contextPath}/empleado/dashboard.jsp" class="link-back">&larr; Volver al panel administrativo</a>
    </div>

</body>
</html>
