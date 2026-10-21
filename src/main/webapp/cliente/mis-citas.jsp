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
        .acciones a { font-weight: 600; text-decoration: none; }

        /* ===== Responsive: tabla -> tarjetas apiladas en celular ===== */
        @media (max-width: 720px) {
            body { padding: 16px 12px; }
            .citas-wrapper { padding: 22px 16px; border-radius: 18px; }
            table, tbody { display: block; width: 100%; }
            table tr:first-child { display: none; }
            table tr { display: block; background: #FAF8F6; border: 1px solid #EFE8E2;
                       border-radius: 14px; padding: 10px 14px; margin-bottom: 12px; }
            table td { display: flex; justify-content: space-between; align-items: center;
                       gap: 14px; text-align: right; border: none; padding: 6px 0; word-break: break-word; }
            table td[data-label]::before { content: attr(data-label); flex-shrink: 0; text-align: left;
                       font-size: 0.7rem; font-weight: 600; text-transform: uppercase;
                       letter-spacing: 0.5px; color: #706464; }
            table td:not([data-label]) { display: block; text-align: center; }
            table td.actions { flex-wrap: wrap; justify-content: flex-start; gap: 8px;
                       border-top: 1px dashed #EFE8E2; margin-top: 6px; padding-top: 10px; }
            table td.actions::before { display: none; }
            table td.actions:not(:has(a)) { display: none; }
            table td.actions a { display: inline-flex; align-items: center; justify-content: center; min-height: 40px;
                       padding: 0 14px; margin: 0; border: 1px solid #EFE8E2; border-radius: 10px; background: #fff; }
        }
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

