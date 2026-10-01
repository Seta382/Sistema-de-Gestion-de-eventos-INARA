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
    <style>
        :root {
            --bg-primary: #F8F4F1; --rose-dark: #A66A6A; --rose-deep: #743E3E;
            --gold: #C6A15B; --text-primary: #3D3333; --text-secondary: #706464;
            --white: #FFFFFF; --border-soft: #EFE8E2;
            --shadow-card: 0 14px 34px rgba(61, 51, 51, 0.07), 0 3px 10px rgba(198, 161, 91, 0.04);
        }
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: 'Montserrat', sans-serif; background-color: var(--bg-primary);
            min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 40px 20px; }
        .panel { background: var(--white); border-radius: 20px; padding: 40px; max-width: 560px; width: 100%;
            border: 1px solid var(--border-soft); box-shadow: var(--shadow-card); }
        .panel h1 { font-family: 'Playfair Display', Georgia, serif; font-size: 1.6rem; color: var(--text-primary); margin-bottom: 4px; }
        .panel .subtitle { font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 24px; }
        .form-row-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
        .field-group { display: flex; flex-direction: column; text-align: left; margin-bottom: 14px; }
        .field-group label { font-size: 0.8rem; font-weight: 600; color: var(--text-primary);
            margin-bottom: 6px; letter-spacing: 0.4px; text-transform: uppercase; }
        .field-group input {
            width: 100%; padding: 12px 14px; border: 1.5px solid var(--border-soft);
            border-radius: 10px; background-color: #FAF8F6; font-size: 0.9rem;
            color: var(--text-primary); font-family: 'Montserrat', sans-serif; outline: none;
        }
        .field-group input:focus { background-color: var(--white); border-color: var(--rose-dark);
            box-shadow: 0 0 0 3px rgba(216, 167, 167, 0.25); }
        .field-group input[readonly] { background-color: #F0EDEA; color: var(--text-secondary); }
        .btn-submit {
            width: 100%; background: linear-gradient(135deg, var(--gold) 0%, #A66A6A 50%, var(--rose-deep) 100%);
            color: var(--white); padding: 15px 24px; border: none; border-radius: 12px;
            font-size: 0.92rem; font-weight: 700; letter-spacing: 1.5px; text-transform: uppercase;
            cursor: pointer; margin-top: 10px;
        }
        .alert-danger { background: #FDF3F3; color: #923838; padding: 12px 16px; border-radius: 10px; font-size: 0.85rem; margin-bottom: 16px; }
        .link-back { display: block; text-align: center; margin-top: 18px; font-size: 0.85rem; color: var(--rose-dark); text-decoration: none; }
        @media (max-width: 560px) { .form-row-2 { grid-template-columns: 1fr; } }
    </style>
</head>
<body>

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
