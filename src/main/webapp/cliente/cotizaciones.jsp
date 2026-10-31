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

    // Si se abre el JSP directo (sin pasar por CotizacionClienteServlet) no hay datos cargados
    if (request.getAttribute("cotizaciones") == null) {
        response.sendRedirect(request.getContextPath() + "/mis-cotizaciones");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>INARA — Mis Cotizaciones</title>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&family=Playfair+Display:ital,wght@0,600;0,700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/cotizacion.css">
</head>
<body class="page-cotizacion">

<header class="navbar">
    <div class="logo">INARA</div>
    <a href="${pageContext.request.contextPath}/cliente/dashboard.jsp" class="btn-volver">&larr; Volver a mi portal</a>
</header>

<main class="contenedor">
    <div class="card">

        <h1>Mis cotizaciones</h1>
        <p class="subtitulo">Las propuestas que nuestro equipo preparó para tus eventos. Puedes aceptarlas o rechazarlas desde aquí.</p>

        <c:if test="${not empty exito}">
            <div class="mensaje exito"><c:out value="${exito}"/></div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="mensaje error"><c:out value="${error}"/></div>
        </c:if>

        <c:if test="${empty cotizaciones}">
            <p class="empty-msg">Todavía no tienes cotizaciones. Cuando nuestro equipo prepare una para tu evento, aparecerá aquí.</p>
        </c:if>

        <c:forEach var="co" items="${cotizaciones}">
            <div class="cot-card ${co.estado == 'PENDIENTE' ? 'cot-pendiente' : ''} ${co.estado == 'MODIFICADA' ? 'cot-inactiva' : ''}">
                <div class="cot-cabecera">
                    <div class="cot-titulo">
                        <c:out value="${empty co.nombreEvento ? 'Evento INARA' : co.nombreEvento}"/>
                        <span class="cot-fecha">Cotización #${co.idCotizacion} · <c:out value="${co.fecha}"/></span>
                    </div>
                    <span class="badge estado-${co.estado}">${co.estado == 'MODIFICADA' ? 'REEMPLAZADA' : co.estado}</span>
                </div>

                <div class="tabla-wrap">
                    <table class="lineas">
                        <tr><th>Servicio</th><th class="num">Cant.</th><th class="num">Precio unit.</th><th class="num">Subtotal</th></tr>
                        <c:forEach var="d" items="${co.detalles}">
                            <tr>
                                <td class="celda-titulo"><span class="nombre"><c:out value="${d.nombreServicio}"/></span></td>
                                <td class="num" data-label="Cantidad">${d.cantidad}</td>
                                <td class="num" data-label="Precio unit.">S/ <fmt:formatNumber value="${d.precioUnitario}" minFractionDigits="2" maxFractionDigits="2"/></td>
                                <td class="num" data-label="Subtotal">S/ <fmt:formatNumber value="${d.subtotal}" minFractionDigits="2" maxFractionDigits="2"/></td>
                            </tr>
                        </c:forEach>
                    </table>
                </div>

                <div class="cot-totales">
                    <div class="linea"><span>Subtotal</span><span>S/ <fmt:formatNumber value="${co.subtotal}" minFractionDigits="2" maxFractionDigits="2"/></span></div>
                    <c:if test="${co.descuento.signum() > 0}">
                        <div class="linea"><span>Descuento</span><span>- S/ <fmt:formatNumber value="${co.descuento}" minFractionDigits="2" maxFractionDigits="2"/></span></div>
                    </c:if>
                    <div class="linea total"><span>Total</span><span>S/ <fmt:formatNumber value="${co.total}" minFractionDigits="2" maxFractionDigits="2"/></span></div>
                </div>

                <c:if test="${not empty co.observaciones}">
                    <p class="cot-obs"><c:out value="${co.observaciones}"/></p>
                </c:if>

                <c:if test="${co.estado == 'PENDIENTE'}">
                    <div class="cot-acciones">
                        <form action="${pageContext.request.contextPath}/mis-cotizaciones" method="post"
                              onsubmit="return confirm('¿Aceptar esta cotización por S/ ${co.total}?')">
                            <input type="hidden" name="accion" value="aceptar">
                            <input type="hidden" name="id" value="${co.idCotizacion}">
                            <button type="submit" class="btn-aceptar">Aceptar cotización</button>
                        </form>
                        <form action="${pageContext.request.contextPath}/mis-cotizaciones" method="post"
                              onsubmit="return confirm('¿Rechazar esta cotización?')">
                            <input type="hidden" name="accion" value="rechazar">
                            <input type="hidden" name="id" value="${co.idCotizacion}">
                            <button type="submit" class="btn-rechazar">Rechazar</button>
                        </form>
                    </div>
                </c:if>
            </div>
        </c:forEach>

    </div>
</main>

</body>
</html>
