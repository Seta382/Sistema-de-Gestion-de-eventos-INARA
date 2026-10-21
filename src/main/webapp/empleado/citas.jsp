<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestión de Citas - INARA</title>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&family=Playfair+Display:ital,wght@0,600;0,700&display=swap" rel="stylesheet">
    <style>
        :root {
            --bg-primary: #F8F4F1; --rose-dark: #A66A6A; --rose-deep: #743E3E;
            --gold: #C6A15B; --text-primary: #3D3333; --text-secondary: #706464;
            --white: #FFFFFF; --border-soft: #EFE8E2;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: 'Montserrat', sans-serif; background-color: var(--bg-primary); padding: 40px 24px; }
        .wrapper { max-width: 1100px; margin: 0 auto; background: var(--white); border-radius: 24px;
                   padding: 36px 40px; border: 1px solid var(--border-soft); }
        h1 { font-family: 'Playfair Display', Georgia, serif; font-size: 1.6rem; color: var(--text-primary); }
        .subtitle { font-size: 0.85rem; color: var(--text-secondary); margin: 4px 0 22px; }

        .search-bar { display: flex; gap: 10px; align-items: center; margin-bottom: 16px; flex-wrap: wrap; }
        .search-bar input[type="search"] {
            flex: 1; min-width: 260px; padding: 12px 16px; border: 1.5px solid var(--border-soft);
            border-radius: 12px; background: #FAF8F6; font-size: 0.9rem; font-family: 'Montserrat', sans-serif;
            color: var(--text-primary); outline: none;
        }
        .search-bar input[type="search"]:focus { border-color: var(--rose-dark); background: var(--white);
            box-shadow: 0 0 0 3px rgba(216, 167, 167, 0.25); }
        .search-bar button {
            padding: 12px 22px; border: none; border-radius: 12px; cursor: pointer;
            background: linear-gradient(135deg, var(--gold) 0%, var(--rose-dark) 100%);
            color: var(--white); font-size: 0.82rem; font-weight: 700; letter-spacing: 0.5px; text-transform: uppercase;
        }
        .search-bar .clear { font-size: 0.82rem; color: var(--rose-dark); text-decoration: none; font-weight: 600; }
        .results-info { font-size: 0.82rem; color: var(--text-secondary); margin-bottom: 14px; }

        .filters { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 18px; }
        .filters a { padding: 7px 14px; border-radius: 20px; font-size: 0.78rem; font-weight: 600;
                     text-decoration: none; color: var(--text-secondary); background: #FAF8F6;
                     border: 1px solid var(--border-soft); }
        .filters a.active { background: var(--rose-dark); color: var(--white); border-color: var(--rose-dark); }
        table { width: 100%; border-collapse: collapse; }
        th, td { text-align: left; padding: 12px 10px; border-bottom: 1px solid var(--border-soft); font-size: 0.86rem; vertical-align: middle; }
        th { text-transform: uppercase; font-size: 0.72rem; letter-spacing: 0.5px; color: var(--text-secondary); }
        .badge { padding: 4px 11px; border-radius: 14px; font-size: 0.72rem; font-weight: 700; white-space: nowrap; }
        .estado-PENDIENTE    { background: #FDF3E3; color: #8C6A29; }
        .estado-CONFIRMADA   { background: #F4F8F4; color: #2F6B4F; }
        .estado-REPROGRAMADA { background: #EFE8F5; color: #5B3E8C; }
        .estado-CANCELADA    { background: #FDF3F3; color: #923838; }
        .estado-FINALIZADA   { background: #F0F0F0; color: #555555; }
        .actions a { font-size: 0.8rem; text-decoration: none; margin-right: 12px; font-weight: 600; white-space: nowrap; }
        .a-confirmar { color: #2F6B4F; }
        .a-convertir { color: var(--rose-deep); }
        .a-cancelar  { color: #923838; }
        .empty-msg { text-align: center; padding: 30px; color: var(--text-secondary); }
        .link-back { display: inline-block; margin-top: 22px; font-size: 0.85rem; color: var(--rose-dark); text-decoration: none; }

        /* ===== Responsive: tabla -> tarjetas apiladas en celular ===== */
        @media (max-width: 720px) {
            body { padding: 16px 12px; }
            .search-bar input[type="search"] { min-width: 0; flex-basis: 100%; }
            .search-bar button, .filters a { min-height: 40px; display: inline-flex; align-items: center; }
            .wrapper { padding: 22px 16px; border-radius: 18px; }
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
            .actions a { display: inline-flex; align-items: center; justify-content: center; min-height: 40px;
                       padding: 0 14px; margin: 0; border: 1px solid #EFE8E2; border-radius: 10px; background: #fff; }
        }
    </style>
</head>
<body>

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
