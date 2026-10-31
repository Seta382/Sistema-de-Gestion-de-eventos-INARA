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

    // Si se abre el JSP directo (sin pasar por CotizacionServlet) no hay datos cargados
    if (request.getAttribute("panel") == null) {
        response.sendRedirect(request.getContextPath() + "/cotizacion");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>INARA — Cotización del Evento</title>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&family=Playfair+Display:ital,wght@0,600;0,700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/cotizacion.css">
</head>
<body class="page-cotizacion">

<header class="navbar">
    <div class="logo">INARA</div>
    <a href="${pageContext.request.contextPath}/evento?accion=cronograma&amp;filtro=TODOS" class="btn-volver">&larr; Volver al cronograma</a>
</header>

<main class="contenedor">
    <div class="card">

        <h1>Cotización del evento</h1>
        <p class="subtitulo">Arma la propuesta con los servicios del catálogo. Cada cotización guarda los precios vigentes de hoy.</p>

        <c:if test="${not empty exito}">
            <div class="mensaje exito"><c:out value="${exito}"/></div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="mensaje error"><c:out value="${error}"/></div>
        </c:if>

        <c:set var="ev" value="${panel.evento}"/>
        <div class="evento-ficha">
            <div><span class="dato-label">Evento</span><span class="dato-valor"><c:out value="${empty ev.nombre ? 'Evento INARA' : ev.nombre}"/></span></div>
            <div><span class="dato-label">Cliente titular</span><span class="dato-valor"><c:out value="${ev.nombreCliente}"/></span></div>
            <div><span class="dato-label">Tipo</span><span class="dato-valor"><c:out value="${ev.tipoCelebracion}"/></span></div>
            <div><span class="dato-label">Fecha</span><span class="dato-valor"><c:out value="${ev.fechaEvento}"/></span></div>
            <div><span class="dato-label">Invitados</span><span class="dato-valor"><c:out value="${ev.numInvitados}"/></span></div>
            <div><span class="dato-label">Etapa</span><span class="dato-valor"><span class="badge badge-activo"><c:out value="${ev.estado}"/></span></span></div>
        </div>

        <c:if test="${not panel.puedeCotizar}">
            <div class="aviso"><c:out value="${panel.motivoBloqueo}"/>
                <c:if test="${panel.motivoBloqueo.contains('aceptada')}">
                    <a href="${pageContext.request.contextPath}/acuerdo?idEvento=${ev.idEvento}">Ir al acuerdo</a>.
                </c:if>
            </div>
        </c:if>

        <c:if test="${panel.puedeCotizar}">
            <details class="nuevo" <c:if test="${abrirForm == 'cotizacion' or empty panel.cotizaciones}">open</c:if>>
                <summary>+ Nueva cotización</summary>
                <form class="form-nuevo" id="form-cotizacion" action="${pageContext.request.contextPath}/cotizacion" method="post">
                    <input type="hidden" name="accion" value="crear">
                    <input type="hidden" name="idEvento" value="${ev.idEvento}">

                    <c:choose>
                        <c:when test="${empty panel.servicios}">
                            <div class="aviso">No hay servicios activos en el catálogo.
                                <a href="${pageContext.request.contextPath}/servicio">Agrégalos en Servicios y Precios</a>.</div>
                        </c:when>
                        <c:otherwise>
                            <p class="results-info">Indica la cantidad de cada servicio que se incluirá (déjalo vacío o en 0 para no incluirlo).</p>
                            <div class="servicios-lista">
                                <div class="srv-fila srv-cabecera">
                                    <span>Servicio</span><span class="srv-precio">Precio</span><span>Cantidad</span><span class="srv-subtotal">Subtotal</span>
                                </div>
                                <c:forEach var="s" items="${panel.servicios}">
                                    <c:set var="clave" value="cantidad_${s.idServicio}"/>
                                    <div class="srv-fila" data-precio="${s.precio}">
                                        <div class="srv-info srv-nombre">
                                            <c:out value="${s.nombre}"/>
                                            <c:if test="${not empty s.descripcion}"><span class="srv-desc"><c:out value="${s.descripcion}"/></span></c:if>
                                        </div>
                                        <div class="srv-precio"><span class="srv-etiqueta">Precio</span>S/ <fmt:formatNumber value="${s.precio}" minFractionDigits="2" maxFractionDigits="2"/></div>
                                        <div>
                                            <label class="srv-etiqueta" for="cant-${s.idServicio}">Cantidad</label>
                                            <input type="number" id="cant-${s.idServicio}" name="${clave}" min="0" max="999" step="1"
                                                   placeholder="0" value="<c:out value='${form[clave]}'/>">
                                        </div>
                                        <div class="srv-subtotal"><span class="srv-etiqueta">Subtotal</span><span class="js-linea">S/ 0.00</span></div>
                                    </div>
                                </c:forEach>
                            </div>

                            <div class="resumen">
                                <div class="linea"><span>Subtotal</span><strong id="js-subtotal">S/ 0.00</strong></div>
                                <div class="linea">
                                    <label for="descuento">Descuento (S/)</label>
                                    <input type="number" id="descuento" name="descuento" min="0" step="0.01" placeholder="0.00"
                                           value="<c:out value='${form.descuento}'/>">
                                </div>
                                <div class="linea total"><span>Total</span><span id="js-total">S/ 0.00</span></div>
                            </div>

                            <div class="campo">
                                <label for="observaciones">Observaciones</label>
                                <textarea id="observaciones" name="observaciones" maxlength="1000"
                                          placeholder="Condiciones, forma de pago, vigencia de la propuesta..."><c:out value="${form.observaciones}"/></textarea>
                            </div>

                            <button type="submit" class="btn-primary">Crear cotización</button>
                        </c:otherwise>
                    </c:choose>
                </form>
            </details>
        </c:if>

        <h2 class="seccion">Cotizaciones de este evento</h2>

        <c:if test="${empty panel.cotizaciones}">
            <p class="empty-msg">Todavía no hay cotizaciones para este evento.</p>
        </c:if>

        <c:forEach var="co" items="${panel.cotizaciones}">
            <div class="cot-card ${co.estado == 'PENDIENTE' ? 'cot-pendiente' : ''} ${co.estado == 'MODIFICADA' ? 'cot-inactiva' : ''}">
                <div class="cot-cabecera">
                    <div class="cot-titulo">Cotización #${co.idCotizacion}<span class="cot-fecha"><c:out value="${co.fecha}"/></span></div>
                    <span class="badge estado-${co.estado}">${co.estado}</span>
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
                        <form action="${pageContext.request.contextPath}/cotizacion" method="post"
                              onsubmit="return confirm('¿Registrar que el cliente ACEPTÓ la cotización #${co.idCotizacion}?')">
                            <input type="hidden" name="accion" value="aceptar">
                            <input type="hidden" name="id" value="${co.idCotizacion}">
                            <input type="hidden" name="idEvento" value="${ev.idEvento}">
                            <button type="submit" class="btn-aceptar">Registrar aceptación</button>
                        </form>
                        <form action="${pageContext.request.contextPath}/cotizacion" method="post"
                              onsubmit="return confirm('¿Registrar que el cliente RECHAZÓ la cotización #${co.idCotizacion}?')">
                            <input type="hidden" name="accion" value="rechazar">
                            <input type="hidden" name="id" value="${co.idCotizacion}">
                            <input type="hidden" name="idEvento" value="${ev.idEvento}">
                            <button type="submit" class="btn-rechazar">Registrar rechazo</button>
                        </form>
                        <span class="nota-acciones">El cliente también puede responder desde su portal. Usa estos botones si respondió por teléfono o WhatsApp.</span>
                    </div>
                </c:if>
            </div>
        </c:forEach>

    </div>
</main>

<script>
    (function () {
        var filas = document.querySelectorAll('.srv-fila[data-precio]');
        if (!filas.length) return;
        var descuento = document.getElementById('descuento');
        var fmt = function (n) {
            return 'S/ ' + n.toLocaleString('es-PE', {minimumFractionDigits: 2, maximumFractionDigits: 2});
        };
        function recalcular() {
            var subtotal = 0;
            filas.forEach(function (fila) {
                var cant = parseInt(fila.querySelector('input[type="number"]').value, 10);
                var linea = (isNaN(cant) || cant < 0) ? 0 : cant * parseFloat(fila.getAttribute('data-precio'));
                fila.querySelector('.js-linea').textContent = fmt(linea);
                fila.classList.toggle('incluido', linea > 0);
                subtotal += linea;
            });
            var desc = parseFloat(descuento.value);
            if (isNaN(desc) || desc < 0) desc = 0;
            document.getElementById('js-subtotal').textContent = fmt(subtotal);
            document.getElementById('js-total').textContent = fmt(Math.max(subtotal - desc, 0));
        }
        document.getElementById('form-cotizacion').addEventListener('input', recalcular);
        recalcular();
    })();
</script>

</body>
</html>
