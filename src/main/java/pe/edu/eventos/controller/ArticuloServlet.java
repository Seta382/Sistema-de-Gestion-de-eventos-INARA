package pe.edu.eventos.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pe.edu.eventos.dao.ArticuloDAO;
import pe.edu.eventos.dto.UsuarioDTO;
import pe.edu.eventos.model.Articulo;
import pe.edu.eventos.model.Proveedor;
import pe.edu.eventos.service.ArticuloService;
import pe.edu.eventos.service.ProveedorService;
import pe.edu.eventos.util.BusquedaUtil;
import pe.edu.eventos.util.Constantes;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel de Insumos y Proveedores (una sola página con dos pestañas).
 * GET  /articulo?tab=insumos|proveedores&q=texto&filtro=ACTIVO|INACTIVO|STOCK_BAJO|TODOS
 * GET  /articulo?accion=editar&id=N  (abre el panel de edición de un insumo)
 * POST accion = registrar | actualizar | registrarProveedor | baja | reactivar
 * Solo para ADMIN y EMPLEADO.
 */
@WebServlet("/articulo")
public class ArticuloServlet extends HttpServlet {

    private static final String TAB_INSUMOS = "insumos";
    private static final String TAB_PROVEEDORES = "proveedores";
    private static final String VISTA = "/articulos/registro.jsp";

    private static final String FLASH_EXITO = "flashExito";
    private static final String FLASH_ERROR = "flashError";

