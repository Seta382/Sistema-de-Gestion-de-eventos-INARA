<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Personal Administrativo - INARA</title>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&family=Playfair+Display:ital,wght@0,600;0,700&display=swap" rel="stylesheet">
    <style>
        :root {
            --bg-primary: #F8F4F1; --rose-dark: #A66A6A; --rose-deep: #743E3E;
            --gold: #C6A15B; --text-primary: #3D3333; --text-secondary: #706464;
            --white: #FFFFFF; --border-soft: #EFE8E2;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: 'Montserrat', sans-serif; background-color: var(--bg-primary); padding: 40px 24px; }
        .wrapper { max-width: 1000px; margin: 0 auto; background: var(--white); border-radius: 24px;
            padding: 36px 40px; border: 1px solid var(--border-soft); }
        .header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; flex-wrap: wrap; gap: 12px; }
        h1 { font-family: 'Playfair Display', Georgia, serif; font-size: 1.6rem; color: var(--text-primary); }
        .btn-new {
            background: linear-gradient(135deg, var(--gold) 0%, var(--rose-dark) 100%);
            color: var(--white); padding: 11px 20px; border-radius: 10px;
            text-decoration: none; font-size: 0.85rem; font-weight: 700;
            text-transform: uppercase; letter-spacing: 0.5px;
        }
        table { width: 100%; border-collapse: collapse; margin-top: 10px; }
        th, td { text-align: left; padding: 12px 10px; border-bottom: 1px solid var(--border-soft); font-size: 0.88rem; }
        th { text-transform: uppercase; font-size: 0.72rem; letter-spacing: 0.5px; color: var(--text-secondary); }
        .badge { padding: 4px 11px; border-radius: 14px; font-size: 0.72rem; font-weight: 700; }
        .badge-activo { background: #F4F8F4; color: #2F6B4F; }
        .badge-inactivo { background: #F0F0F0; color: #706464; }
        .rol-badge { padding: 3px 9px; border-radius: 8px; font-size: 0.7rem; font-weight: 600; background: #F5EDE3; color: var(--rose-deep); }
        .actions a { font-size: 0.82rem; text-decoration: none; margin-right: 12px; font-weight: 600; }
        .actions .a-editar { color: var(--rose-dark); }
        .actions .a-baja { color: #923838; }
        .actions .a-reactivar { color: #2F6B4F; }
        .empty-msg { text-align: center; padding: 30px; color: var(--text-secondary); }
        .link-back { display: inline-block; margin-top: 20px; font-size: 0.85rem; color: var(--rose-dark); text-decoration: none; }
    </style>
</head>
<body>

<div class="wrapper">
    <div class="header-row">
        <h1>Personal Administrativo</h1>
        <a href="${pageContext.request.contextPath}/empleado?accion=registro" class="btn-new">+ Nuevo personal</a>
    </div>

    <c:if test="${not empty errorGlobal}">
        <div style="background:#FDF3F3; color:#923838; padding:12px 16px; border-radius:10px; font-size:0.85rem; margin-bottom:18px;">
                ${errorGlobal}
        </div>
    </c:if>

    <table>
        <tr>
            <th>Nombre</th>
            <th>Correo</th>
            <th>DNI</th>
            <th>Cargo</th>
            <th>Área</th>
            <th>Rol</th>
            <th>Estado</th>
            <th></th>
        </tr>
        <c:forEach var="emp" items="${listaPersonal}">
            <tr>
                <td>${emp.nombreCompleto}</td>
                <td>${emp.correo}</td>
                <td>${emp.dni}</td>
                <td>${emp.cargo}</td>
                <td>${emp.area}</td>
                <td><span class="rol-badge">${emp.rol}</span></td>
                <td>
                    <c:choose>
                        <c:when test="${emp.estado == 'INACTIVO'}">
                            <span class="badge badge-inactivo">Inactivo</span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge badge-activo">Activo</span>
                        </c:otherwise>
                    </c:choose>
                </td>
                <td class="actions">
                    <a class="a-editar" href="${pageContext.request.contextPath}/empleado?accion=editar&id=${emp.idEmpleado}">Editar</a>
                    <c:choose>
                        <c:when test="${emp.estado == 'INACTIVO'}">
                            <a class="a-reactivar" href="${pageContext.request.contextPath}/empleado?accion=reactivar&idUsuario=${emp.idUsuario}"
                               onclick="return confirm('¿Reactivar a este colaborador?')">Reactivar</a>
                        </c:when>
                        <c:otherwise>
                            <a class="a-baja" href="${pageContext.request.contextPath}/empleado?accion=baja&idUsuario=${emp.idUsuario}"
                               onclick="return confirm('¿Dar de baja a este colaborador? Podrás reactivarlo después.')">Dar de baja</a>
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty listaPersonal}">
            <tr><td colspan="8" class="empty-msg">Todavía no hay personal registrado.</td></tr>
        </c:if>
    </table>

    <a href="${pageContext.request.contextPath}/empleado/dashboard.jsp" class="link-back">&larr; Volver al panel administrativo</a>
</div>

</body>
</html>
