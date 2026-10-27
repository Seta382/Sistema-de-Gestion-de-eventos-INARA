<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>INARA — Productora & Gestión de Eventos</title>
    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&family=Playfair+Display:ital,wght@0,500;0,600;0,700;1,400;1,600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css?v=<%= System.currentTimeMillis()%>">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login-paginas.css">
</head>
<body class="page-login">

<main class="portal-wrapper">
    <!-- Lado Izquierdo: Presentación INARA Eventos -->
    <section class="portal-showcase">
        <div class="showcase-header">
            <span class="showcase-badge">Gestión & Producción de Eventos</span>
            <h1 class="showcase-title">INARA</h1>
            <p class="showcase-subtitle">Momentos únicos, eventos inolvidables</p>
            <p class="showcase-description">
                Planificamos, coordinamos y materializamos experiencias extraordinarias con el más alto estándar de elegancia, diseño y exclusividad.
            </p>

            <div class="event-tags">
                <span class="event-tag">&#10024; Bodas & Galas</span>
                <span class="event-tag">&#127881; Quinceañeros</span>
                <span class="event-tag">&#127870; Corporativos</span>
                <span class="event-tag">&#127912; Ambientación</span>
            </div>
        </div>

        <footer class="showcase-footer">
            &copy; INARA Eventos — Excelencia y distinción en cada detalle
        </footer>
    </section>

    <!-- Lado Derecho: Formulario de Login -->
    <section class="portal-form-side">
        <div class="form-brand-header">
            <h2>Iniciar Sesión</h2>
            <div class="golden-accent-bar"></div>
            <p>Accede a tu panel para gestionar tus eventos y cotizaciones</p>
        </div>

        <%
            String registro = request.getParameter("registro");
            if ("exito".equalsIgnoreCase(registro)) {
        %>
        <div class="alert alert-success">
            <span>&#10003;</span> ¡Cuenta creada con éxito! Por favor ingresa con tus credenciales.
        </div>
        <%
            }
        %>

        <%
            if ("1".equals(request.getParameter("passwordActualizada"))) {
        %>
        <div class="alert alert-success">
            ¡Contraseña actualizada correctamente! Ya puedes iniciar sesión.
        </div>
        <%
            }
        %>

        <%
            String error = (String) request.getAttribute("error");
            if (error != null) {
        %>
        <div class="alert alert-danger">
            <span>&#9888;</span> <%= error%>
        </div>
        <%
            }
        %>

        <form action="${pageContext.request.contextPath}/login" method="post" class="login-form">
            <div class="form-group">
                <label for="correo">Correo Electrónico</label>
                <div class="input-wrapper">
                    <input type="email" id="correo" name="correo"
                           value="<%= request.getAttribute("correoIngresado") != null ? request.getAttribute("correoIngresado") : ""%>"
                           placeholder="ejemplo@inara.pe" required autofocus>
                </div>
            </div>

            <div class="form-group">
                <label for="password">Contraseña</label>
                <div class="input-wrapper">
                    <input type="password" id="password" name="password" placeholder="Tu contraseña" required>
                </div>
            </div>

            <div class="link-footer" style="margin-top: 0; text-align: right;">
                <a href="${pageContext.request.contextPath}/recuperar-password">
                    ¿Olvidaste tu contraseña?
                </a>
            </div>

            <button type="submit" class="btn-submit">Ingresar al Portal</button>

            <div class="link-footer">
                ¿Aún no tienes cuenta? <a href="${pageContext.request.contextPath}/registro">Regístrate aquí</a>
            </div>

            <div class="staff-access-pill">
                <button type="button" class="btn-staff-shortcut" onclick="autoFillStaff()">
                    <span>&#128188;</span> Acceso Staff Administrativo
                </button>
            </div>
        </form>
    </section>
</main>

<script src="${pageContext.request.contextPath}/js/login.js"></script>
<script>
    function autoFillStaff() {
        const correo = document.getElementById('correo');
        const pass = document.getElementById('password');
        if (correo && pass) {
            correo.value = 'admin@inara.pe';
            pass.value = 'AdminInara2026!';
            pass.focus();
        }
    }
</script>
</body>
</html>
