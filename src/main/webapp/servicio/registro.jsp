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

    // Si se abre el JSP directo (sin pasar por ServicioServlet) no hay datos cargados
    if (request.getAttribute("servicios") == null) {
        response.sendRedirect(request.getContextPath() + "/servicio");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>INARA — Servicios y Precios</title>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&family=Playfair+Display:ital,wght@0,600;0,700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/servicio.css">
</head>
<body class="page-servicio">

<header class="navbar">
    <div class="logo">INARA</div>
    <a href="${pageContext.request.contextPath}/evento" class="btn-volver">&larr; Volver al Centro de Control</a>
</header>

<main class="contenedor">
    <div class="card">

        <h1>Servicios y Precios</h1>
        <p class="subtitulo">Catálogo de servicios que ofrece la promotora. Son la base de las cotizaciones: al cotizar se copia el precio vigente, así que cambiarlo aquí no altera cotizaciones anteriores.</p>

        <c:if test="${not empty exito}">
            <div class="mensaje exito"><c:out value="${exito}"/></div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="mensaje error"><c:out value="${error}"/></div>
        </c:if>

        <%-- Panel de edición: aparece al pulsar "Editar" en una fila --%>
        <c:if test="${not empty servicioEdit}">
            <div class="panel-edicion" id="panel-edicion">
                <h2>Editando: <c:out value="${servicioEdit.nombre}"/></h2>

                <form action="${pageContext.request.contextPath}/servicio" method="post">
                    <input type="hidden" name="accion" value="actualizar">
                    <input type="hidden" name="id" value="${servicioEdit.idServicio}">
                    <input type="hidden" name="filtro" value="${filtro}">
                    <input type="hidden" name="q" value="<c:out value='${busqueda}'/>">

                    <div class="campo">
                        <label for="edit-nombre">Nombre del servicio *</label>
                        <input type="text" id="edit-nombre" name="nombre" maxlength="150"
                               value="<c:out value='${empty formEdit ? servicioEdit.nombre : formEdit.nombre}'/>" required>
                    </div>

                    <div class="campo">
                        <label for="edit-descripcion">Descripción</label>
                        <textarea id="edit-descripcion" name="descripcion" maxlength="1000"><c:out value="${empty formEdit ? servicioEdit.descripcion : formEdit.descripcion}"/></textarea>
                    </div>

                    <div class="campo">
                        <label for="edit-precio">Precio (S/) *</label>
                        <input type="number" id="edit-precio" name="precio" min="0.01" step="0.01"
                               value="<c:out value='${empty formEdit ? servicioEdit.precio : formEdit.precio}'/>" required>
                    </div>

                    <div class="acciones-form">
                        <button type="submit" class="btn-primary">Guardar cambios</button>
                        <a class="clear" href="${pageContext.request.contextPath}/servicio?filtro=${filtro}${qParamHtml}">Cancelar</a>
                    </div>
                </form>
            </div>
        </c:if>

        <form class="search-bar" action="${pageContext.request.contextPath}/servicio" method="get">
            <input type="hidden" name="filtro" value="${filtro}">
            <input type="search" name="q" value="<c:out value='${busqueda}'/>"
                   placeholder="Buscar por nombre o descripción" maxlength="100">
            <button type="submit" class="btn-primary">Buscar</button>
            <c:if test="${not empty busqueda}">
                <a class="clear" href="${pageContext.request.contextPath}/servicio?filtro=${filtro}">Limpiar</a>
            </c:if>
        </form>

        <details class="nuevo" <c:if test="${abrirForm == 'servicio'}">open</c:if>>
            <summary>+ Nuevo servicio</summary>
            <form class="form-nuevo" action="${pageContext.request.contextPath}/servicio" method="post">
                <input type="hidden" name="accion" value="registrar">
                <input type="hidden" name="filtro" value="${filtro}">
                <input type="hidden" name="q" value="<c:out value='${busqueda}'/>">

                <div class="campo">
                    <label for="nombre">Nombre del servicio *</label>
                    <input type="text" id="nombre" name="nombre" maxlength="150" placeholder="Ej: Decoración floral"
                           value="<c:out value='${form.nombre}'/>" required>
                </div>

                <div class="campo">
                    <label for="descripcion">Descripción</label>
                    <textarea id="descripcion" name="descripcion" maxlength="1000"
                              placeholder="Qué incluye el servicio..."><c:out value="${form.descripcion}"/></textarea>
                </div>

                <div class="campo">
                    <label for="precio">Precio (S/) *</label>
                    <input type="number" id="precio" name="precio" min="0.01" step="0.01" placeholder="0.00"
                           value="<c:out value='${form.precio}'/>" required>
                </div>

                <button type="submit" class="btn-primary">Registrar servicio</button>
            </form>
        </details>

        <div class="filters">
            <a href="${pageContext.request.contextPath}/servicio?filtro=ACTIVO${qParamHtml}" class="${filtro == 'ACTIVO' ? 'active' : ''}">Activos</a>
            <a href="${pageContext.request.contextPath}/servicio?filtro=INACTIVO${qParamHtml}" class="${filtro == 'INACTIVO' ? 'active' : ''}">Dados de baja</a>
            <a href="${pageContext.request.contextPath}/servicio?filtro=TODOS${qParamHtml}" class="${filtro == 'TODOS' ? 'active' : ''}">Todos</a>
        </div>

        <c:if test="${not empty busqueda}">
            <p class="results-info">
                ${servicios.size()} resultado(s) para "<strong><c:out value="${busqueda}"/></strong>"
            </p>
        </c:if>

        <div class="tabla-wrap">
            <table>
                <tr>
                    <th>Servicio</th>
                    <th>Precio</th>
                    <th>Estado</th>
                    <th></th>
                </tr>
                <c:forEach var="s" items="${servicios}">
                    <tr class="${s.estado == 'INACTIVO' ? 'fila-inactiva' : ''}">
                        <td class="celda-titulo">
                            <span class="nombre"><c:out value="${s.nombre}"/></span>
                            <c:if test="${not empty s.descripcion}">
                                <span class="desc"><c:out value="${s.descripcion}"/></span>
                            </c:if>
                        </td>
                        <td data-label="Precio"><span class="precio">S/ <fmt:formatNumber value="${s.precio}" minFractionDigits="2" maxFractionDigits="2"/></span></td>
                        <td data-label="Estado">
                            <span class="badge ${s.estado == 'INACTIVO' ? 'badge-inactivo' : 'badge-activo'}">${s.estado == 'INACTIVO' ? 'Inactivo' : 'Activo'}</span>
                        </td>
                        <td class="actions">
                            <a class="btn-editar"
                               href="${pageContext.request.contextPath}/servicio?accion=editar&amp;id=${s.idServicio}&amp;filtro=${filtro}${qParamHtml}">Editar</a>
                            <c:choose>
                                <c:when test="${s.estado == 'INACTIVO'}">
                                    <form action="${pageContext.request.contextPath}/servicio" method="post">
                                        <input type="hidden" name="accion" value="reactivar">
                                        <input type="hidden" name="id" value="${s.idServicio}">
                                        <input type="hidden" name="filtro" value="${filtro}">
                                        <input type="hidden" name="q" value="<c:out value='${busqueda}'/>">
                                        <button type="submit" class="btn-link btn-reactivar">Reactivar</button>
                                    </form>
                                </c:when>
                                <c:otherwise>
                                    <form action="${pageContext.request.contextPath}/servicio" method="post"
                                          onsubmit="return confirm('¿Dar de baja este servicio? Dejará de ofrecerse en cotizaciones nuevas. Podrás reactivarlo después.')">
                                        <input type="hidden" name="accion" value="baja">
                                        <input type="hidden" name="id" value="${s.idServicio}">
                                        <input type="hidden" name="filtro" value="${filtro}">
                                        <input type="hidden" name="q" value="<c:out value='${busqueda}'/>">
                                        <button type="submit" class="btn-link btn-baja">Dar de baja</button>
                                    </form>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty servicios}">
                    <tr>
                        <td colspan="4" class="empty-msg">
                            <c:choose>
                                <c:when test="${not empty busqueda}">No se encontraron servicios que coincidan con tu búsqueda.</c:when>
                                <c:otherwise>No hay servicios para mostrar con este filtro.</c:otherwise>
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
