@echo off
setlocal EnableExtensions
title INARA - Cloudflare Public Tunnel

echo ========================================================
echo   Iniciando Tunel Publico HTTPS para INARA Eventos
echo ========================================================
echo.

rem --- Rutas propias de cada persona (opcional). Ese archivo no se sube a git.
if exist "%~dp0iniciar-config.bat" call "%~dp0iniciar-config.bat"

rem --- Buscar cloudflared: 1) CLOUDFLARED_CMD del config  2) junto a este .bat  3) en el PATH
if defined CLOUDFLARED_CMD goto CF_REVISAR
if exist "%~dp0cloudflared.exe" goto CF_LOCAL
where cloudflared >nul 2>&1
if errorlevel 1 goto ERR_CLOUDFLARED
set "CLOUDFLARED_CMD=cloudflared"
goto CF_LISTO
:CF_LOCAL
set "CLOUDFLARED_CMD=%~dp0cloudflared.exe"
goto CF_LISTO
:CF_REVISAR
if not exist "%CLOUDFLARED_CMD%" goto ERR_CLOUDFLARED_RUTA
:CF_LISTO

rem --- http2 funciona en casi todas las redes; el modo por defecto (QUIC/UDP) suele estar bloqueado
if not defined CLOUDFLARED_PROTOCOL set "CLOUDFLARED_PROTOCOL=http2"

echo Comprobando Tomcat en el puerto 8080...
netstat -ano | findstr /R /C:":8080 .*LISTENING" > nul
if not errorlevel 1 goto TOMCAT_OK
echo [ADVERTENCIA] No se detecto Tomcat en el puerto 8080.
echo Ejecuta primero iniciar-servidor.bat (o inicia Tomcat desde IntelliJ).
echo.
:TOMCAT_OK

echo Comprobando que esta red llega a Cloudflare...
curl.exe -s -o nul -m 10 https://api.trycloudflare.com
if not errorlevel 1 goto RED_OK
echo [ADVERTENCIA] No se pudo contactar con api.trycloudflare.com desde esta red.
echo Si el tunel se queda en "Requesting new quick Tunnel", prueba con otra red
echo (por ejemplo los datos del celular), sin VPN, o revisa antivirus y firewall.
echo.
:RED_OK

echo Iniciando Cloudflare Quick Tunnel hacia http://localhost:8080 (protocolo %CLOUDFLARED_PROTOCOL%) ...
echo.
echo El enlace HTTPS aparecera en pantalla (busca https://...trycloudflare.com).
echo IMPORTANTE: la aplicacion esta en /gestion-eventos/, asi que comparte el enlace asi:
echo     https://ESE-ENLACE.trycloudflare.com/gestion-eventos/
echo El enlace cambia cada vez que se inicia el tunel. Cierra esta ventana para detenerlo.
echo.
"%CLOUDFLARED_CMD%" tunnel --protocol %CLOUDFLARED_PROTOCOL% --url http://localhost:8080
echo.
echo El tunel se cerro. Si fue por un error, copia el mensaje de arriba.
pause
exit /b 0

:ERR_CLOUDFLARED
echo [ERROR] No se encontro cloudflared.
echo Instalalo con:  winget install Cloudflare.cloudflared
echo Luego cierra y vuelve a abrir esta ventana. (O pon cloudflared.exe junto a este .bat.)
pause
exit /b 1

:ERR_CLOUDFLARED_RUTA
echo [ERROR] No se encontro cloudflared en CLOUDFLARED_CMD: %CLOUDFLARED_CMD%
pause
exit /b 1
