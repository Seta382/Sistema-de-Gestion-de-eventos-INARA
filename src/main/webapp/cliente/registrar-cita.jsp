<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Agendar Cita - INARA</title>

    <!-- Fuentes de INARA -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@300;400;500;600;700&family=Playfair+Display:ital,wght@0,500;0,600;0,700&display=swap" rel="stylesheet">

    <!-- Bootstrap 5.3 (solo CSS) y luego los estilos propios del cliente -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/cliente.css">
</head>
<body class="page-agendar-cita">

<main class="container py-4 py-md-5">
    <div class="row justify-content-center">
        <div class="col-12 col-md-9 col-lg-7 col-xl-5">

            <div class="registro-card">

                <header class="text-center mb-4">
                    <h1 class="registro-title">INARA</h1>
                    <div class="golden-accent-bar"></div>
                    <h2 class="registro-heading">Agenda tu cita</h2>
                    <p class="registro-lead">Cuéntanos cuándo te queda bien y nuestro equipo te confirmará la reunión.</p>
                </header>

                <c:if test="${not empty error}">
                    <div class="alert alert-danger" role="alert">
                        <c:out value="${error}"/>
                    </div>
                </c:if>

                <form id="form-cita" action="${pageContext.request.contextPath}/citas" method="post">

                    <div class="row g-3">

                        <div class="col-12 col-sm-6">
                            <label for="fecha" class="form-label">Fecha <span class="req">*</span></label>
                            <input type="date" class="form-control" id="fecha" name="fecha"
                                   value="<c:out value='${param.fecha}'/>" required>
                        </div>

                        <div class="col-12 col-sm-6">
                            <label for="hora" class="form-label">Hora <span class="req">*</span></label>
                            <input type="time" class="form-control" id="hora" name="hora"
                                   value="<c:out value='${param.hora}'/>" required>
                        </div>

                        <div class="col-12">
                            <span class="form-label d-block" id="lbl-modalidad">Modalidad</span>
                            <div class="modalidad-switch" role="radiogroup" aria-labelledby="lbl-modalidad">
                                <input type="radio" class="btn-check" name="modalidad" id="mod-presencial"
                                       value="PRESENCIAL" autocomplete="off"
                                       <c:if test="${param.modalidad != 'VIRTUAL'}">checked</c:if>>
                                <label class="modalidad-opt" for="mod-presencial">Presencial</label>

                                <input type="radio" class="btn-check" name="modalidad" id="mod-virtual"
                                       value="VIRTUAL" autocomplete="off"
                                       <c:if test="${param.modalidad == 'VIRTUAL'}">checked</c:if>>
                                <label class="modalidad-opt" for="mod-virtual">Virtual</label>
                            </div>
                        </div>

                        <div class="col-12" id="grupo-lugar">
                            <label for="lugar" class="form-label">Lugar de la reunión</label>
                            <input type="text" class="form-control" id="lugar" name="lugar"
                                   value="<c:out value='${param.lugar}'/>"
                                   placeholder="Ej. Oficina INARA - San Isidro" autocomplete="off">
                        </div>

                        <div class="col-12">
                            <label for="motivo" class="form-label">Motivo o tipo de evento a tratar</label>
                            <input type="text" class="form-control" id="motivo" name="motivo"
                                   value="<c:out value='${param.motivo}'/>"
                                   placeholder="Ej. Cotización para boda en diciembre" autocomplete="off">
                            <div class="form-text">Tu cita quedará pendiente hasta que nuestro equipo la confirme.</div>
                        </div>

                        <div class="col-12 mt-4">
                            <button type="submit" class="btn btn-registro">Agendar cita</button>
                        </div>

                    </div>
                </form>

                <div class="registro-footer agendar-links">
                    <a href="${pageContext.request.contextPath}/citas">Volver a mis citas</a>
                    <a href="${pageContext.request.contextPath}/cliente/dashboard.jsp">&larr; Volver al dashboard</a>
                </div>

            </div>
        </div>
    </div>
</main>

<script>
(function () {
    var form = document.getElementById('form-cita');
    var fecha = document.getElementById('fecha');
    var grupoLugar = document.getElementById('grupo-lugar');
    var lugar = document.getElementById('lugar');
    var boton = form.querySelector('button[type="submit"]');

    // No permitir fechas pasadas
    var hoy = new Date();
    fecha.min = hoy.getFullYear() + '-' +
                String(hoy.getMonth() + 1).padStart(2, '0') + '-' +
                String(hoy.getDate()).padStart(2, '0');

    // El campo "Lugar" solo aplica a citas presenciales.
    // Se oculta (no se deshabilita) para que el formulario siga enviando "lugar" vacío, igual que antes.
    function actualizarModalidad() {
        var virtual = form.querySelector('input[name="modalidad"]:checked').value === 'VIRTUAL';
        grupoLugar.hidden = virtual;
        if (virtual) lugar.value = '';
    }
    form.querySelectorAll('input[name="modalidad"]').forEach(function (r) {
        r.addEventListener('change', actualizarModalidad);
    });
    actualizarModalidad();

    // Evita el doble envío (que crearía la cita dos veces)
    form.addEventListener('submit', function (e) {
        if (form.dataset.enviado === '1') { e.preventDefault(); return; }
        form.dataset.enviado = '1';
        boton.disabled = true;
        boton.textContent = 'Agendando...';
    });
    // Si el usuario vuelve atrás con el botón del navegador, se reactiva
    window.addEventListener('pageshow', function () {
        form.dataset.enviado = '';
        boton.disabled = false;
        boton.textContent = 'Agendar cita';
    });
})();
</script>
</body>
</html>

