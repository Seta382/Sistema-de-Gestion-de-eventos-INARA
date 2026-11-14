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

    // Si se abre el JSP directo (sin pasar por NotificacionServlet) no hay datos cargados
    if (request.getAttribute("notificaciones") == null) {
        response.sendRedirect(request.getContextPath() + "/notificaciones");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>INARA — Notificaciones</title>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&family=Playfair+Display:ital,wght@0,600;0,700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/notificaciones.css">
</head>
<body class="page-notificacion">

<header class="navbar">
    <div class="logo">INARA</div>
    <a href="${pageContext.request.contextPath}${esPersonal ? '/empleado/dashboard.jsp' : '/cliente/dashboard.jsp'}" class="btn-volver">&larr; Volver ${esPersonal ? 'al Centro de Control' : 'a mi portal'}</a>
</header>

<main class="contenedor">
    <div class="card">

        <h1>Notificaciones</h1>
        <p class="subtitulo">
            <c:choose>
                <c:when test="${esPersonal}">Avisos de lo que hacen tus clientes: respuestas a cotizaciones y aceptación de acuerdos.</c:when>
                <c:otherwise>Novedades de tu evento: cotizaciones, acuerdos, pagos y avances de la preparación.</c:otherwise>
            </c:choose>
        </p>

        <div class="barra-notif">
            <div class="filters">
                <a href="${pageContext.request.contextPath}/notificaciones?filtro=TODAS" class="${filtro == 'TODAS' ? 'active' : ''}">Todas</a>
                <a href="${pageContext.request.contextPath}/notificaciones?filtro=SIN_LEER" class="${filtro == 'SIN_LEER' ? 'active' : ''}">Sin leer (${totalNoLeidas})</a>
            </div>
            <c:if test="${totalNoLeidas > 0}">
                <form action="${pageContext.request.contextPath}/notificaciones" method="post">
                    <input type="hidden" name="accion" value="todas">
                    <input type="hidden" name="filtro" value="${filtro}">
                    <button type="submit" class="btn-link btn-leer-todas">Marcar todas como leídas</button>
                </form>
            </c:if>
        </div>

        <c:forEach var="n" items="${notificaciones}">
            <div class="notif ${n.leida ? '' : 'sin-leer'}">
                <div class="notif-icono tipo-${n.tipo}" aria-hidden="true">
                    <c:choose>
                        <c:when test="${n.tipo == 'COTIZACION'}">&#129534;</c:when>
                        <c:when test="${n.tipo == 'ACUERDO'}">&#128221;</c:when>
                        <c:when test="${n.tipo == 'PAGO'}">&#128179;</c:when>
                        <c:when test="${n.tipo == 'SEGUIMIENTO'}">&#128203;</c:when>
                        <c:otherwise>&#128276;</c:otherwise>
                    </c:choose>
                </div>
                <div class="notif-cuerpo">
                    <div class="notif-mensaje"><c:out value="${n.mensaje}"/></div>
                    <div class="notif-fecha"><c:out value="${n.fechaTxt}"/><c:if test="${not n.leida}"> &middot; <span class="punto-nueva">Nueva</span></c:if></div>
                </div>
                <div class="notif-acciones">
                    <form action="${pageContext.request.contextPath}/notificaciones" method="post">
                        <input type="hidden" name="accion" value="abrir">
                        <input type="hidden" name="id" value="${n.idNotificacion}">
                        <button type="submit" class="btn-ver">Ver</button>
                    </form>
                    <c:if test="${not n.leida}">
                        <form action="${pageContext.request.contextPath}/notificaciones" method="post">
                            <input type="hidden" name="accion" value="leer">
                            <input type="hidden" name="id" value="${n.idNotificacion}">
                            <input type="hidden" name="filtro" value="${filtro}">
                            <button type="submit" class="btn-link btn-reactivar">Marcar leída</button>
                        </form>
                    </c:if>
                </div>
            </div>
        </c:forEach>

        <c:if test="${empty notificaciones}">
            <p class="empty-msg">
                <c:choose>
                    <c:when test="${filtro == 'SIN_LEER'}">No tienes notificaciones sin leer. ¡Estás al día!</c:when>
                    <c:otherwise>Todavía no tienes notificaciones. Aquí aparecerán las novedades de tu evento.</c:otherwise>
                </c:choose>
            </p>
        </c:if>

    </div>
</main>

</body>
</html>
