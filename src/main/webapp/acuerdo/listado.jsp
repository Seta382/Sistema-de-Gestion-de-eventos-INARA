<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="pe.edu.eventos.dto.UsuarioDTO" %>
<%@ page import="pe.edu.eventos.util.Constantes" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
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

    // Si se abre el JSP directo (sin pasar por AcuerdoServlet) no hay datos cargados
    if (request.getAttribute("acuerdos") == null) {
        response.sendRedirect(request.getContextPath() + "/acuerdo");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>INARA — Acuerdos</title>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&family=Playfair+Display:ital,wght@0,600;0,700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/acuerdo.css">
</head>
<body class="page-acuerdo">

<header class="navbar">
    <div class="logo">INARA</div>
    <a href="${pageContext.request.contextPath}/empleado/dashboard.jsp" class="btn-volver">&larr; Volver al Centro de Control</a>
</header>

<main class="contenedor">
    <div class="card">

        <h1>Acuerdos</h1>
        <p class="subtitulo">Acuerdos enviados a los clientes. Para crear uno nuevo, el cliente debe haber aceptado una cotización: entra al evento desde el cronograma.</p>

        <c:if test="${not empty exito}">
            <div class="mensaje exito"><c:out value="${exito}"/></div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="mensaje error"><c:out value="${error}"/></div>
        </c:if>

        <form class="search-bar" action="${pageContext.request.contextPath}/acuerdo" method="get">
            <input type="hidden" name="filtro" value="${filtro}">
            <input type="search" name="q" value="<c:out value='${busqueda}'/>"
                   placeholder="Buscar por evento o cliente" maxlength="100">
            <button type="submit" class="btn-primary">Buscar</button>
            <c:if test="${not empty busqueda}">
                <a class="clear" href="${pageContext.request.contextPath}/acuerdo?filtro=${filtro}">Limpiar</a>
            </c:if>
        </form>

        <div class="filters">
            <a href="${pageContext.request.contextPath}/acuerdo?filtro=PENDIENTE${qParamHtml}" class="${filtro == 'PENDIENTE' ? 'active' : ''}">Pendientes (${totalPendientes})</a>
            <a href="${pageContext.request.contextPath}/acuerdo?filtro=ACEPTADO${qParamHtml}" class="${filtro == 'ACEPTADO' ? 'active' : ''}">Aceptados</a>
            <a href="${pageContext.request.contextPath}/acuerdo?filtro=CANCELADO${qParamHtml}" class="${filtro == 'CANCELADO' ? 'active' : ''}">Cancelados</a>
            <a href="${pageContext.request.contextPath}/acuerdo?filtro=TODOS${qParamHtml}" class="${filtro == 'TODOS' ? 'active' : ''}">Todos</a>
        </div>

        <c:if test="${not empty busqueda}">
            <p class="results-info">
                ${acuerdos.size()} resultado(s) para "<strong><c:out value="${busqueda}"/></strong>"
            </p>
        </c:if>

        <div class="tabla-wrap">
            <table>
                <tr>
                    <th>Acuerdo</th>
                    <th>Cliente</th>
                    <th>Fecha del evento</th>
                    <th>Estado</th>
                    <th></th>
                </tr>
                <c:forEach var="ac" items="${acuerdos}">
                    <tr class="${ac.estado == 'CANCELADO' ? 'fila-inactiva' : ''}">
                        <td class="celda-titulo">
                            <span class="nombre">#${ac.idAcuerdo} · <c:out value="${ac.nombreEvento}"/></span>
                            <span class="desc"><c:out value="${ac.fechaRegistroTxt}"/></span>
                        </td>
                        <td data-label="Cliente"><c:out value="${ac.nombreCliente}"/></td>
                        <td data-label="Fecha del evento"><c:out value="${ac.fechaEvento}"/></td>
                        <td data-label="Estado"><span class="badge estado-${ac.estado}">${ac.estado}</span></td>
                        <td class="actions">
                            <a class="btn-editar" href="${pageContext.request.contextPath}/acuerdo?idEvento=${ac.idEvento}">Abrir</a>
                            <c:if test="${ac.estado == 'PENDIENTE'}">
                                <form action="${pageContext.request.contextPath}/acuerdo" method="post"
                                      onsubmit="return confirm('¿Registrar que el cliente ACEPTÓ el acuerdo #${ac.idAcuerdo}? El evento pasará a CONFIRMADO.')">
                                    <input type="hidden" name="accion" value="aceptar">
                                    <input type="hidden" name="id" value="${ac.idAcuerdo}">
                                    <input type="hidden" name="filtro" value="${filtro}">
                                    <input type="hidden" name="q" value="<c:out value='${busqueda}'/>">
                                    <button type="submit" class="btn-link btn-reactivar">Marcar aceptado</button>
                                </form>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty acuerdos}">
                    <tr>
                        <td colspan="5" class="empty-msg">
                            <c:choose>
                                <c:when test="${not empty busqueda}">No se encontraron acuerdos que coincidan con tu búsqueda.</c:when>
                                <c:otherwise>No hay acuerdos para mostrar con este filtro.</c:otherwise>
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