    private final ArticuloService articuloService = new ArticuloService();
    private final ProveedorService proveedorService = new ProveedorService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!esStaff(request, response)) return;

        pasarMensajesFlashALaVista(request);

        // accion=editar abre el panel de edición del insumo; cualquier otra
        // (incluida "registro", que usa el dashboard) muestra el panel normal
        if ("editar".equals(request.getParameter("accion"))) {
            Integer id = leerId(request.getParameter("id"));
            Articulo articulo = id != null ? articuloService.buscarPorId(id) : null;
            if (articulo == null) {
                setFlash(request, FLASH_ERROR, "El insumo que intentas editar no existe.");
                redirigir(request, response, TAB_INSUMOS,
                        leerFiltro(request), BusquedaUtil.normalizar(request.getParameter("q")));
                return;
            }
            request.setAttribute("articuloEdit", articulo);
        }

        mostrarPagina(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!esStaff(request, response)) return;

        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");
        if (accion == null) accion = "";

        switch (accion) {
            case "registrar":
                registrarArticulo(request, response);
                break;
            case "actualizar":
                actualizarArticulo(request, response);
                break;
            case "registrarProveedor":
                registrarProveedor(request, response);
                break;
            case "baja":
            case "reactivar":
                cambiarEstado(request, response, accion);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/articulo");
                break;
        }
    }

    // ---------------------------------------------------------------- acciones

    private void registrarArticulo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Map<String, String> form = new HashMap<>();
        form.put("nombre", request.getParameter("nombre"));
        form.put("descripcion", request.getParameter("descripcion"));
        form.put("precio", request.getParameter("precio"));
        form.put("stock", request.getParameter("stock"));
        form.put("idProveedor", request.getParameter("idProveedor"));

        String error;
        String nombreRegistrado = null;
        try {
            Articulo articulo = leerArticulo(request);
            error = articuloService.registrar(articulo);
            nombreRegistrado = articulo.getNombre();
        } catch (IllegalArgumentException e) {
            error = e.getMessage();
        }

        if (error != null) {
            // Se vuelve a mostrar la página con lo que escribió el usuario (sin redirigir)
            request.setAttribute("error", error);
            request.setAttribute("form", form);
            request.setAttribute("abrirForm", "insumo");
            mostrarPagina(request, response);
            return;
        }

        setFlash(request, FLASH_EXITO, "Insumo «" + nombreRegistrado + "» registrado correctamente. "
                + "Estará disponible al registrar eventos mientras tenga stock.");
        // Se muestra la lista de activos sin búsqueda para que se vea el insumo nuevo
        redirigir(request, response, TAB_INSUMOS, "ACTIVO", "");
    }

    private void actualizarArticulo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Integer id = leerId(request.getParameter("id"));

        Map<String, String> form = new HashMap<>();
        form.put("nombre", request.getParameter("nombre"));
        form.put("descripcion", request.getParameter("descripcion"));
        form.put("precio", request.getParameter("precio"));
        form.put("stock", request.getParameter("stock"));
        form.put("idProveedor", request.getParameter("idProveedor"));
        form.put("stockAnterior", request.getParameter("stockAnterior"));

        String error;
        String nombreGuardado = null;
        try {
            if (id == null) {
                throw new IllegalArgumentException("No se indicó qué insumo editar.");
            }
            Articulo articulo = leerArticulo(request);
            articulo.setIdArticulo(id);
            Integer stockAnterior = leerEntero(request.getParameter("stockAnterior"), "El stock anterior");

            error = articuloService.actualizar(articulo, stockAnterior);
            nombreGuardado = articulo.getNombre();
        } catch (IllegalArgumentException e) {
            error = e.getMessage();
        }

        if (error != null) {
            Articulo actual = id != null ? articuloService.buscarPorId(id) : null;
            if (actual == null) {
                setFlash(request, FLASH_ERROR, "El insumo que intentas editar ya no existe.");
                redirigir(request, response, TAB_INSUMOS,
                        leerFiltro(request), BusquedaUtil.normalizar(request.getParameter("q")));
                return;
            }
            // Si el stock cambió en la base, el próximo guardado debe compararse contra el valor actual
            if (ArticuloService.ERROR_STOCK_CAMBIO.equals(error)) {
                form.put("stockAnterior", String.valueOf(actual.getStock()));
            }
            request.setAttribute("error", error);
            request.setAttribute("formEdit", form);
            request.setAttribute("articuloEdit", actual);
            mostrarPagina(request, response);
            return;
        }

        setFlash(request, FLASH_EXITO, "Insumo «" + nombreGuardado + "» actualizado correctamente.");
        // Se vuelve a la lista con el mismo filtro y búsqueda
        redirigir(request, response, TAB_INSUMOS,
                leerFiltro(request), BusquedaUtil.normalizar(request.getParameter("q")));
    }

    private void registrarProveedor(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Map<String, String> form = new HashMap<>();
        form.put("razonSocial", request.getParameter("razonSocial"));
        form.put("ruc", request.getParameter("ruc"));
        form.put("correo", request.getParameter("correo"));
        form.put("telefono", request.getParameter("telefono"));
        form.put("direccion", request.getParameter("direccion"));

        Proveedor proveedor = new Proveedor(
                request.getParameter("razonSocial"),
                request.getParameter("ruc"),
                request.getParameter("correo"),
                request.getParameter("telefono"),
                request.getParameter("direccion"));

        String error = proveedorService.registrar(proveedor);

        if (error != null) {
            request.setAttribute("error", error);
            request.setAttribute("form", form);
            request.setAttribute("abrirForm", "proveedor");
            mostrarPagina(request, response);
            return;
        }

        setFlash(request, FLASH_EXITO, "Proveedor «" + proveedor.getRazonSocial() + "» registrado correctamente. "
                + "Ya puedes asignarlo a un insumo desde la pestaña Insumos.");
        redirigir(request, response, TAB_PROVEEDORES, "", "");
    }

    private void cambiarEstado(HttpServletRequest request, HttpServletResponse response, String accion)
            throws IOException {

        Integer id = leerId(request.getParameter("id"));
        Articulo articulo = id != null ? articuloService.buscarPorId(id) : null;

        if (articulo == null) {
            setFlash(request, FLASH_ERROR, "El insumo indicado no existe.");
        } else if ("baja".equals(accion)) {
            if (articuloService.darDeBaja(id)) {
                setFlash(request, FLASH_EXITO, "«" + articulo.getNombre() + "» se dio de baja. "
                        + "Ya no aparecerá al registrar eventos; puedes reactivarlo cuando quieras.");
            } else {
                setFlash(request, FLASH_ERROR, "No se pudo dar de baja el insumo. Intenta nuevamente.");
            }
        } else {
            if (articuloService.reactivar(id)) {
                setFlash(request, FLASH_EXITO, "«" + articulo.getNombre() + "» fue reactivado.");
            } else {
                setFlash(request, FLASH_ERROR, "No se pudo reactivar el insumo. Intenta nuevamente.");
            }
        }

        // Se vuelve al mismo punto de la lista (mismo filtro y búsqueda)
        redirigir(request, response, TAB_INSUMOS,
                leerFiltro(request), BusquedaUtil.normalizar(request.getParameter("q")));
    }

    // ------------------------------------------------------------------ vista

    /** Carga los datos según pestaña, búsqueda y filtro, y muestra la página. */
    private void mostrarPagina(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String tab = TAB_PROVEEDORES.equalsIgnoreCase(request.getParameter("tab")) ? TAB_PROVEEDORES : TAB_INSUMOS;
        String q = BusquedaUtil.normalizar(request.getParameter("q"));
        String filtro = leerFiltro(request);

        request.setAttribute("tab", tab);
        request.setAttribute("busqueda", q);
        request.setAttribute("filtro", filtro);
        // Fragmento ya codificado para armar los links de pestañas, filtros y acciones en la vista
        request.setAttribute("qParamHtml", q.isEmpty() ? "" : "&amp;q=" + codificar(q));
        request.setAttribute("umbralStockBajo", ArticuloDAO.UMBRAL_STOCK_BAJO);

        if (TAB_INSUMOS.equals(tab)) {
            request.setAttribute("articulos", articuloService.buscar(q, filtro));
            List<Proveedor> activos = proveedorService.listarActivos();
            request.setAttribute("proveedoresActivos", activos);

            // Panel de edición: el proveedor actual del insumo debe poder verse aunque ya esté inactivo
            Articulo enEdicion = (Articulo) request.getAttribute("articuloEdit");
            if (enEdicion != null) {
                List<Proveedor> opciones = new ArrayList<>(activos);
                Integer idProv = enEdicion.getIdProveedor();
                if (idProv != null && idProv > 0) {
                    boolean yaEsta = false;
                    for (Proveedor p : opciones) {
                        if (idProv.equals(p.getIdProveedor())) {
                            yaEsta = true;
                            break;
                        }
                    }
                    if (!yaEsta) {
                        Proveedor actual = proveedorService.buscarPorId(idProv);
                        if (actual != null) opciones.add(actual);
                    }
                }
                request.setAttribute("proveedoresEdicion", opciones);
            }
        } else {
            request.setAttribute("proveedores", proveedorService.buscar(q));
            // El enlace "Agrégalo en Proveedores" del formulario de insumos llega con ?nuevo=1
            if (request.getAttribute("abrirForm") == null && "1".equals(request.getParameter("nuevo"))) {
                request.setAttribute("abrirForm", "proveedor");
            }
        }

        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    // ---------------------------------------------------------------- helpers

    private Articulo leerArticulo(HttpServletRequest request) {
        BigDecimal precio = leerDecimal(request.getParameter("precio"), "El precio");
        Integer stock = leerEntero(request.getParameter("stock"), "El stock");

        Integer idProveedor = null;
        String idProvTxt = request.getParameter("idProveedor");
        if (idProvTxt != null && !idProvTxt.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(idProvTxt.trim());
                idProveedor = id > 0 ? id : null; // 0 = "-- Seleccionar proveedor --"
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("El proveedor seleccionado no es válido.");
            }
        }

        return new Articulo(
                request.getParameter("nombre"),
                request.getParameter("descripcion"),
                precio,
                stock,
                idProveedor);
    }

    private BigDecimal leerDecimal(String texto, String etiqueta) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException(etiqueta + " es obligatorio.");
        }
        try {
            return new BigDecimal(texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(etiqueta + " no es un número válido.");
        }
    }

    private Integer leerEntero(String texto, String etiqueta) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException(etiqueta + " es obligatorio.");
        }
        try {
            return Integer.valueOf(texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(etiqueta + " debe ser un número entero.");
        }
    }

    private Integer leerId(String texto) {
        try {
            return Integer.valueOf(texto);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** ACTIVO (por defecto), INACTIVO, STOCK_BAJO o TODOS. */
    private String leerFiltro(HttpServletRequest request) {
        String f = request.getParameter("filtro");
        if (f == null) return "ACTIVO";
        f = f.trim().toUpperCase();
        switch (f) {
            case "ACTIVO":
            case "INACTIVO":
            case "STOCK_BAJO":
            case "TODOS":
                return f;
            default:
                return "ACTIVO";
        }
    }

    private void redirigir(HttpServletRequest request, HttpServletResponse response,
                           String tab, String filtro, String q) throws IOException {
        StringBuilder url = new StringBuilder(request.getContextPath() + "/articulo?tab=" + tab);
        if (filtro != null && !filtro.isEmpty()) {
            url.append("&filtro=").append(codificar(filtro));
        }
        if (q != null && !q.isEmpty()) {
            url.append("&q=").append(codificar(q));
        }
        response.sendRedirect(url.toString());
    }

    private String codificar(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    // Mensajes de una sola vez (sobreviven a la redirección, y no viajan en la URL)
    private void setFlash(HttpServletRequest request, String clave, String mensaje) {
        request.getSession().setAttribute(clave, mensaje);
    }

    private void pasarMensajesFlashALaVista(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return;

        Object exito = session.getAttribute(FLASH_EXITO);
        if (exito != null) {
            request.setAttribute("exito", exito);
            session.removeAttribute(FLASH_EXITO);
        }
        Object error = session.getAttribute(FLASH_ERROR);
        if (error != null) {
            request.setAttribute("error", error);
            session.removeAttribute(FLASH_ERROR);
        }
    }

    private boolean esStaff(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        UsuarioDTO usuario = session != null ? (UsuarioDTO) session.getAttribute(Constantes.SESION_USUARIO) : null;

        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        String rol = usuario.getRol() != null ? usuario.getRol().toUpperCase() : "";
        if (!Constantes.ROL_ADMIN.equals(rol) && !Constantes.ROL_EMPLEADO.equals(rol)) {
            response.sendRedirect(request.getContextPath() + "/cliente/dashboard.jsp");
            return false;
        }
        return true;
    }
}
