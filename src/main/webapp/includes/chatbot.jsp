<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="pe.edu.eventos.dto.UsuarioDTO" %>
<%@ page import="pe.edu.eventos.util.Constantes" %>
<%
    UsuarioDTO usuarioChat = (UsuarioDTO) session.getAttribute(Constantes.SESION_USUARIO);
    String uNombre = (usuarioChat != null && usuarioChat.getNombre() != null) ? usuarioChat.getNombre().trim() : "";
    String uNombreCompleto = (usuarioChat != null && usuarioChat.getNombreCompleto() != null) ? usuarioChat.getNombreCompleto().trim() : "";
    String uRol = (usuarioChat != null && usuarioChat.getRol() != null) ? usuarioChat.getRol() : "";
    String uId = (usuarioChat != null) ? String.valueOf(usuarioChat.getId()) : "";
%>

<!-- Estilos del Chatbot INARA -->
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/chatbot.css?v=<%= System.currentTimeMillis() %>">

<!-- Teaser / Globo flotante de invitación -->
<div id="inaraChatTeaser" style="display: none;">
    <div>
        <strong>¿Tienes dudas sobre tu evento?</strong><br>
        ¡Pregúntame aquí! Soy Sofía, tu asesora virtual. ✨
    </div>
    <button type="button" class="teaser-close" aria-label="Cerrar aviso">&times;</button>
</div>

<!-- Botón Flotante (Launcher) -->
<button id="inaraChatLauncher" type="button" aria-label="Abrir Asistente Virtual INARA" title="Asistente Virtual INARA">
    <span id="inaraChatBadge">1</span>
    <!-- Icono Chat -->
    <svg class="icon-chat" viewBox="0 0 24 24" aria-hidden="true">
        <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-2 12H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z"/>
    </svg>
    <!-- Icono Cerrar -->
    <svg class="icon-close" viewBox="0 0 24 24" aria-hidden="true">
        <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
    </svg>
</button>

<!-- Ventana Principal del Chatbot -->
<div id="inaraChatWindow" role="dialog" aria-label="Chat con Sofía - Asistente INARA" aria-modal="true">

    <!-- Header -->
    <div class="inara-chat-header">
        <div class="inara-header-info">
            <div class="inara-avatar-wrap">
                <span class="avatar-letter">S</span>
                <span class="status-indicator" title="En línea"></span>
            </div>
            <div class="inara-title-group">
                <h3 class="chat-title">Sofía &bull; INARA</h3>
                <span class="chat-subtitle">
                    <span style="display: inline-block; width: 6px; height: 6px; background: #2ECC71; border-radius: 50%;"></span>
                    Asesora de Bodas & Eventos
                </span>
            </div>
        </div>
        <div class="inara-header-actions">
            <button type="button" id="inaraBtnReset" class="inara-btn-icon" title="Reiniciar conversación" aria-label="Reiniciar conversación">
                <svg viewBox="0 0 24 24"><path d="M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/></svg>
            </button>
            <button type="button" id="inaraBtnCerrar" class="inara-btn-icon" title="Cerrar chat" aria-label="Cerrar chat">
                <svg viewBox="0 0 24 24"><path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/></svg>
            </button>
        </div>
    </div>

    <!-- Contenedor de Mensajes -->
    <div id="inaraChatMessages" class="inara-chat-body">
        <!-- Los mensajes se renderizan dinámicamente -->
    </div>

    <!-- Indicador de "Escribiendo..." -->
    <div id="inaraChatTyping" class="inara-typing-indicator">
        <div class="inara-dot"></div>
        <div class="inara-dot"></div>
        <div class="inara-dot"></div>
        <span class="inara-typing-text">Sofía está escribiendo...</span>
    </div>

    <!-- Chips de Sugerencias Rápidas -->
    <div id="inaraChatChips" class="inara-chat-chips-wrap">
        <!-- Chips generados por JS -->
    </div>

    <!-- Footer / Formulario de Envío -->
    <div class="inara-chat-footer">
        <form id="inaraChatForm" class="inara-input-form" autocomplete="off">
            <input type="text" id="inaraChatInput" class="inara-chat-input" placeholder="Escribe tu consulta sobre eventos..." aria-label="Mensaje para el asistente" required maxlength="500">
            <button type="submit" class="inara-send-btn" aria-label="Enviar mensaje">
                <svg viewBox="0 0 24 24"><path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/></svg>
            </button>
        </form>
        <div class="inara-footer-tagline">INARA Luxury Events &bull; Asistente 24/7</div>
    </div>

</div>

<!-- Configuración y Script del Chatbot -->
<script>
    window.INARA_CHAT_CONFIG = {
        contextPath: '<%= request.getContextPath() %>',
        userName: '<%= uNombre.replace("'", "\\'") %>',
        userFullName: '<%= uNombreCompleto.replace("'", "\\'") %>',
        userRole: '<%= uRol.replace("'", "\\'") %>',
        userId: '<%= uId %>'
    };
</script>
<script src="${pageContext.request.contextPath}/js/chatbot.js?v=<%= System.currentTimeMillis() %>"></script>
