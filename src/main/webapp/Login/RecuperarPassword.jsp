<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
boolean verificado = Boolean.TRUE.equals(request.getAttribute("verificado"));
String error = (String) request.getAttribute("error");
%>

<!DOCTYPE html>

<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Recuperar contraseña - INARA</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/login.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/login.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/login-paginas.css">
    ...


</head>
<body class="page-recuperar">


<main class="recuperacion">
    <h1>Recuperar contraseña</h1>

    <p>
        <% if (verificado) { %>
            Verificación correcta. Ingresa tu nueva contraseña.
        <% } else { %>
            Ingresa tus datos personales para comprobar tu cuenta.
        <% } %>
    </p>

    <% if (error != null) { %>
        <div class="mensaje-error"><%= error %></div>
    <% } %>

    <% if (!verificado) { %>

        <form action="<%= request.getContextPath() %>/recuperar-password"
              method="post">

            <input type="hidden" name="accion" value="verificar">

            <div class="campo">
                <label for="nombreCompleto">Nombre completo</label>
                <input type="text" id="nombreCompleto"
                       name="nombreCompleto" required>
            </div>

            <div class="campo">
                <label for="dni">DNI</label>
                <input type="text" id="dni" name="dni"
                       inputmode="numeric" required>
            </div>

            <div class="campo">
                <label for="telefono">Celular</label>
                <input type="tel" id="telefono" name="telefono"
                       required>
            </div>

            <div class="campo">
                <label for="correo">Correo electrónico</label>
                <input type="email" id="correo" name="correo"
                       required>
            </div>

            <button type="submit" class="boton">
                Verificar datos
            </button>
        </form>

    <% } else { %>

        <form action="<%= request.getContextPath() %>/recuperar-password"
              method="post">

            <input type="hidden" name="accion" value="cambiar">

            <div class="campo">
                <label for="password">Nueva contraseña</label>
                <input type="password" id="password" name="password"
                       minlength="6" autocomplete="new-password" required>
            </div>

            <div class="campo">
                <label for="confirmarPassword">Confirmar contraseña</label>
                <input type="password" id="confirmarPassword"
                       name="confirmarPassword"
                       minlength="6" autocomplete="new-password" required>
            </div>

            <button type="submit" class="boton">
                Cambiar contraseña
            </button>
        </form>

    <% } %>

    <a class="volver"
       href="<%= request.getContextPath() %>/login">
        Volver al inicio de sesión
    </a>
</main>


</body>
</html>
