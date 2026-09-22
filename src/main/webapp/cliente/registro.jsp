<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Crear Cuenta - INARA</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css">
    <style>
        .form-row {
            display: flex;
            gap: 12px;
        }
        .form-row .form-group {
            flex: 1;
        }
        .link-footer {
            text-align: center;
            margin-top: 20px;
            font-size: 0.9rem;
            color: #555;
        }
        .link-footer a {
            color: #1e3c72;
            font-weight: 600;
            text-decoration: none;
        }
        .link-footer a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>

    <div class="login-container" style="max-width: 480px;">
        <div class="login-header">
            <h1>INARA</h1>
            <p>Registro de Nuevo Cliente</p>
        </div>

        <%
            String error = (String) request.getAttribute("error");
            if (error != null) {
        %>
            <div class="alert alert-danger">
                <%= error %>
            </div>
        <%
            }
        %>

        <form action="${pageContext.request.contextPath}/registro" method="post" class="login-form">
            <input type="hidden" name="accion" value="registrar">

            <div class="form-row">
                <div class="form-group">
                    <label for="nombre">Nombre: *</label>
                    <input type="text" id="nombre" name="nombre" 
                           value="<%= request.getAttribute("nombre") != null ? request.getAttribute("nombre") : "" %>" 
                           placeholder="Ej. Carlos" required autofocus>
                </div>
                <div class="form-group">
                    <label for="apellido">Apellido: *</label>
                    <input type="text" id="apellido" name="apellido" 
                           value="<%= request.getAttribute("apellido") != null ? request.getAttribute("apellido") : "" %>" 
                           placeholder="Ej. Mendoza" required>
                </div>
            </div>

            <div class="form-group">
                <label for="correo">Correo Electrónico: *</label>
                <input type="email" id="correo" name="correo" 
                       value="<%= request.getAttribute("correo") != null ? request.getAttribute("correo") : "" %>" 
                       placeholder="carlos@correo.com" required>
            </div>

            <div class="form-group">
                <label for="telefono">Teléfono / WhatsApp:</label>
                <input type="tel" id="telefono" name="telefono" 
                       value="<%= request.getAttribute("telefono") != null ? request.getAttribute("telefono") : "" %>" 
                       placeholder="Ej. 987654321">
            </div>

            <div class="form-group">
                <label for="password">Contraseña: *</label>
                <input type="password" id="password" name="password" placeholder="Mínimo 6 caracteres" required>
            </div>

            <button type="submit" class="btn-submit">Crear Cuenta</button>

            <div class="link-footer">
                ¿Ya tienes una cuenta? <a href="${pageContext.request.contextPath}/login">Inicia sesión aquí</a>
            </div>
        </form>
    </div>

    <script src="${pageContext.request.contextPath}/js/login.js"></script>
</body>
</html>
