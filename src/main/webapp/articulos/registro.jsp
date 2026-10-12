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
    <style>
        :root {
            --bg-primary: #F8F4F1; --rose-dark: #A66A6A; --rose-deep: #743E3E; --rose-light: #D8A7A7;
            --gold: #C6A15B; --text-primary: #3D3333; --text-secondary: #706464;
            --white: #FFFFFF; --border-soft: #EFE8E2;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: 'Montserrat', sans-serif; background: var(--bg-primary); color: var(--text-primary); min-height: 100vh; }

        .navbar { background: var(--white); padding: 18px 40px; border-bottom: 1px solid var(--border-soft);
                  display: flex; justify-content: space-between; align-items: center; }
        .logo { font-size: 1.7rem; font-weight: 700; letter-spacing: 3px; color: var(--rose-dark); }
        .btn-volver { text-decoration: none; color: var(--rose-dark); border: 1px solid var(--rose-light);
                      padding: 8px 16px; border-radius: 20px; font-size: 0.85rem; }

        .contenedor { max-width: 1100px; margin: 36px auto; padding: 0 20px; }
        .card { background: var(--white); padding: 36px 40px; border-radius: 24px; border: 1px solid var(--border-soft); }
        h1 { font-family: 'Playfair Display', Georgia, serif; font-size: 1.6rem; margin-bottom: 4px; }
        .subtitulo { color: var(--text-secondary); font-size: 0.85rem; margin-bottom: 22px; }

        .tabs { display: flex; gap: 6px; border-bottom: 1px solid var(--border-soft); margin-bottom: 22px; }
        .tabs a { padding: 10px 22px; text-decoration: none; font-size: 0.88rem; font-weight: 600;
                  color: var(--text-secondary); border-bottom: 3px solid transparent; margin-bottom: -1px; }
        .tabs a.active { color: var(--rose-dark); border-bottom-color: var(--rose-dark); }

        .mensaje { padding: 12px 16px; border-radius: 10px; margin-bottom: 18px; font-size: 0.85rem; }
        .exito { background: #E8F5E9; color: #2E7D32; }
        .error { background: #FFEBEE; color: #C62828; }

        .search-bar { display: flex; gap: 10px; align-items: center; margin-bottom: 14px; flex-wrap: wrap; }
        .search-bar input[type="search"] { flex: 1; min-width: 240px; padding: 12px 16px; border: 1.5px solid var(--border-soft);
            border-radius: 12px; background: #FAF8F6; font-size: 0.9rem; font-family: inherit; color: var(--text-primary); outline: none; }
        .search-bar input[type="search"]:focus { border-color: var(--rose-dark); background: var(--white);
            box-shadow: 0 0 0 3px rgba(216, 167, 167, 0.25); }
        .btn-primary { padding: 12px 22px; border: none; border-radius: 12px; cursor: pointer;
            background: linear-gradient(135deg, var(--gold), var(--rose-dark)); color: var(--white);
            font-size: 0.82rem; font-weight: 700; letter-spacing: 0.5px; text-transform: uppercase; font-family: inherit; }
        .clear { font-size: 0.82rem; color: var(--rose-dark); text-decoration: none; font-weight: 600; }
        .results-info { font-size: 0.82rem; color: var(--text-secondary); margin-bottom: 12px; }

        details.nuevo { margin-bottom: 18px; border: 1px solid var(--border-soft); border-radius: 14px; background: #FDFBFA; }
        details.nuevo > summary { cursor: pointer; padding: 13px 18px; font-size: 0.88rem; font-weight: 700; color: var(--rose-deep); list-style: none; }
        details.nuevo > summary::-webkit-details-marker { display: none; }
        details.nuevo[open] > summary { border-bottom: 1px solid var(--border-soft); }
        .form-nuevo { padding: 20px 18px 22px; }
        .campo { margin-bottom: 16px; }
        .campo label { display: block; margin-bottom: 6px; font-weight: 600; font-size: 0.78rem; text-transform: uppercase; letter-spacing: 0.4px; }
        .campo input, .campo textarea, .campo select { width: 100%; padding: 11px 12px; border: 1px solid var(--border-soft);
            border-radius: 9px; font-size: 0.9rem; font-family: inherit; background: var(--white); }
        .campo textarea { min-height: 80px; resize: vertical; }
        .campo small { display: block; margin-top: 6px; font-size: 0.78rem; color: var(--text-secondary); }
        .campo small a { color: var(--rose-dark); font-weight: 600; }
        .fila { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }

        .filters { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 16px; }
        .filters a { padding: 7px 14px; border-radius: 20px; font-size: 0.78rem; font-weight: 600; text-decoration: none;
                     color: var(--text-secondary); background: #FAF8F6; border: 1px solid var(--border-soft); }
        .filters a.active { background: var(--rose-dark); color: var(--white); border-color: var(--rose-dark); }

        .tabla-wrap { overflow-x: auto; }
        table { width: 100%; border-collapse: collapse; }
        th, td { text-align: left; padding: 12px 10px; border-bottom: 1px solid var(--border-soft); font-size: 0.86rem; vertical-align: middle; }
        th { text-transform: uppercase; font-size: 0.72rem; letter-spacing: 0.5px; color: var(--text-secondary); }
        .nombre { display: block; font-weight: 600; }
        .desc { display: block; font-size: 0.78rem; color: var(--text-secondary); max-width: 320px;
                white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
        .fila-inactiva td { opacity: 0.6; }
        .badge { padding: 4px 11px; border-radius: 14px; font-size: 0.72rem; font-weight: 700; white-space: nowrap; }
        .badge-activo { background: #F4F8F4; color: #2F6B4F; }
        .badge-inactivo { background: #F0F0F0; color: #706464; }
        .stock-cero { background: #FDF3F3; color: #923838; }
        .stock-bajo { background: #FDF3E3; color: #8C6A29; }
        .actions form { display: inline; }
        .actions a.btn-editar { color: var(--rose-dark); font-size: 0.8rem; font-weight: 600; text-decoration: none; margin-right: 12px; }

        .panel-edicion { border: 1px solid var(--gold); border-left-width: 5px; border-radius: 14px;
                         background: #FFFDF8; padding: 20px 22px 22px; margin-bottom: 22px; }
        .panel-edicion h2 { font-family: 'Playfair Display', Georgia, serif; font-size: 1.15rem; margin-bottom: 14px; }
        .panel-edicion .acciones-form { display: flex; align-items: center; gap: 16px; }
        .btn-link { background: none; border: none; cursor: pointer; font-family: inherit; font-size: 0.8rem; font-weight: 600; padding: 0; }
        .btn-baja { color: #923838; }
        .btn-reactivar { color: #2F6B4F; }
        .empty-msg { text-align: center; padding: 30px; color: var(--text-secondary); }

        @media (max-width: 640px) {
            .fila { grid-template-columns: 1fr; }
            .card { padding: 24px 20px; }
            .navbar { padding: 14px 20px; }
        }
    </style>
</head>
<body>

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

