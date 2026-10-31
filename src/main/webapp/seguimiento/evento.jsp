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

    // Si se abre el JSP directo (sin pasar por SeguimientoServlet) no hay datos cargados
    if (request.getAttribute("evento") == null) {
        response.sendRedirect(request.getContextPath() + "/empleado/eventos.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>INARA — Seguimiento del Evento</title>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&family=Playfair+Display:ital,wght@0,600;0,700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/seguimiento.css">
</head>
<body class="page-seguimiento">

<header class="navbar">
    <div class="logo">INARA</div>
    <a href="${pageContext.request.contextPath}/evento?accion=cronograma&amp;filtro=TODOS" class="btn-volver">&larr; Volver al cronograma</a>
</header>

<main class="contenedor">
    <div class="card">

        <h1>Seguimiento del evento</h1>
        <p class="subtitulo">Registra los avances de la preparación. El cliente los ve en su pantalla «Mi evento».</p>

        <c:if test="${not empty exito}">
            <div class="mensaje exito"><c:out value="${exito}"/></div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="mensaje error"><c:out value="${error}"/></div>
        </c:if>

        <div class="evento-ficha">
            <div><span class="dato-label">Evento</span><span class="dato-valor"><c:out value="${empty evento.nombre ? 'Evento INARA' : evento.nombre}"/></span></div>
            <div><span class="dato-label">Cliente titular</span><span class="dato-valor"><c:out value="${evento.nombreCliente}"/></span></div>
            <div><span class="dato-label">Fecha</span><span class="dato-valor"><c:out value="${evento.fechaEvento}"/></span></div>
            <div><span class="dato-label">Etapa</span><span class="dato-valor"><span class="badge badge-activo"><c:out value="${evento.estado}"/></span></span></div>
        </div>

        <c:if test="${not empty motivoBloqueo}">
            <div class="aviso"><c:out value="${motivoBloqueo}"/></div>
        </c:if>

        <%-- Edición de un avance --%>
        <c:if test="${not empty avanceEdit}">
            <div class="panel-edicion" id="panel-edicion">
                <h2>Editando el avance</h2>
                <form action="${pageContext.request.contextPath}/seguimiento" method="post">
                    <input type="hidden" name="accion" value="actualizar">
                    <input type="hidden" name="id" value="${avanceEdit.idSeguimiento}">
                    <input type="hidden" name="idEvento" value="${evento.idEvento}">
                    <div class="campo">
                        <label for="edit-titulo">Título *</label>
                        <input type="text" id="edit-titulo" name="titulo" maxlength="150" required
                               value="<c:out value='${empty form ? avanceEdit.titulo : form.titulo}'/>">
                    </div>
                    <div class="campo">
                        <label for="edit-descripcion">Detalle</label>
                        <textarea id="edit-descripcion" name="descripcion" class="detalle" maxlength="2000"><c:out value="${empty form ? avanceEdit.descripcion : form.descripcion}"/></textarea>
                    </div>
                    <div class="acciones-form">
                        <button type="submit" class="btn-primary">Guardar cambios</button>
                        <a class="clear" href="${pageContext.request.contextPath}/seguimiento?idEvento=${evento.idEvento}">Cancelar</a>
                    </div>
                </form>
            </div>
        </c:if>

        <c:if test="${empty motivoBloqueo and empty avanceEdit}">
            <details class="nuevo" ${not empty form ? 'open' : ''}>
                <summary>+ Registrar avance</summary>
                <form class="form-nuevo" action="${pageContext.request.contextPath}/seguimiento" method="post">
                    <input type="hidden" name="accion" value="registrar">
                    <input type="hidden" name="idEvento" value="${evento.idEvento}">
                    <div class="campo">
                        <label for="titulo">Título *</label>
                        <input type="text" id="titulo" name="titulo" maxlength="150" required
                               placeholder="Ej. Decoración reservada" value="<c:out value='${form.titulo}'/>">
                    </div>
                    <div class="campo">
                        <label for="descripcion">Detalle</label>
                        <textarea id="descripcion" name="descripcion" class="detalle" maxlength="2000"
                                  placeholder="Qué se hizo y qué sigue (opcional)"><c:out value="${form.descripcion}"/></textarea>
                    </div>
                    <button type="submit" class="btn-primary">Registrar avance</button>
                </form>
            </details>
        </c:if>

        <h2 class="seccion">Avances registrados</h2>

        <c:if test="${empty avances}">
            <p class="empty-msg">Todavía no hay avances para este evento.</p>
        </c:if>

        <div class="linea">
            <c:forEach var="av" items="${avances}">
                <div class="avance">
                    <div class="avance-titulo"><c:out value="${av.titulo}"/></div>
                    <div class="avance-fecha"><c:out value="${av.fechaRegistroTxt}"/></div>
                    <c:if test="${not empty av.descripcion}">
                        <div class="avance-detalle"><c:out value="${av.descripcion}"/></div>
                    </c:if>
                    <div class="avance-acciones">
                        <a href="${pageContext.request.contextPath}/seguimiento?idEvento=${evento.idEvento}&amp;editar=${av.idSeguimiento}#panel-edicion">Editar</a>
                        <form action="${pageContext.request.contextPath}/seguimiento" method="post"
                              onsubmit="return confirm('¿Eliminar este avance? El cliente dejará de verlo.')">
                            <input type="hidden" name="accion" value="eliminar">
                            <input type="hidden" name="id" value="${av.idSeguimiento}">
                            <input type="hidden" name="idEvento" value="${evento.idEvento}">
                            <button type="submit" class="btn-link btn-baja">Eliminar</button>
                        </form>
                    </div>
                </div>
            </c:forEach>
        </div>

    </div>
</main>

</body>
</html>
