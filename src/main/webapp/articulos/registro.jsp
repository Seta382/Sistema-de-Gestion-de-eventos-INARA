<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="pe.edu.eventos.dto.UsuarioDTO" %>
<%@ page import="pe.edu.eventos.util.Constantes" %>
<%@ page import="pe.edu.eventos.model.Proveedor" %>
<%@ page import="java.util.List" %>

<%
    UsuarioDTO usuario = (UsuarioDTO) session.getAttribute(Constantes.SESION_USUARIO);

    if (usuario == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }

    String rol = usuario.getRol() != null
            ? usuario.getRol().toUpperCase()
            : "";

    if (!Constantes.ROL_ADMIN.equals(rol)
            && !Constantes.ROL_EMPLEADO.equals(rol)) {

        response.sendRedirect(
                request.getContextPath() + "/cliente/dashboard.jsp"
        );
        return;
    }

    List<Proveedor> proveedores =
            (List<Proveedor>) request.getAttribute("proveedores");

    if (proveedores == null) {
        response.sendRedirect(
                request.getContextPath() + "/articulo?accion=registro"
        );
        return;
    }

    String exito = request.getParameter("exito");
    String error = request.getParameter("error");
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>INARA — Registrar Artículo</title>

    <style>

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: Arial, sans-serif;
            background: #F8F4F1;
            color: #3D3333;
            min-height: 100vh;
        }

        .navbar {
            background: white;
            padding: 18px 40px;
            border-bottom: 1px solid #EFE8E2;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .logo {
            font-size: 1.7rem;
            font-weight: bold;
            letter-spacing: 3px;
            color: #A66A6A;
        }

        .btn-volver {
            text-decoration: none;
            color: #A66A6A;
            border: 1px solid #D8A7A7;
            padding: 8px 16px;
            border-radius: 20px;
            font-size: 0.85rem;
        }

        .contenedor {
            max-width: 700px;
            margin: 45px auto;
            padding: 0 20px;
        }

        .card {
            background: white;
            padding: 35px;
            border-radius: 18px;
            box-shadow: 0 8px 25px rgba(0,0,0,0.06);
        }

        h1 {
            margin-bottom: 8px;
            color: #3D3333;
        }

        .subtitulo {
            color: #706464;
            margin-bottom: 28px;
            font-size: 0.9rem;
        }

        .campo {
            margin-bottom: 18px;
        }

        label {
            display: block;
            margin-bottom: 7px;
            font-weight: bold;
            font-size: 0.85rem;
        }

        input,
        textarea,
        select {
            width: 100%;
            padding: 12px;
            border: 1px solid #EFE8E2;
            border-radius: 9px;
            font-size: 0.9rem;
        }

        textarea {
            min-height: 100px;
            resize: vertical;
        }

        .fila {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 15px;
        }

        .btn-registrar {
            width: 100%;
            margin-top: 10px;
            padding: 14px;
            border: none;
            border-radius: 10px;
            background: linear-gradient(135deg, #C6A15B, #A66A6A);
            color: white;
            font-weight: bold;
            cursor: pointer;
            font-size: 0.9rem;
        }

        .mensaje {
            padding: 12px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-size: 0.85rem;
        }

        .exito {
            background: #E8F5E9;
            color: #2E7D32;
        }

        .error {
            background: #FFEBEE;
            color: #C62828;
        }

        @media (max-width: 600px) {
            .fila {
                grid-template-columns: 1fr;
            }

            .card {
                padding: 25px;
            }

            .navbar {
                padding: 15px 20px;
            }
        }

    </style>
</head>

<body>

<header class="navbar">

    <div class="logo">
        INARA
    </div>

    <a href="<%= request.getContextPath() %>/evento"
       class="btn-volver">
        ← Volver al Centro de Control
    </a>

</header>

<main class="contenedor">

    <div class="card">

        <h1>Registrar Artículo</h1>

        <p class="subtitulo">
            Registra un nuevo artículo o insumo disponible para los eventos.
        </p>

        <% if ("1".equals(exito)) { %>

            <div class="mensaje exito">
                ✓ Artículo registrado correctamente.
                Ahora estará disponible para seleccionar al registrar un evento,
                siempre que tenga stock disponible.
            </div>

        <% } %>

        <% if (error != null) { %>

            <div class="mensaje error">
                No se pudo registrar el artículo.
                <% if (!"1".equals(error)) { %>
                    <br><%= error %>
                <% } %>
            </div>

        <% } %>

        <form action="<%= request.getContextPath() %>/articulo"
              method="post">

            <div class="campo">

                <label for="nombre">
                    Nombre del artículo *
                </label>

                <input type="text"
                       id="nombre"
                       name="nombre"
                       placeholder="Ej: Mesa redonda"
                       required>

            </div>

            <div class="campo">

                <label for="descripcion">
                    Descripción
                </label>

                <textarea id="descripcion"
                          name="descripcion"
                          placeholder="Descripción del artículo..."></textarea>

            </div>

            <div class="fila">

                <div class="campo">

                    <label for="precio">
                        Precio *
                    </label>

                    <input type="number"
                           id="precio"
                           name="precio"
                           min="0"
                           step="0.01"
                           placeholder="0.00"
                           required>

                </div>

                <div class="campo">

                    <label for="stock">
                        Stock *
                    </label>

                    <input type="number"
                           id="stock"
                           name="stock"
                           min="0"
                           placeholder="0"
                           required>

                </div>

            </div>

            <div class="campo">

                <label for="idProveedor">
                    Proveedor
                </label>

                <select id="idProveedor"
                        name="idProveedor">

                    <option value="0">
                        -- Seleccionar proveedor --
                    </option>

                    <% for (Proveedor proveedor : proveedores) { %>

                        <option value="<%= proveedor.getIdProveedor() %>">
                            <%= proveedor.getRazonSocial() %>
                            - RUC: <%= proveedor.getRuc() %>
                        </option>

                    <% } %>

                </select>

            </div>

            <button type="submit"
                    class="btn-registrar">
                REGISTRAR ARTÍCULO
            </button>

        </form>

    </div>

</main>

</body>
</html>
