<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mis Citas - INARA</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css">
    <style>
        body { display: block; padding: 40px 24px; }
        .citas-wrapper { max-width: 900px; margin: 0 auto; background: var(--white, #fff);
            border-radius: 24px; padding: 36px 40px; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { text-align: left; padding: 10px 12px; border-bottom: 1px solid #EFE8E2; font-size: 0.9rem; }
        th { text-transform: uppercase; font-size: 0.75rem; letter-spacing: 0.5px; color: #706464; }
        .estado-badge { padding: 4px 10px; border-radius: 12px; font-size: 0.78rem; font-weight: 600; }
        .estado-PENDIENTE { background: #FDF3E3; color: #8C6A29; }
        .estado-CONFIRMADA { background: #F4F8F4; color: #2F6B4F; }
        .estado-REPROGRAMADA { background: #EFE8F5; color: #5B3E8C; }
        .estado-CANCELADA { background: #FDF3F3; color: #923838; }
        .estado-FINALIZADA { background: #F0F0F0; color: #555; }
    </style>
</head>
<body>

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
                <td>${cita.fecha}</td>
                <td>${cita.hora}</td>
                <td>${cita.modalidad}</td>
                <td>${cita.lugar}</td>
                <td>${cita.motivo}</td>
                <td><span class="estado-badge estado-${cita.estado}">${cita.estado}</span></td>
                <td>
                    <c:if test="${cita.estado == 'PENDIENTE' || cita.estado == 'CONFIRMADA'}">
                        <a href="${pageContext.request.contextPath}/citas?accion=cancelar&id=${cita.idCita}"
                           onclick="return confirm('¿Cancelar esta cita?')">Cancelar</a>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty citas}">
            <tr><td colspan="7">Todavía no tienes citas agendadas.</td></tr>
        </c:if>
    </table>
</div>

</body>
</html>

