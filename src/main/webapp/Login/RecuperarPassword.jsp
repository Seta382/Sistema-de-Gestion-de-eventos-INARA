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
    <style>
        .recuperacion {
            box-sizing: border-box;
            max-width: 480px;
            width: 100%;
            margin: 50px auto;
            padding: 35px;
            background: #fff;
            border-radius: 20px;
            box-shadow: 0 10px 35px rgba(61, 51, 51, 0.10);
            font-family: 'Montserrat', sans-serif;
        }


        .recuperacion h1 {
            color: #7D4848;
            font-size: 1.7rem;
            margin-bottom: 10px;
        }

        .recuperacion p {
            color: #706464;
            margin-bottom: 22px;
            line-height: 1.5;
        }

        .campo {
            margin-bottom: 16px;
        }

        .campo label {
            display: block;
            margin-bottom: 7px;
            font-size: 0.85rem;
            font-weight: 600;
            color: #3D3333;
        }

        .campo input {
            width: 100%;
            padding: 12px;
            border: 1px solid #EFE8E2;
            border-radius: 10px;
            font-size: 0.95rem;
            box-sizing: border-box;
        }

        .boton {
            width: 100%;
            padding: 14px;
            border: none;
            border-radius: 10px;
            background: #A66A6A;
            color: white;
            font-weight: bold;
            cursor: pointer;
            margin-top: 8px;
        }

        .mensaje-error {
            padding: 12px;
            background: #FDF3F3;
            color: #923838;
            border-radius: 8px;
            margin-bottom: 18px;
        }

        .volver {
            display: block;
            text-align: center;
            margin-top: 20px;
            color: #7D4848;
            text-decoration: none;
            padding: 10px 0;
        }

        @media (max-width: 480px) {
            .recuperacion { margin: 16px auto; padding: 24px 18px; border-radius: 16px; }
            .recuperacion h1 { font-size: 1.4rem; overflow-wrap: anywhere; }
            .campo input { min-height: 46px; font-size: 16px; }
            .boton { min-height: 48px; font-size: 1rem; }
        }
    </style>


</head>
<body style="background:#F8F4F1; padding:20px 12px; box-sizing:border-box;">


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
