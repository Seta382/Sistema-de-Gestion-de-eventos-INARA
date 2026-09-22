package pe.edu.eventos.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pe.edu.eventos.dto.UsuarioDTO;
import pe.edu.eventos.util.Constantes;

import java.io.IOException;

/**
 * Filtro de seguridad para proteger rutas privadas y validar sesiones activas.
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {"/cliente/*", "/empleado/*", "/compras/*", "/articulos/*", "/proveedor/*"})
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Evitar caché de páginas protegidas
        httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        httpResponse.setHeader("Pragma", "no-cache");
        httpResponse.setDateHeader("Expires", 0);

        HttpSession session = httpRequest.getSession(false);
        boolean autenticado = (session != null && session.getAttribute(Constantes.SESION_USUARIO) != null);

        if (!autenticado) {
            // Usuario no autenticado intentando acceder a ruta protegida
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
            return;
        }

        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute(Constantes.SESION_USUARIO);
        String uri = httpRequest.getRequestURI();

        // Control de acceso basado en rol: /empleado/* restringido para roles administrativos
        if (uri.contains("/empleado/") || uri.contains("/compras/") || uri.contains("/articulos/") || uri.contains("/proveedor/")) {
            String rol = usuario.getRol() != null ? usuario.getRol().toUpperCase() : "";
            if (!Constantes.ROL_ADMIN.equals(rol) && !Constantes.ROL_EMPLEADO.equals(rol)) {
                // Cliente intentando entrar a zona administrativa -> redirigir a su portal
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/cliente/dashboard.jsp");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
