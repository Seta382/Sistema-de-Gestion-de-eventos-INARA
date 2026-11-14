<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="pe.edu.eventos.dto.UsuarioDTO" %>
<%@ page import="pe.edu.eventos.service.NotificacionService" %>
<%@ page import="pe.edu.eventos.util.Constantes" %>
<%--
  Campana de notificaciones con contador. Se incluye en la barra superior de cualquier pantalla:
      <jsp:include page="/includes/campana.jsp" />
  Trae su propio CSS. El número se actualiza solo cada minuto.
--%>
<%
    UsuarioDTO campanaUsuario = (UsuarioDTO) session.getAttribute(Constantes.SESION_USUARIO);
    int campanaNoLeidas = campanaUsuario == null ? 0 : new NotificacionService().contarNoLeidas(campanaUsuario.getId());
    String campanaCtx = request.getContextPath();
%>
<% if (campanaUsuario != null) { %>
<link rel="stylesheet" href="<%= campanaCtx %>/css/notificaciones.css">
<a class="campana" id="campana" href="<%= campanaCtx %>/notificaciones" title="Notificaciones" aria-label="Notificaciones">
    <span class="campana-icono" aria-hidden="true">&#128276;</span>
    <span class="campana-contador" id="campana-contador"<%= campanaNoLeidas > 0 ? "" : " hidden" %>><%= campanaNoLeidas > 99 ? "99+" : String.valueOf(campanaNoLeidas) %></span>
</a>
<script>
    (function () {
        var contador = document.getElementById('campana-contador');
        if (!contador || !window.fetch) return;
        function actualizar() {
            fetch('<%= campanaCtx %>/notificaciones?accion=contar', { credentials: 'same-origin', cache: 'no-store' })
                .then(function (r) { return r.ok ? r.json() : null; })
                .then(function (d) {
                    if (!d) return;
                    contador.textContent = d.noLeidas > 99 ? '99+' : d.noLeidas;
                    contador.hidden = !(d.noLeidas > 0);
                })
                .catch(function () { /* sin conexión: se deja el número anterior */ });
        }
        setInterval(function () { if (!document.hidden) actualizar(); }, 60000);
    })();
</script>
<% } %>
