# Como ejecutar INARA en tu computadora

## Requisitos (una sola vez)
- JDK 17 o superior (el proyecto compila con Java 17).
- Apache Tomcat 10.1 (Jakarta EE 10), descomprimido en cualquier carpeta.
- Maven (`mvn`) en el PATH, o el que trae IntelliJ.
- Acceso a la base de Supabase (pide la contrasena al equipo; no esta en el repositorio).

## Configuracion (una sola vez)
1. Copia `src/main/resources/application.properties.example` como `src/main/resources/application.properties` y completa los datos de Supabase.
2. Copia `iniciar-config.ejemplo.bat` como `iniciar-config.bat` y escribe en el las rutas de TU computadora (`JAVA_HOME`, `CATALINA_HOME`, y `MAVEN_CMD` solo si `mvn` no funciona en una ventana normal).

Esos dos archivos son personales y estan en `.gitignore`: no se suben.

## Iniciar
- Doble clic en `iniciar-servidor.bat`: compila, copia el WAR a Tomcat, arranca y abre `http://localhost:8080/gestion-eventos/`.

## Compartir por internet (opcional, para demos)
1. Instala cloudflared una vez: `winget install Cloudflare.cloudflared` (cierra y abre la ventana despues).
2. Con el servidor ya iniciado, doble clic en `iniciar-tunel.bat`.
3. Busca el enlace `https://...trycloudflare.com` y compartelo **terminado en `/gestion-eventos/`**. El enlace cambia en cada ejecucion.
4. Mientras el tunel esta abierto, cualquiera con el enlace puede entrar: usalo solo para demos y cierralo al terminar.

## Si el tunel se queda en "Requesting new quick Tunnel"
La red no deja llegar a Cloudflare. Prueba otra red (datos del celular), desactiva la VPN o revisa antivirus y firewall.
