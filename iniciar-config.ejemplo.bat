rem =====================================================================
rem  iniciar-config.ejemplo.bat
rem  Copia este archivo con el nombre "iniciar-config.bat" (misma carpeta que
rem  iniciar-servidor.bat) y escribe las rutas de TU computadora.
rem  iniciar-config.bat NO se sube a git: cada persona tiene el suyo.
rem  No uses acentos ni comillas dobles dentro de las rutas.
rem =====================================================================

rem Carpeta del JDK (version 17 o superior)
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"

rem Carpeta de Apache Tomcat 10 (la que contiene bin y webapps)
set "CATALINA_HOME=C:\ruta\a\apache-tomcat-10.1.xx"

rem OPCIONAL: solo si "mvn" no funciona en una ventana de comandos normal.
rem IntelliJ trae uno; la ruta cambia con la version de IntelliJ.
rem set "MAVEN_CMD=C:\Program Files\JetBrains\IntelliJ IDEA XXXX\plugins\maven-plugin\lib\maven3\bin\mvn.cmd"

rem OPCIONAL: solo si cloudflared no esta en el PATH ni junto a los .bat
rem set "CLOUDFLARED_CMD=C:\ruta\a\cloudflared.exe"

rem OPCIONAL: protocolo del tunel. Por defecto http2. Prueba "quic" o "auto" si quieres cambiarlo.
rem set "CLOUDFLARED_PROTOCOL=http2"
