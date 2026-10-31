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

    // Si se abre el JSP directo (sin pasar por AcuerdoServlet) no hay datos cargados
    if (request.getAttribute("panel") == null) {
        response.sendRedirect(request.getContextPath() + "/acuerdo");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>INARA — Acuerdo del Evento</title>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&family=Playfair+Display:ital,wght@0,600;0,700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/acuerdo.css">
</head>
<body class="page-acuerdo">

<header class="navbar">
    <div class="logo">INARA</div>
    <a href="${pageContext.request.contextPath}/evento?accion=cronograma&amp;filtro=TODOS" class="btn-volver">&larr; Volver al cronograma</a>
</header>

<main class="contenedor">
    <div class="card">

        <h1>Acuerdo del evento</h1>
        <p class="subtitulo">Las condiciones que el cliente acepta después de aprobar una cotización. Al aceptarlas, el evento queda confirmado.</p>

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
            <div><span class="dato-label">Fecha</span><span class="dato-valor"><c:out value="${ev.fechaEvento}"/></span></div>
            <div><span class="dato-label">Etapa</span><span class="dato-valor"><span class="badge badge-activo"><c:out value="${ev.estado}"/></span></span></div>
            <c:if test="${not empty panel.cotizacionAceptada}">
                <div><span class="dato-label">Cotización aceptada</span>
                    <span class="dato-valor">#${panel.cotizacionAceptada.idCotizacion} · S/ <fmt:formatNumber value="${panel.cotizacionAceptada.total}" minFractionDigits="2" maxFractionDigits="2"/></span></div>
            </c:if>
        </div>

        <c:if test="${not panel.puedeCrear}">
            <div class="aviso">
                <c:out value="${panel.motivoBloqueo}"/>
                <c:if test="${empty panel.cotizacionAceptada}">
                    <a href="${pageContext.request.contextPath}/cotizacion?idEvento=${ev.idEvento}">Ir a las cotizaciones del evento</a>.
                </c:if>
            </div>
        </c:if>

        <%-- Edición de las condiciones de un acuerdo pendiente --%>
        <c:if test="${not empty acuerdoEdit}">
            <div class="panel-edicion" id="panel-edicion">
                <h2>Editando el acuerdo #${acuerdoEdit.idAcuerdo}</h2>
                <form action="${pageContext.request.contextPath}/acuerdo" method="post">
                    <input type="hidden" name="accion" value="actualizar">
                    <input type="hidden" name="id" value="${acuerdoEdit.idAcuerdo}">
                    <input type="hidden" name="idEvento" value="${ev.idEvento}">
                    <div class="campo">
                        <label for="edit-condiciones">Condiciones *</label>
                        <textarea id="edit-condiciones" name="condiciones" class="condiciones" maxlength="5000" required><c:out value="${empty condicionesForm ? acuerdoEdit.condiciones : condicionesForm}"/></textarea>
                    </div>
                    <div class="acciones-form">
                        <button type="submit" class="btn-primary">Guardar cambios</button>
                        <a class="clear" href="${pageContext.request.contextPath}/acuerdo?idEvento=${ev.idEvento}">Cancelar</a>
                    </div>
                </form>
            </div>
        </c:if>

        <c:if test="${panel.puedeCrear}">
            <details class="nuevo" open>
                <summary>+ Nuevo acuerdo</summary>
                <form class="form-nuevo" action="${pageContext.request.contextPath}/acuerdo" method="post">
                    <input type="hidden" name="accion" value="crear">
                    <input type="hidden" name="idEvento" value="${ev.idEvento}">
                    <p class="ayuda-borrador">Armamos un borrador con los servicios y el total de la cotización aceptada. Ajusta las condiciones antes de enviarlo al cliente.</p>
                    <div class="campo">
                        <label for="condiciones">Condiciones *</label>
                        <textarea id="condiciones" name="condiciones" class="condiciones" maxlength="5000" required><c:out value="${empty condicionesForm ? panel.borrador : condicionesForm}"/></textarea>
                    </div>
                    <button type="submit" class="btn-primary">Crear acuerdo</button>
                </form>
            </details>
        </c:if>

        <h2 class="seccion">Acuerdos de este evento</h2>

        <c:if test="${empty panel.acuerdos}">
            <p class="empty-msg">Todavía no hay acuerdos para este evento.</p>
        </c:if>

        <c:forEach var="ac" items="${panel.acuerdos}">
            <div class="acuerdo-card ${ac.estado == 'PENDIENTE' ? 'acuerdo-pendiente' : ''} ${ac.estado == 'ACEPTADO' ? 'acuerdo-aceptado' : ''} ${ac.estado == 'CANCELADO' ? 'acuerdo-inactivo' : ''}">
                <div class="acuerdo-cabecera">
                    <div class="acuerdo-titulo">Acuerdo #${ac.idAcuerdo}<span class="acuerdo-fecha"><c:out value="${ac.fechaRegistroTxt}"/></span></div>
                    <span class="badge estado-${ac.estado}">${ac.estado}</span>
                </div>

                <div class="acuerdo-texto"><c:out value="${ac.condiciones}"/></div>

                <c:if test="${not empty ac.fechaConfirmacionTxt}">
                    <p class="acuerdo-meta">Aceptado el <c:out value="${ac.fechaConfirmacionTxt}"/></p>
                </c:if>

                <c:if test="${ac.estado == 'PENDIENTE'}">
                    <div class="acuerdo-acciones">
                        <a class="btn-secundario" href="${pageContext.request.contextPath}/acuerdo?idEvento=${ev.idEvento}&amp;editar=${ac.idAcuerdo}#panel-edicion">Editar condiciones</a>
                        <form action="${pageContext.request.contextPath}/acuerdo" method="post"
                              onsubmit="return confirm('¿Registrar que el cliente ACEPTÓ el acuerdo #${ac.idAcuerdo}? El evento pasará a CONFIRMADO.')">
                            <input type="hidden" name="accion" value="aceptar">
                            <input type="hidden" name="id" value="${ac.idAcuerdo}">
                            <input type="hidden" name="idEvento" value="${ev.idEvento}">
                            <button type="submit" class="btn-aceptar">Registrar aceptación</button>
                        </form>
                        <form action="${pageContext.request.contextPath}/acuerdo" method="post"
                              onsubmit="return confirm('¿Cancelar el acuerdo #${ac.idAcuerdo}?')">
                            <input type="hidden" name="accion" value="cancelar">
                            <input type="hidden" name="id" value="${ac.idAcuerdo}">
                            <input type="hidden" name="idEvento" value="${ev.idEvento}">
                            <button type="submit" class="btn-rechazar">Cancelar acuerdo</button>
                        </form>
                        <span class="nota-acciones">El cliente también puede aceptarlo desde su portal. Usa «Registrar aceptación» si lo aceptó en persona o por WhatsApp.</span>
                    </div>
                </c:if>

                <c:if test="${ac.estado == 'ACEPTADO' and ev.estado == 'CONFIRMADO'}">
                    <div class="acuerdo-acciones">
                        <form action="${pageContext.request.contextPath}/acuerdo" method="post"
                              onsubmit="return confirm('¿Cancelar el acuerdo #${ac.idAcuerdo}? El evento volverá a la etapa COTIZADO.')">
                            <input type="hidden" name="accion" value="cancelar">
                            <input type="hidden" name="id" value="${ac.idAcuerdo}">
                            <input type="hidden" name="idEvento" value="${ev.idEvento}">
                            <button type="submit" class="btn-rechazar">Cancelar acuerdo</button>
                        </form>
                        <span class="nota-acciones">Si lo cancelas, el evento vuelve a «Cotizado» y podrás redactar un acuerdo nuevo.</span>
                    </div>
                </c:if>
            </div>
        </c:forEach>

    </div>
</main>

</body>
</html>
