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

    // Si se abre el JSP directo (sin pasar por AcuerdoClienteServlet) no hay datos cargados
    if (request.getAttribute("acuerdos") == null) {
        response.sendRedirect(request.getContextPath() + "/mis-acuerdos");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>INARA — Mis Acuerdos</title>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&family=Playfair+Display:ital,wght@0,600;0,700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/acuerdo.css">
</head>
<body class="page-acuerdo">

<header class="navbar">
    <div class="logo">INARA</div>
    <a href="${pageContext.request.contextPath}/cliente/dashboard.jsp" class="btn-volver">&larr; Volver a mi portal</a>
</header>

<main class="contenedor">
    <div class="card">

        <h1>Mis acuerdos</h1>
        <p class="subtitulo">Las condiciones de servicio de tus eventos. Léelas con calma: al aceptar un acuerdo, tu evento queda confirmado.</p>

        <c:if test="${not empty exito}">
            <div class="mensaje exito"><c:out value="${exito}"/></div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="mensaje error"><c:out value="${error}"/></div>
        </c:if>

        <c:if test="${empty acuerdos}">
            <p class="empty-msg">Todavía no tienes acuerdos. Cuando aceptes una cotización, nuestro equipo preparará el acuerdo de tu evento y aparecerá aquí.</p>
        </c:if>

        <c:forEach var="ac" items="${acuerdos}">
            <div class="acuerdo-card ${ac.estado == 'PENDIENTE' ? 'acuerdo-pendiente' : ''} ${ac.estado == 'ACEPTADO' ? 'acuerdo-aceptado' : ''} ${ac.estado == 'CANCELADO' ? 'acuerdo-inactivo' : ''}">
                <div class="acuerdo-cabecera">
                    <div class="acuerdo-titulo">
                        <c:out value="${empty ac.nombreEvento ? 'Evento INARA' : ac.nombreEvento}"/>
                        <span class="acuerdo-fecha">Acuerdo #${ac.idAcuerdo} · <c:out value="${ac.fechaRegistroTxt}"/></span>
                    </div>
                    <span class="badge estado-${ac.estado}">${ac.estado == 'ACEPTADO' ? 'ACEPTADO' : (ac.estado == 'CANCELADO' ? 'ANULADO' : 'POR ACEPTAR')}</span>
                </div>

                <div class="acuerdo-texto"><c:out value="${ac.condiciones}"/></div>

                <c:if test="${not empty ac.fechaConfirmacionTxt}">
                    <p class="acuerdo-meta">Aceptaste este acuerdo el <c:out value="${ac.fechaConfirmacionTxt}"/>.</p>
                </c:if>

                <c:if test="${ac.estado == 'PENDIENTE'}">
                    <div class="acuerdo-acciones">
                        <form action="${pageContext.request.contextPath}/mis-acuerdos" method="post"
                              onsubmit="return confirm('¿Aceptar este acuerdo? Tu evento quedará confirmado.')">
                            <input type="hidden" name="accion" value="aceptar">
                            <input type="hidden" name="id" value="${ac.idAcuerdo}">
                            <button type="submit" class="btn-aceptar">Aceptar acuerdo</button>
                        </form>
                        <a class="btn-secundario" target="_blank" rel="noopener noreferrer"
                           href="https://wa.me/51992937943?text=Hola%20INARA%2C%20tengo%20una%20consulta%20sobre%20el%20acuerdo%20%23${ac.idAcuerdo}%20de%20mi%20evento.">Tengo dudas</a>
                        <span class="nota-acciones">Si quieres cambiar algo, escríbenos antes de aceptar y ajustamos las condiciones.</span>
                    </div>
                </c:if>
            </div>
        </c:forEach>

    </div>
</main>

</body>
</html>
