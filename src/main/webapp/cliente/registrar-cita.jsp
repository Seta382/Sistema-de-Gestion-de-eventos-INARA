<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Agendar Cita - INARA</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css">
    <style>
        .form-row { display: flex; gap: 12px; }
        .form-row .form-group { flex: 1; }
    </style>
</head>
<body>

<div class="login-container" style="max-width: 480px;">
    <div class="login-header">
        <h1>INARA</h1>
        <p>Agendar una cita</p>
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

    <form action="${pageContext.request.contextPath}/citas" method="post" class="login-form">

        <div class="form-row">
            <div class="form-group">
                <label for="fecha">Fecha: *</label>
                <input type="date" id="fecha" name="fecha" required>
            </div>
            <div class="form-group">
                <label for="hora">Hora: *</label>
                <input type="time" id="hora" name="hora" required>
            </div>
        </div>

        <div class="form-group">
            <label for="modalidad">Modalidad:</label>
            <select id="modalidad" name="modalidad">
                <option value="PRESENCIAL">Presencial</option>
                <option value="VIRTUAL">Virtual</option>
            </select>
        </div>

        <div class="form-group">
            <label for="lugar">Lugar (si es presencial):</label>
            <input type="text" id="lugar" name="lugar" placeholder="Ej. Oficina INARA - San Isidro">
        </div>

        <div class="form-group">
            <label for="motivo">Motivo / tipo de evento a tratar:</label>
            <input type="text" id="motivo" name="motivo" placeholder="Ej. Cotización para boda en diciembre">
        </div>

        <button type="submit" class="btn-submit">Agendar cita</button>

        <div class="link-footer">
            <a href="${pageContext.request.contextPath}/citas">Volver a mis citas</a>
        </div>
    </form>
</div>

</body>
</html>
