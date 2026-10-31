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

    String rol = usuario.getRol() != null ? usuario.getRol().toUpperCase() : "";
    if (!Constantes.ROL_ADMIN.equals(rol) && !Constantes.ROL_EMPLEADO.equals(rol)) {
        response.sendRedirect(request.getContextPath() + "/cliente/dashboard.jsp");
        return;
    }

    // Si se abre el JSP directo (sin pasar por PagoServlet) no hay datos cargados
    if (request.getAttribute("panel") == null) {
        response.sendRedirect(request.getContextPath() + "/pago");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>INARA — Pagos del Evento</title>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&family=Playfair+Display:ital,wght@0,600;0,700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/pago.css">
</head>
<body class="page-pago">

<header class="navbar">
    <div class="logo">INARA</div>
    <a href="${pageContext.request.contextPath}/evento?accion=cronograma&amp;filtro=TODOS" class="btn-volver">&larr; Volver al cronograma</a>
</header>

<main class="contenedor">
    <div class="card">

        <h1>Pagos del evento</h1>
        <p class="subtitulo">Registra los pagos del cliente. El saldo se calcula con el total de la cotización aceptada; solo los pagos completados lo reducen.</p>

        <c:if test="${not empty exito}">
            <div class="mensaje exito"><c:out value="${exito}"/></div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="mensaje error"><c:out value="${error}"/></div>
        </c:if>

        <c:set var="ev" value="${panel.evento}"/>
        <c:set var="res" value="${panel.resumen}"/>
        <div class="evento-ficha">
            <div><span class="dato-label">Evento</span><span class="dato-valor"><c:out value="${empty ev.nombre ? 'Evento INARA' : ev.nombre}"/></span></div>
            <div><span class="dato-label">Cliente titular</span><span class="dato-valor"><c:out value="${ev.nombreCliente}"/></span></div>
            <div><span class="dato-label">Fecha</span><span class="dato-valor"><c:out value="${ev.fechaEvento}"/></span></div>
            <div><span class="dato-label">Etapa</span><span class="dato-valor"><span class="badge badge-activo"><c:out value="${ev.estado}"/></span></span></div>
        </div>

        <c:if test="${not empty panel.motivoBloqueo}">
            <div class="aviso"><c:out value="${panel.motivoBloqueo}"/></div>
        </c:if>
        <c:if test="${empty panel.motivoBloqueo and not res.tieneTotal}">
            <div class="aviso">
                Este evento no tiene una cotización aceptada.
                <a href="${pageContext.request.contextPath}/cotizacion?idEvento=${ev.idEvento}">Ir a las cotizaciones del evento</a>.
            </div>
        </c:if>

        <c:if test="${res.tieneTotal}">
            <div class="resumen-pagos">
                <div class="dato"><span class="dato-label">Total acordado</span><span class="dato-monto">S/ <fmt:formatNumber value="${res.total}" minFractionDigits="2" maxFractionDigits="2"/></span></div>
                <div class="dato"><span class="dato-label">Pagado</span><span class="dato-monto">S/ <fmt:formatNumber value="${res.pagado}" minFractionDigits="2" maxFractionDigits="2"/></span></div>
                <div class="dato"><span class="dato-label">Por confirmar</span><span class="dato-monto">S/ <fmt:formatNumber value="${res.porConfirmar}" minFractionDigits="2" maxFractionDigits="2"/></span></div>
                <div class="dato saldo ${res.pagadoCompleto ? 'al-dia' : ''}"><span class="dato-label">Saldo pendiente</span><span class="dato-monto">S/ <fmt:formatNumber value="${res.saldo}" minFractionDigits="2" maxFractionDigits="2"/></span></div>
            </div>
            <div class="barra"><span style="width: ${res.porcentaje}%"></span></div>
            <p class="barra-txt">${res.porcentaje}% pagado<c:if test="${res.pagadoCompleto}"> · ¡Evento pagado por completo!</c:if></p>
        </c:if>

        <c:if test="${panel.puedeRegistrar}">
            <details class="nuevo" ${not empty form ? 'open' : ''}>
                <summary>+ Registrar pago</summary>
                <form class="form-nuevo" action="${pageContext.request.contextPath}/pago" method="post">
                    <input type="hidden" name="accion" value="registrar">
                    <input type="hidden" name="idEvento" value="${ev.idEvento}">
                    <div class="fila">
                        <div class="campo">
                            <label for="monto">Monto (S/) *</label>
                            <input type="text" id="monto" name="monto" inputmode="decimal" required maxlength="14"
                                   placeholder="Ej. 500.00" value="<c:out value='${form.monto}'/>">
                            <small>Saldo pendiente: S/ <fmt:formatNumber value="${res.saldo}" minFractionDigits="2" maxFractionDigits="2"/></small>
                        </div>
                        <div class="campo">
                            <label for="metodo">Método de pago *</label>
                            <select id="metodo" name="metodo" required>
                                <c:forEach var="m" items="${metodos}">
                                    <option value="${m}" ${form.metodo == m ? 'selected' : ''}>${m}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>
                    <div class="fila">
                        <div class="campo">
                            <label for="estado">Estado</label>
                            <select id="estado" name="estado">
                                <option value="COMPLETADO" ${form.estado != 'PENDIENTE' ? 'selected' : ''}>Completado (ya recibido)</option>
                                <option value="PENDIENTE" ${form.estado == 'PENDIENTE' ? 'selected' : ''}>Pendiente de verificar</option>
                            </select>
                        </div>
                        <div class="campo">
                            <label for="comprobante">Enlace del comprobante</label>
                            <input type="url" id="comprobante" name="comprobante" maxlength="255"
                                   placeholder="https://..." value="<c:out value='${form.comprobante}'/>">
                        </div>
                    </div>
                    <button type="submit" class="btn-primary">Registrar pago</button>
                </form>
            </details>
        </c:if>

        <h2 class="seccion">Pagos de este evento</h2>

        <div class="tabla-wrap">
            <table>
                <tr>
                    <th>Pago</th>
                    <th>Método</th>
                    <th class="num">Monto</th>
                    <th>Estado</th>
                    <th></th>
                </tr>
                <c:forEach var="p" items="${panel.pagos}">
                    <tr class="${p.estado == 'RECHAZADO' ? 'fila-inactiva' : ''}">
                        <td class="celda-titulo">
                            <span class="nombre">Pago #${p.idPago}</span>
                            <span class="desc"><c:out value="${p.fechaPagoTxt}"/></span>
                        </td>
                        <td data-label="Método"><c:out value="${p.metodoPago}"/>
                            <c:if test="${not empty p.comprobanteUrl}"> · <a class="comprobante" href="<c:out value='${p.comprobanteUrl}'/>" target="_blank" rel="noopener noreferrer">Comprobante</a></c:if>
                        </td>
                        <td data-label="Monto" class="num precio">S/ <fmt:formatNumber value="${p.monto}" minFractionDigits="2" maxFractionDigits="2"/></td>
                        <td data-label="Estado"><span class="badge estado-${p.estado}">${p.estado}</span></td>
                        <td class="actions">
                            <c:if test="${p.estado == 'PENDIENTE'}">
                                <form action="${pageContext.request.contextPath}/pago" method="post"
                                      onsubmit="return confirm('¿Confirmar que recibiste el pago #${p.idPago}?')">
                                    <input type="hidden" name="accion" value="confirmar">
                                    <input type="hidden" name="id" value="${p.idPago}">
                                    <input type="hidden" name="idEvento" value="${ev.idEvento}">
                                    <button type="submit" class="btn-aceptar">Confirmar</button>
                                </form>
                            </c:if>
                            <c:if test="${p.estado != 'RECHAZADO'}">
                                <form action="${pageContext.request.contextPath}/pago" method="post"
                                      onsubmit="return confirm('¿${p.estado == 'COMPLETADO' ? 'Anular' : 'Rechazar'} el pago #${p.idPago}? ${p.estado == 'COMPLETADO' ? 'El saldo del evento volverá a subir.' : ''}')">
                                    <input type="hidden" name="accion" value="rechazar">
                                    <input type="hidden" name="id" value="${p.idPago}">
                                    <input type="hidden" name="idEvento" value="${ev.idEvento}">
                                    <button type="submit" class="btn-rechazar">${p.estado == 'COMPLETADO' ? 'Anular' : 'Rechazar'}</button>
                                </form>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty panel.pagos}">
                    <tr><td colspan="5" class="empty-msg">Todavía no hay pagos registrados para este evento.</td></tr>
                </c:if>
            </table>
        </div>

    </div>
</main>

</body>
</html>
