<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="pe.edu.eventos.dto.UsuarioDTO" %>
<%@ page import="pe.edu.eventos.util.Constantes" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%
    UsuarioDTO usuario = (UsuarioDTO) session.getAttribute(Constantes.SESION_USUARIO);
    if (usuario == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }

    // Si se abre el JSP directo (sin pasar por MiEventoServlet) no hay datos cargados.
    // Un cliente sin eventos llega con "mi" = null pero con "cargado" definido.
    if (request.getAttribute("cargado") == null) {
        response.sendRedirect(request.getContextPath() + "/mi-evento");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>INARA — Mi Evento</title>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&family=Playfair+Display:ital,wght@0,600;0,700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/mi-evento.css">
</head>
<body class="page-mi-evento">

<header class="navbar">
    <div class="logo">INARA</div>
    <a href="${pageContext.request.contextPath}/cliente/dashboard.jsp" class="btn-volver">&larr; Volver a mi portal</a>
</header>

<main class="contenedor">
    <div class="card">

        <c:if test="${empty mi}">
            <h1>Mi evento</h1>
            <p class="vacio">Todavía no tienes un evento registrado. Agenda una cita con nuestro equipo y aquí podrás seguir cada paso de tu celebración.</p>
        </c:if>

        <c:if test="${not empty mi}">
            <c:set var="ev" value="${mi.evento}"/>
            <c:set var="res" value="${mi.resumen}"/>

            <c:if test="${mi.eventos.size() > 1}">
                <div class="selector">
                    <c:forEach var="e" items="${mi.eventos}">
                        <a href="${pageContext.request.contextPath}/mi-evento?id=${e.idEvento}" class="${e.idEvento == ev.idEvento ? 'active' : ''}"><c:out value="${empty e.nombre ? 'Evento INARA' : e.nombre}"/></a>
                    </c:forEach>
                </div>
            </c:if>

            <h1 class="evento-titulo"><c:out value="${empty ev.nombre ? 'Mi evento' : ev.nombre}"/></h1>
            <p class="evento-sub"><c:out value="${ev.tipoCelebracion}"/><c:if test="${not empty ev.tipoCelebracion and not empty ev.fechaEvento}"> · </c:if><c:out value="${ev.fechaEvento}"/></p>

            <c:if test="${not mi.cancelado}">
                <ol class="etapas">
                    <c:forEach var="et" items="${mi.etapas}">
                        <li class="${et.estado == 'HECHO' ? 'hecho' : ''} ${et.estado == 'ACTUAL' ? 'actual' : ''}"><c:out value="${et.nombre}"/></li>
                    </c:forEach>
                </ol>
            </c:if>

            <div class="proximo ${mi.cancelado ? 'cancelado' : ''}">
                <p><span class="proximo-label">${mi.cancelado ? 'Estado' : 'Qué sigue'}</span><c:out value="${mi.proximoPaso}"/></p>
                <c:if test="${mi.tieneBoton}">
                    <a class="btn-ir" href="${pageContext.request.contextPath}${mi.proximoPasoUrl}"><c:out value="${mi.proximoPasoBoton}"/></a>
                </c:if>
            </div>

            <div class="evento-ficha">
                <div><span class="dato-label">Fecha</span><span class="dato-valor"><c:out value="${ev.fechaEvento}"/></span></div>
                <div><span class="dato-label">Hora</span><span class="dato-valor"><c:out value="${empty ev.horaEvento ? 'Por definir' : ev.horaEvento}"/></span></div>
                <div><span class="dato-label">Lugar</span><span class="dato-valor"><c:out value="${empty ev.lugar ? 'Por definir' : ev.lugar}"/></span></div>
                <div><span class="dato-label">Invitados</span><span class="dato-valor"><c:out value="${ev.numInvitados}"/></span></div>
                <c:if test="${not empty ev.nombreCoordinador}">
                    <div><span class="dato-label">Tu coordinador/a</span><span class="dato-valor"><c:out value="${ev.nombreCoordinador}"/></span></div>
                </c:if>
            </div>

            <%-- Pagos: solo cuando ya hay una cotización aceptada --%>
            <c:if test="${res.tieneTotal and not mi.cancelado}">
                <h2 class="seccion">Pagos</h2>
                <div class="resumen-pagos">
                    <div class="dato"><span class="dato-label">Total acordado</span><span class="dato-monto">S/ <fmt:formatNumber value="${res.total}" minFractionDigits="2" maxFractionDigits="2"/></span></div>
                    <div class="dato"><span class="dato-label">Pagado</span><span class="dato-monto">S/ <fmt:formatNumber value="${res.pagado}" minFractionDigits="2" maxFractionDigits="2"/></span></div>
                    <div class="dato ${res.pagadoCompleto ? 'al-dia' : ''}"><span class="dato-label">Saldo</span><span class="dato-monto">S/ <fmt:formatNumber value="${res.saldo}" minFractionDigits="2" maxFractionDigits="2"/></span></div>
                </div>
                <div class="barra"><span style="width: ${res.porcentaje}%"></span></div>
                <p class="barra-txt">${res.porcentaje}% pagado<c:if test="${res.pagadoCompleto}"> · ¡Tu evento está pagado por completo!</c:if></p>

                <c:if test="${not empty mi.pagos}">
                    <div class="tabla-wrap">
                        <table>
                            <tr>
                                <th>Fecha</th>
                                <th>Método</th>
                                <th class="num">Monto</th>
                                <th>Estado</th>
                            </tr>
                            <c:forEach var="p" items="${mi.pagos}">
                                <tr>
                                    <td class="celda-titulo"><span class="nombre"><c:out value="${p.fechaPagoTxt}"/></span></td>
                                    <td data-label="Método"><c:out value="${p.metodoPago}"/></td>
                                    <td data-label="Monto" class="num">S/ <fmt:formatNumber value="${p.monto}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                    <td data-label="Estado"><span class="badge estado-${p.estado}">${p.estado == 'PENDIENTE' ? 'EN REVISIÓN' : 'RECIBIDO'}</span></td>
                                </tr>
                            </c:forEach>
                        </table>
                    </div>
                </c:if>
            </c:if>

            <%-- Avances --%>
            <c:if test="${not empty mi.seguimientos}">
                <h2 class="seccion">Avances de tu evento</h2>
                <div class="linea">
                    <c:forEach var="av" items="${mi.seguimientos}">
                        <div class="avance">
                            <div class="avance-titulo"><c:out value="${av.titulo}"/></div>
                            <div class="avance-fecha"><c:out value="${av.fechaRegistroTxt}"/></div>
                            <c:if test="${not empty av.descripcion}">
                                <div class="avance-detalle"><c:out value="${av.descripcion}"/></div>
                            </c:if>
                        </div>
                    </c:forEach>
                </div>
            </c:if>

            <div class="contacto">
                <p>¿Tienes dudas o quieres cambiar algo? Escríbenos y lo coordinamos.</p>
                <a class="btn-wsp" href="https://wa.me/51992937943?text=Hola%20INARA%2C%20tengo%20una%20consulta%20sobre%20mi%20evento" target="_blank" rel="noopener noreferrer">Escribir por WhatsApp</a>
            </div>
        </c:if>

    </div>
</main>

</body>
</html>
