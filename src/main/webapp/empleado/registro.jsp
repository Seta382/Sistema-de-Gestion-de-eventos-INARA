<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="pe.edu.eventos.dto.UsuarioDTO" %>
<%@ page import="pe.edu.eventos.util.Constantes" %>
<%
    UsuarioDTO usuario = (UsuarioDTO) session.getAttribute(Constantes.SESION_USUARIO);
    if (usuario == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    String rol = usuario.getRol() != null ? usuario.getRol().toUpperCase() : "";
    if (!Constantes.ROL_ADMIN.equals(rol)) {
        response.sendRedirect(request.getContextPath() + "/empleado/dashboard.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Alta de Personal - INARA</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&family=Playfair+Display:ital,wght@0,500;0,600;0,700;1,400;1,600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/empleado.css">
</head>
<body class="page-form">

<div class="panel">
    <h1>Alta de Personal Administrativo</h1>
    <p class="subtitle">Registra un nuevo colaborador operativo de INARA. Podrá iniciar sesión con el correo y contraseña indicados.</p>

    <% if (request.getAttribute("error") != null) { %>
    <div class="alert-danger"><%= request.getAttribute("error") %></div>
    <% } %>

    <form action="${pageContext.request.contextPath}/empleado" method="post">
        <input type="hidden" name="accion" value="registrar">

        <div class="form-row-2">
            <div class="field-group">
                <label for="nombre">Nombre *</label>
                <input type="text" id="nombre" name="nombre"
                       value="<%= request.getAttribute("nombre") != null ? request.getAttribute("nombre") : "" %>" required>
            </div>
            <div class="field-group">
                <label for="apellido">Apellido *</label>
                <input type="text" id="apellido" name="apellido"
                       value="<%= request.getAttribute("apellido") != null ? request.getAttribute("apellido") : "" %>" required>
            </div>
        </div>

        <div class="form-row-2">
            <div class="field-group">
                <label for="correo">Correo *</label>
                <input type="email" id="correo" name="correo"
                       value="<%= request.getAttribute("correo") != null ? request.getAttribute("correo") : "" %>" required>
            </div>
            <div class="field-group">
                <label for="telefono">Teléfono</label>
                <input type="tel" id="telefono" name="telefono"
                       value="<%= request.getAttribute("telefono") != null ? request.getAttribute("telefono") : "" %>">
            </div>
        </div>

        <div class="field-group">
            <label for="password">Contraseña temporal *</label>
            <input type="password" id="password" name="password" required>
        </div>

        <div class="form-row-2">
            <div class="field-group">
                <label for="dni">DNI</label>
                <input type="text" id="dni" name="dni"
                       value="<%= request.getAttribute("dni") != null ? request.getAttribute("dni") : "" %>">
            </div>
            <div class="field-group">
                <label for="cargo">Cargo</label>
                <input type="text" id="cargo" name="cargo" placeholder="Ej. Coordinador de eventos"
                       value="<%= request.getAttribute("cargo") != null ? request.getAttribute("cargo") : "" %>">
            </div>
        </div>

        <div class="field-group">
            <label for="area">Área</label>
            <input type="text" id="area" name="area" placeholder="Ej. Producción"
                   value="<%= request.getAttribute("area") != null ? request.getAttribute("area") : "" %>">
        </div>

        <button type="submit" class="btn-submit">Registrar personal</button>
    </form>

    <a href="${pageContext.request.contextPath}/empleado/dashboard.jsp" class="link-back">&larr; Volver al panel administrativo</a>
</div>

</body>
</html>

