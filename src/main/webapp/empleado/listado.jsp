<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Personal Administrativo - INARA</title>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&family=Playfair+Display:ital,wght@0,600;0,700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/empleado.css">
</head>
<body class="page-listado">

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
                    <td data-label="Nombre">${emp.nombreCompleto}</td>
                    <td data-label="Correo">${emp.correo}</td>
                    <td data-label="DNI">${emp.dni}</td>
                    <td data-label="Cargo">${emp.cargo}</td>
                    <td data-label="Área">${emp.area}</td>
                    <td data-label="Rol"><span class="rol-badge">${emp.rol}</span></td>
                    <td data-label="Estado">
                        <c:choose>
                            <c:when test="${emp.estado == 'INACTIVO'}">
                                <span class="badge badge-inactivo">Inactivo</span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge badge-activo">Activo</span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td class="actions" data-label="Acciones">
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
