<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Login - INARA</title>
</head>
<body>

    <h1>INARA</h1>
    <h2>Iniciar Sesión</h2>

    <form action="LoginServlet" method="post">

        <label>Correo:</label>
        <input type="email" name="correo" required>

        <br><br>

        <label>Contraseña:</label>
        <input type="password" name="password" required>

        <br><br>

        <button type="submit">Ingresar</button>

    </form>

</body>
</html>
