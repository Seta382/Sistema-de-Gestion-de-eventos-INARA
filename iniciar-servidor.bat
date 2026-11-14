@echo off
setlocal EnableExtensions

echo ========================================================
echo   Iniciando Sistema de Gestion de Eventos INARA
echo ========================================================

rem --- La carpeta del proyecto es la de este .bat: funciona en cualquier computadora
set "PROJECT_DIR=%~dp0"
if "%PROJECT_DIR:~-1%"=="\" set "PROJECT_DIR=%PROJECT_DIR:~0,-1%"

rem --- Rutas propias de cada persona (JAVA_HOME, CATALINA_HOME...). Ese archivo no se sube a git.
if exist "%~dp0iniciar-config.bat" call "%~dp0iniciar-config.bat"

rem 1. Verificacion previa de lo necesario
if not defined JAVA_HOME goto ERR_JAVA
if not exist "%JAVA_HOME%\bin\java.exe" goto ERR_JAVA_RUTA
set "PATH=%JAVA_HOME%\bin;%PATH%"

if not defined CATALINA_HOME goto ERR_TOMCAT
if not exist "%CATALINA_HOME%\bin\catalina.bat" goto ERR_TOMCAT_RUTA

if defined MAVEN_CMD goto MVN_REVISAR
where mvn >nul 2>&1
if errorlevel 1 goto ERR_MVN
set "MAVEN_CMD=mvn"
goto MVN_LISTO
:MVN_REVISAR
if not exist "%MAVEN_CMD%" goto ERR_MVN_RUTA
:MVN_LISTO

if not exist "%PROJECT_DIR%\pom.xml" goto ERR_POM
if not exist "%PROJECT_DIR%\src\main\resources\application.properties" goto ERR_PROPS

echo [1/4] Compilando y empaquetando proyecto con Maven...
cd /d "%PROJECT_DIR%"
call "%MAVEN_CMD%" package -DskipTests
if errorlevel 1 goto ERR_COMPILACION

if not exist "%PROJECT_DIR%\target\gestion-eventos.war" goto ERR_WAR

echo [2/4] Copiando WAR a Tomcat webapps...
copy /Y "%PROJECT_DIR%\target\gestion-eventos.war" "%CATALINA_HOME%\webapps\gestion-eventos.war" > nul
if errorlevel 1 goto ERR_COPIA

rem Comprobar si Tomcat ya esta en ejecucion en el puerto 8080
netstat -ano | findstr /R /C:":8080 .*LISTENING" > nul
if not errorlevel 1 goto TOMCAT_YA_ACTIVO

echo [3/4] Iniciando Apache Tomcat en ventana independiente...
cd /d "%CATALINA_HOME%\bin"
start "Tomcat - INARA" cmd /k "catalina.bat run"
goto VERIFICAR_APP

:TOMCAT_YA_ACTIVO
echo.
echo [AVISO] El puerto 8080 ya esta activo. Tomcat parece estar en ejecucion.
echo Se omite iniciar una segunda instancia; Tomcat recargara el WAR nuevo.

:VERIFICAR_APP
echo [4/4] Esperando que la aplicacion responda en http://localhost:8080/gestion-eventos/ ...
set INTENTOS=0

:LOOP_ESPERA
ping -n 3 127.0.0.1 > nul
curl.exe -s -f -o nul "http://localhost:8080/gestion-eventos/"
if not errorlevel 1 goto SERVIDOR_LISTO

set /a INTENTOS+=1
if %INTENTOS% GEQ 20 goto ERROR_TIMEOUT

echo Esperando respuesta del servidor... (intento %INTENTOS% de 20)
goto LOOP_ESPERA

:ERROR_TIMEOUT
echo.
echo [ERROR] El servidor no respondio en http://localhost:8080/gestion-eventos/ tras 40 segundos.
echo Revisa la ventana de consola de Tomcat para verificar si ocurrio algun error.
pause
exit /b 1

:SERVIDOR_LISTO
echo.
echo ========================================================
echo   Aplicacion lista y respondiendo correctamente
echo ========================================================
echo Abriendo navegador en http://localhost:8080/gestion-eventos/ ...
echo (Para compartirla por internet ejecuta iniciar-tunel.bat)
start "" "http://localhost:8080/gestion-eventos/"
exit /b 0

rem ----------------------------- Errores -----------------------------

:ERR_JAVA
echo.
echo [ERROR] No esta definida la variable JAVA_HOME (carpeta del JDK, version 17 o superior).
goto AYUDA_CONFIG

:ERR_JAVA_RUTA
echo.
echo [ERROR] No se encontro Java en JAVA_HOME: %JAVA_HOME%
goto AYUDA_CONFIG

:ERR_TOMCAT
echo.
echo [ERROR] No esta definida la variable CATALINA_HOME (carpeta de Apache Tomcat 10).
goto AYUDA_CONFIG

:ERR_TOMCAT_RUTA
echo.
echo [ERROR] No se encontro Tomcat en CATALINA_HOME: %CATALINA_HOME%
goto AYUDA_CONFIG

:ERR_MVN
echo.
echo [ERROR] No se encontro Maven (mvn) en el PATH ni en MAVEN_CMD.
goto AYUDA_CONFIG

:ERR_MVN_RUTA
echo.
echo [ERROR] No se encontro Maven en MAVEN_CMD: %MAVEN_CMD%
goto AYUDA_CONFIG

:ERR_POM
echo.
echo [ERROR] No se encontro pom.xml en: %PROJECT_DIR%
echo Este .bat debe estar en la misma carpeta que el pom.xml.
pause
exit /b 1

:ERR_PROPS
echo.
echo [ERROR] Falta src\main\resources\application.properties (los datos de conexion a Supabase).
echo Copia application.properties.example con ese nombre y completa tus datos.
pause
exit /b 1

:ERR_COMPILACION
echo.
echo [ERROR] Fallo la compilacion con Maven.
pause
exit /b 1

:ERR_WAR
echo.
echo [ERROR] No se genero el archivo WAR en "%PROJECT_DIR%\target\gestion-eventos.war".
pause
exit /b 1

:ERR_COPIA
echo.
echo [ERROR] Fallo al copiar gestion-eventos.war hacia "%CATALINA_HOME%\webapps".
pause
exit /b 1

:AYUDA_CONFIG
echo.
echo Solucion: copia "iniciar-config.ejemplo.bat" con el nombre "iniciar-config.bat"
echo (en esta misma carpeta) y escribe las rutas de TU computadora.
pause
exit /b 1
