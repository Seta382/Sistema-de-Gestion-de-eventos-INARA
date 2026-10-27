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

    // Si se abre el JSP directo (sin pasar por ArticuloServlet) no hay datos cargados
    if (request.getAttribute("tab") == null) {
        response.sendRedirect(request.getContextPath() + "/articulo");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>INARA — Insumos y Proveedores</title>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&family=Playfair+Display:ital,wght@0,600;0,700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/articulo.css">

</head>
<body class="page-articulo">

<header class="navbar">
    <div class="logo">INARA</div>
    <a href="${pageContext.request.contextPath}/evento" class="btn-volver">&larr; Volver al Centro de Control</a>
</header>

<main class="contenedor">
    <div class="card">

        <h1>Insumos y Proveedores</h1>
        <p class="subtitulo">Administra el inventario disponible para los eventos y los proveedores que lo abastecen.</p>

        <nav class="tabs">
            <a href="${pageContext.request.contextPath}/articulo?tab=insumos" class="${tab == 'insumos' ? 'active' : ''}">Insumos</a>
            <a href="${pageContext.request.contextPath}/articulo?tab=proveedores" class="${tab == 'proveedores' ? 'active' : ''}">Proveedores</a>
        </nav>

        <c:if test="${not empty exito}">
            <div class="mensaje exito"><c:out value="${exito}"/></div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="mensaje error"><c:out value="${error}"/></div>
        </c:if>

        <%-- =========================== PESTAÑA INSUMOS =========================== --%>
        <c:if test="${tab == 'insumos'}">

            <%-- Panel de edición: aparece al pulsar "Editar" en una fila --%>
            <c:if test="${not empty articuloEdit}">
                <div class="panel-edicion" id="panel-edicion">
                    <h2>Editando: <c:out value="${articuloEdit.nombre}"/></h2>

                    <form action="${pageContext.request.contextPath}/articulo" method="post">
                        <input type="hidden" name="accion" value="actualizar">
                        <input type="hidden" name="tab" value="insumos">
                        <input type="hidden" name="id" value="${articuloEdit.idArticulo}">
                        <input type="hidden" name="stockAnterior"
                               value="<c:out value='${not empty formEdit and not empty formEdit.stockAnterior ? formEdit.stockAnterior : articuloEdit.stock}'/>">
                        <input type="hidden" name="filtro" value="${filtro}">
                        <input type="hidden" name="q" value="<c:out value='${busqueda}'/>">

                        <div class="campo">
                            <label for="edit-nombre">Nombre del insumo *</label>
                            <input type="text" id="edit-nombre" name="nombre" maxlength="150"
                                   value="<c:out value='${empty formEdit ? articuloEdit.nombre : formEdit.nombre}'/>" required>
                        </div>

                        <div class="campo">
                            <label for="edit-descripcion">Descripción</label>
                            <textarea id="edit-descripcion" name="descripcion"><c:out value="${empty formEdit ? articuloEdit.descripcion : formEdit.descripcion}"/></textarea>
                        </div>

                        <div class="fila">
                            <div class="campo">
                                <label for="edit-precio">Precio (S/) *</label>
                                <input type="number" id="edit-precio" name="precio" min="0" step="0.01"
                                       value="<c:out value='${empty formEdit ? articuloEdit.precio : formEdit.precio}'/>" required>
                            </div>
                            <div class="campo">
                                <label for="edit-stock">Stock *</label>
                                <input type="number" id="edit-stock" name="stock" min="0" step="1"
                                       value="<c:out value='${empty formEdit ? articuloEdit.stock : formEdit.stock}'/>" required>
                                <small>Stock actual: ${articuloEdit.stock}. Se descuenta solo al registrar eventos; cámbialo aquí para reponer o corregir el inventario.</small>
                            </div>
                        </div>

                        <div class="campo">
                            <label for="edit-proveedor">Proveedor</label>
                            <select id="edit-proveedor" name="idProveedor">
                                <option value="0">-- Sin proveedor --</option>
                                <c:forEach var="p" items="${proveedoresEdicion}">
                                    <option value="${p.idProveedor}" ${(empty formEdit ? articuloEdit.idProveedor : formEdit.idProveedor) eq p.idProveedor.toString() ? 'selected' : ''}>
                                        <c:out value="${p.razonSocial}"/> — RUC <c:out value="${p.ruc}"/><c:if test="${p.estado == 'INACTIVO'}"> (inactivo)</c:if>
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="acciones-form">
                            <button type="submit" class="btn-primary">Guardar cambios</button>
                            <a class="clear" href="${pageContext.request.contextPath}/articulo?tab=insumos&amp;filtro=${filtro}${qParamHtml}">Cancelar</a>
                        </div>
                    </form>
                </div>
            </c:if>

            <form class="search-bar" action="${pageContext.request.contextPath}/articulo" method="get">
                <input type="hidden" name="tab" value="insumos">
                <input type="hidden" name="filtro" value="${filtro}">
                <input type="search" name="q" value="<c:out value='${busqueda}'/>"
                       placeholder="Buscar por nombre, descripción, proveedor o RUC" maxlength="100">
                <button type="submit" class="btn-primary">Buscar</button>
                <c:if test="${not empty busqueda}">
                    <a class="clear" href="${pageContext.request.contextPath}/articulo?tab=insumos&amp;filtro=${filtro}">Limpiar</a>
                </c:if>
            </form>

            <details class="nuevo" <c:if test="${abrirForm == 'insumo'}">open</c:if>>
                <summary>+ Nuevo insumo</summary>
                <form class="form-nuevo" action="${pageContext.request.contextPath}/articulo" method="post">
                    <input type="hidden" name="accion" value="registrar">
                    <input type="hidden" name="tab" value="insumos">
                    <input type="hidden" name="filtro" value="${filtro}">
                    <input type="hidden" name="q" value="<c:out value='${busqueda}'/>">

                    <div class="campo">
                        <label for="nombre">Nombre del insumo *</label>
                        <input type="text" id="nombre" name="nombre" maxlength="150" placeholder="Ej: Mesa redonda"
                               value="<c:out value='${form.nombre}'/>" required>
                    </div>

                    <div class="campo">
                        <label for="descripcion">Descripción</label>
                        <textarea id="descripcion" name="descripcion" placeholder="Descripción del insumo..."><c:out value="${form.descripcion}"/></textarea>
                    </div>

                    <div class="fila">
                        <div class="campo">
                            <label for="precio">Precio (S/) *</label>
                            <input type="number" id="precio" name="precio" min="0" step="0.01" placeholder="0.00"
                                   value="<c:out value='${form.precio}'/>" required>
                        </div>
                        <div class="campo">
                            <label for="stock">Stock *</label>
                            <input type="number" id="stock" name="stock" min="0" step="1" placeholder="0"
                                   value="<c:out value='${form.stock}'/>" required>
                        </div>
                    </div>

                    <div class="campo">
                        <label for="idProveedor">Proveedor</label>
                        <select id="idProveedor" name="idProveedor">
                            <option value="0">-- Seleccionar proveedor --</option>
                            <c:forEach var="p" items="${proveedoresActivos}">
                                <option value="${p.idProveedor}" ${form.idProveedor eq p.idProveedor.toString() ? 'selected' : ''}>
                                    <c:out value="${p.razonSocial}"/> — RUC <c:out value="${p.ruc}"/>
                                </option>
                            </c:forEach>
                        </select>
                        <small>¿No está el proveedor?
                            <a href="${pageContext.request.contextPath}/articulo?tab=proveedores&amp;nuevo=1">Agrégalo en la pestaña Proveedores</a>
                        </small>
                    </div>

                    <button type="submit" class="btn-primary">Registrar insumo</button>
                </form>
            </details>

            <div class="filters">
                <a href="${pageContext.request.contextPath}/articulo?tab=insumos&amp;filtro=ACTIVO${qParamHtml}" class="${filtro == 'ACTIVO' ? 'active' : ''}">Activos</a>
                <a href="${pageContext.request.contextPath}/articulo?tab=insumos&amp;filtro=STOCK_BAJO${qParamHtml}" class="${filtro == 'STOCK_BAJO' ? 'active' : ''}">Stock bajo (&le; ${umbralStockBajo})</a>
                <a href="${pageContext.request.contextPath}/articulo?tab=insumos&amp;filtro=INACTIVO${qParamHtml}" class="${filtro == 'INACTIVO' ? 'active' : ''}">Dados de baja</a>
                <a href="${pageContext.request.contextPath}/articulo?tab=insumos&amp;filtro=TODOS${qParamHtml}" class="${filtro == 'TODOS' ? 'active' : ''}">Todos</a>
            </div>

            <c:if test="${not empty busqueda}">
                <p class="results-info">
                    ${articulos.size()} resultado(s) para "<strong><c:out value="${busqueda}"/></strong>"
                </p>
            </c:if>

            <div class="tabla-wrap">
                <table>
                    <tr>
                        <th>Insumo</th>
                        <th>Proveedor</th>
                        <th>Precio</th>
                        <th>Stock</th>
                        <th>Estado</th>
                        <th></th>
                    </tr>
                    <c:forEach var="a" items="${articulos}">
                        <tr class="${a.estado == 'INACTIVO' ? 'fila-inactiva' : ''}">
                            <td>
                                <span class="nombre"><c:out value="${a.nombre}"/></span>
                                <c:if test="${not empty a.descripcion}">
                                    <span class="desc"><c:out value="${a.descripcion}"/></span>
                                </c:if>
                            </td>
                            <td><c:out value="${a.nombreProveedor}"/></td>
                            <td>S/ ${a.precio}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${a.stock == 0}"><span class="badge stock-cero">Sin stock</span></c:when>
                                    <c:when test="${a.stock <= umbralStockBajo}"><span class="badge stock-bajo">${a.stock} &middot; bajo</span></c:when>
                                    <c:otherwise>${a.stock}</c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <span class="badge ${a.estado == 'INACTIVO' ? 'badge-inactivo' : 'badge-activo'}">${a.estado == 'INACTIVO' ? 'Inactivo' : 'Activo'}</span>
                            </td>
                            <td class="actions">
                                <a class="btn-editar"
                                   href="${pageContext.request.contextPath}/articulo?tab=insumos&amp;accion=editar&amp;id=${a.idArticulo}&amp;filtro=${filtro}${qParamHtml}">Editar</a>
                                <c:choose>
                                    <c:when test="${a.estado == 'INACTIVO'}">
                                        <form action="${pageContext.request.contextPath}/articulo" method="post">
                                            <input type="hidden" name="accion" value="reactivar">
                                            <input type="hidden" name="id" value="${a.idArticulo}">
                                            <input type="hidden" name="filtro" value="${filtro}">
                                            <input type="hidden" name="q" value="<c:out value='${busqueda}'/>">
                                            <button type="submit" class="btn-link btn-reactivar">Reactivar</button>
                                        </form>
                                    </c:when>
                                    <c:otherwise>
                                        <form action="${pageContext.request.contextPath}/articulo" method="post"
                                              onsubmit="return confirm('¿Dar de baja este insumo? Dejará de aparecer al registrar eventos. Podrás reactivarlo después.')">
                                            <input type="hidden" name="accion" value="baja">
                                            <input type="hidden" name="id" value="${a.idArticulo}">
                                            <input type="hidden" name="filtro" value="${filtro}">
                                            <input type="hidden" name="q" value="<c:out value='${busqueda}'/>">
                                            <button type="submit" class="btn-link btn-baja">Dar de baja</button>
                                        </form>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty articulos}">
                        <tr>
                            <td colspan="6" class="empty-msg">
                                <c:choose>
                                    <c:when test="${not empty busqueda}">No se encontraron insumos que coincidan con tu búsqueda.</c:when>
                                    <c:otherwise>No hay insumos para mostrar con este filtro.</c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:if>
                </table>
            </div>
        </c:if>

        <%-- ========================= PESTAÑA PROVEEDORES ========================= --%>
        <c:if test="${tab == 'proveedores'}">

            <form class="search-bar" action="${pageContext.request.contextPath}/articulo" method="get">
                <input type="hidden" name="tab" value="proveedores">
                <input type="search" name="q" value="<c:out value='${busqueda}'/>"
                       placeholder="Buscar por razón social, RUC, correo o dirección" maxlength="100">
                <button type="submit" class="btn-primary">Buscar</button>
                <c:if test="${not empty busqueda}">
                    <a class="clear" href="${pageContext.request.contextPath}/articulo?tab=proveedores">Limpiar</a>
                </c:if>
            </form>

            <details class="nuevo" <c:if test="${abrirForm == 'proveedor'}">open</c:if>>
                <summary>+ Nuevo proveedor</summary>
                <form class="form-nuevo" action="${pageContext.request.contextPath}/articulo" method="post">
                    <input type="hidden" name="accion" value="registrarProveedor">
                    <input type="hidden" name="tab" value="proveedores">
                    <input type="hidden" name="q" value="<c:out value='${busqueda}'/>">

                    <div class="fila">
                        <div class="campo">
                            <label for="razonSocial">Razón social *</label>
                            <input type="text" id="razonSocial" name="razonSocial" maxlength="150" placeholder="Ej: Eventos y Mobiliario S.A.C."
                                   value="<c:out value='${form.razonSocial}'/>" required>
                        </div>
                        <div class="campo">
                            <label for="ruc">RUC *</label>
                            <input type="text" id="ruc" name="ruc" maxlength="11" pattern="\d{11}" inputmode="numeric"
                                   title="El RUC tiene 11 dígitos" placeholder="11 dígitos"
                                   value="<c:out value='${form.ruc}'/>" required>
                        </div>
                    </div>

                    <div class="fila">
                        <div class="campo">
                            <label for="correo">Correo</label>
                            <input type="email" id="correo" name="correo" maxlength="150" placeholder="contacto@proveedor.com"
                                   value="<c:out value='${form.correo}'/>">
                        </div>
                        <div class="campo">
                            <label for="telefono">Teléfono</label>
                            <input type="tel" id="telefono" name="telefono" maxlength="20" placeholder="Ej: 987654321"
                                   value="<c:out value='${form.telefono}'/>">
                        </div>
                    </div>

                    <div class="campo">
                        <label for="direccion">Dirección</label>
                        <input type="text" id="direccion" name="direccion" maxlength="150" placeholder="Ej: Av. Industrial 123, Lima"
                               value="<c:out value='${form.direccion}'/>">
                    </div>

                    <button type="submit" class="btn-primary">Registrar proveedor</button>
                </form>
            </details>

            <c:if test="${not empty busqueda}">
                <p class="results-info">
                    ${proveedores.size()} resultado(s) para "<strong><c:out value="${busqueda}"/></strong>"
                </p>
            </c:if>

            <div class="tabla-wrap">
                <table>
                    <tr>
                        <th>Razón social</th>
                        <th>RUC</th>
                        <th>Correo</th>
                        <th>Teléfono</th>
                        <th>Dirección</th>
                        <th>Estado</th>
                    </tr>
                    <c:forEach var="p" items="${proveedores}">
                        <tr class="${p.estado == 'INACTIVO' ? 'fila-inactiva' : ''}">
                            <td><span class="nombre"><c:out value="${p.razonSocial}"/></span></td>
                            <td><c:out value="${p.ruc}"/></td>
                            <td><c:out value="${p.correo}"/></td>
                            <td><c:out value="${p.telefono}"/></td>
                            <td><c:out value="${p.direccion}"/></td>
                            <td>
                                <span class="badge ${p.estado == 'INACTIVO' ? 'badge-inactivo' : 'badge-activo'}">${p.estado == 'INACTIVO' ? 'Inactivo' : 'Activo'}</span>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty proveedores}">
                        <tr>
                            <td colspan="6" class="empty-msg">
                                <c:choose>
                                    <c:when test="${not empty busqueda}">No se encontraron proveedores que coincidan con tu búsqueda.</c:when>
                                    <c:otherwise>Todavía no hay proveedores registrados.</c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:if>
                </table>
            </div>
        </c:if>

    </div>
</main>

</body>
</html>

