<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Crear Cuenta - INARA</title>

    <!-- Fuentes de INARA -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&family=Playfair+Display:ital,wght@0,500;0,600;0,700&display=swap" rel="stylesheet">

    <!-- Bootstrap 5.3 (solo CSS) y luego los estilos propios -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/cliente.css">
</head>
<body class="page-registro-cliente">

<main class="container py-4 py-md-5">
    <div class="row justify-content-center">
        <div class="col-12 col-md-10 col-lg-8 col-xl-6">

            <div class="registro-card">

                <header class="text-center mb-4">
                    <h1 class="registro-title">INARA</h1>
                    <div class="golden-accent-bar"></div>
                    <h2 class="registro-heading">Crea tu cuenta</h2>
                    <p class="registro-lead">Agenda citas y sigue el avance de tus eventos desde tu portal.</p>
                </header>

                <c:if test="${not empty error}">
                    <div class="alert alert-danger" role="alert">
                        <c:out value="${error}"/>
                    </div>
                </c:if>

                <form action="${pageContext.request.contextPath}/registro" method="post" class="login-form">
                    <input type="hidden" name="accion" value="registrar">

                    <div class="row g-3">

                        <div class="col-12 col-sm-6">
                            <label for="nombre" class="form-label">Nombre <span class="req">*</span></label>
                            <input type="text" class="form-control" id="nombre" name="nombre"
                                   value="<c:out value='${nombre}'/>"
                                   placeholder="Ej. Carlos" autocomplete="given-name" required autofocus>
                        </div>

                        <div class="col-12 col-sm-6">
                            <label for="apellido" class="form-label">Apellido <span class="req">*</span></label>
                            <input type="text" class="form-control" id="apellido" name="apellido"
                                   value="<c:out value='${apellido}'/>"
                                   placeholder="Ej. Mendoza" autocomplete="family-name" required>
                        </div>

                        <div class="col-12">
                            <label for="correo" class="form-label">Correo electrónico <span class="req">*</span></label>
                            <input type="email" class="form-control" id="correo" name="correo"
                                   value="<c:out value='${correo}'/>"
                                   placeholder="carlos@correo.com" autocomplete="email" required>
                        </div>

                        <div class="col-12">
                            <label for="telefono" class="form-label">Teléfono / WhatsApp</label>
                            <input type="tel" class="form-control" id="telefono" name="telefono"
                                   value="<c:out value='${telefono}'/>"
                                   placeholder="Ej. 987654321" autocomplete="tel">
                        </div>

                        <div class="col-12 col-sm-5">
                            <label for="dni" class="form-label">DNI</label>
                            <input type="text" class="form-control" id="dni" name="dni"
                                   value="<c:out value='${dni}'/>"
                                   placeholder="Ej. 12345678" inputmode="numeric" autocomplete="off">
                        </div>

                        <div class="col-12 col-sm-7">
                            <label for="direccion" class="form-label">Dirección</label>
                            <input type="text" class="form-control" id="direccion" name="direccion"
                                   value="<c:out value='${direccion}'/>"
                                   placeholder="Ej. Av. Larco 123" autocomplete="street-address">
                        </div>

                        <div class="col-12">
                            <label for="password" class="form-label">Contraseña <span class="req">*</span></label>
                            <input type="password" class="form-control" id="password" name="password"
                                   placeholder="Mínimo 6 caracteres" autocomplete="new-password" required>
                        </div>

                        <div class="col-12 mt-4">
                            <button type="submit" class="btn btn-registro">Crear cuenta</button>
                        </div>

                    </div>
                </form>

                <p class="registro-footer">
                    ¿Ya tienes una cuenta?
                    <a href="${pageContext.request.contextPath}/login">Inicia sesión aquí</a>
                </p>

            </div>
        </div>
    </div>
</main>
<script>
    const form = document.querySelector('form.login-form');
    form.addEventListener('submit', function (e) {
        if (form.dataset.enviado === '1') { e.preventDefault(); return; }
        form.dataset.enviado = '1';
        const btn = form.querySelector('button[type="submit"]');
        btn.disabled = true;
        btn.textContent = 'Creando cuenta...';
    });
    // Si el usuario vuelve atrás, el formulario vuelve a estar disponible
    window.addEventListener('pageshow', function () {
        form.dataset.enviado = '';
        const btn = form.querySelector('button[type="submit"]');
        btn.disabled = false;
        btn.textContent = 'Crear cuenta';
    });
</script>

<script src="${pageContext.request.contextPath}/js/login.js"></script>
</body>
</html>
