<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mis Citas - INARA</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/cliente.css">
</head>
<body class="page-mis-citas">

<div class="citas-wrapper">
    <div class="form-brand-header">
        <h2>Mis citas</h2>
        <div class="golden-accent-bar"></div>
        <p>
            <a href="${pageContext.request.contextPath}/citas?accion=nueva">+ Agendar nueva cita</a>
            &nbsp;&middot;&nbsp;
            <a href="${pageContext.request.contextPath}/cliente/dashboard.jsp">&larr; Volver al dashboard</a>
        </p>
    </div>

    <%
        String error = (String) request.getAttribute("error");
        if (error != null) {
    %>
    <div class="alert alert-danger"><%= error %></div>
    <%
        }
    %>

    <table>
        <tr>
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
                <td data-label="Fecha">${cita.fecha}</td>
                <td data-label="Hora">${cita.hora}</td>
                <td data-label="Modalidad">${cita.modalidad}</td>
                <td data-label="Lugar">${cita.lugar}</td>
                <td data-label="Motivo">${cita.motivo}</td>
                <td data-label="Estado"><span class="estado-badge estado-${cita.estado}">${cita.estado}</span></td>
                <td class="actions" data-label="Acciones">
                    <c:if test="${cita.estado == 'PENDIENTE' || cita.estado == 'CONFIRMADA'}">
                        <a href="${pageContext.request.contextPath}/citas?accion=cancelar&id=${cita.idCita}"
                           onclick="return confirm('¿Cancelar esta cita?')">Cancelar</a>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty citas}">
            <tr><td colspan="7" class="empty-msg">Todavía no tienes citas agendadas.</td></tr>
        </c:if>
    </table>
</div>

</body>
</html>

