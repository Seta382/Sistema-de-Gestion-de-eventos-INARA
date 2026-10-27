<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="pe.edu.eventos.model.Empleado" %>
<%@ page import="pe.edu.eventos.dto.UsuarioDTO" %>
<%@ page import="pe.edu.eventos.util.Constantes" %>
<%
    UsuarioDTO sesion = (UsuarioDTO) session.getAttribute(Constantes.SESION_USUARIO);
    if (sesion == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    String rolSesion = sesion.getRol() != null ? sesion.getRol().toUpperCase() : "";
    if (!Constantes.ROL_ADMIN.equals(rolSesion)) {
        response.sendRedirect(request.getContextPath() + "/empleado/dashboard.jsp");
        return;
    }

    Empleado empleado = (Empleado) request.getAttribute("empleado");
    if (empleado == null) {
        response.sendRedirect(request.getContextPath() + "/empleado?accion=listar");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Editar Personal - INARA</title>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&family=Playfair+Display:ital,wght@0,600;0,700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/empleado.css">
</head>
<body class="page-form">

<div class="panel">
    <h1>Editar Personal</h1>
    <p class="subtitle">Actualiza los datos de <%= empleado.getNombreCompleto() %>. El correo no puede modificarse aquí.</p>

    <% if (request.getAttribute("error") != null) { %>
    <div class="alert-danger"><%= request.getAttribute("error") %></div>
    <% } %>

    <form action="${pageContext.request.contextPath}/empleado" method="post">
        <input type="hidden" name="accion" value="actualizar">
        <input type="hidden" name="idUsuario" value="<%= empleado.getIdUsuario() %>">
        <input type="hidden" name="idEmpleado" value="<%= empleado.getIdEmpleado() %>">

        <div class="form-row-2">
            <div class="field-group">
                <label for="nombre">Nombre *</label>
                <input type="text" id="nombre" name="nombre" value="<%= empleado.getNombre() != null ? empleado.getNombre() : "" %>" required>
            </div>
            <div class="field-group">
                <label for="apellido">Apellido *</label>
                <input type="text" id="apellido" name="apellido" value="<%= empleado.getApellido() != null ? empleado.getApellido() : "" %>" required>
            </div>
        </div>

        <div class="form-row-2">
            <div class="field-group">
                <label for="correo">Correo (no editable)</label>
                <input type="email" id="correo" value="<%= empleado.getCorreo() != null ? empleado.getCorreo() : "" %>" readonly>
            </div>
            <div class="field-group">
                <label for="telefono">Teléfono</label>
                <input type="tel" id="telefono" name="telefono" value="<%= empleado.getTelefono() != null ? empleado.getTelefono() : "" %>">
            </div>
        </div>

        <div class="form-row-2">
            <div class="field-group">
                <label for="dni">DNI</label>
                <input type="text" id="dni" name="dni" value="<%= empleado.getDni() != null ? empleado.getDni() : "" %>">
            </div>
            <div class="field-group">
                <label for="cargo">Cargo</label>
                <input type="text" id="cargo" name="cargo" value="<%= empleado.getCargo() != null ? empleado.getCargo() : "" %>">
            </div>
        </div>

        <div class="field-group">
            <label for="area">Área</label>
            <input type="text" id="area" name="area" value="<%= empleado.getArea() != null ? empleado.getArea() : "" %>">
        </div>

        <button type="submit" class="btn-submit">Guardar cambios</button>
    </form>

    <a href="${pageContext.request.contextPath}/empleado?accion=listar" class="link-back">&larr; Volver al listado</a>
</div>

</body>
</html>
