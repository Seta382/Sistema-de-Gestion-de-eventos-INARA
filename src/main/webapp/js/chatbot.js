/**
 * chatbot.js — Motor interactivo del Asistente Virtual INARA ("Sofía")
 * Soporta conexión asíncrona con el backend Servlet, persistencia de sesión,
 * animaciones fluidas, chips contextuales y fallback automático.
 */

(function () {
    'use strict';

    // Configuración inicial leída desde el JSP
    const cfg = window.INARA_CHAT_CONFIG || {};
    const contextPath = cfg.contextPath || '';
    const userName = cfg.userName || '';
    const userFullName = cfg.userFullName || '';
    const userId = cfg.userId || '';

    const STORAGE_KEY_HISTORIAL = 'inara_chat_history_v1';
    const STORAGE_KEY_TEASER_DISMISSED = 'inara_chat_teaser_dismissed';

    // Elementos del DOM
    let launcher, chatWindow, teaser, badge, messagesContainer, typingIndicator, chipsContainer, inputForm, textInput;

    document.addEventListener('DOMContentLoaded', initChatbot);

    function initChatbot() {
        launcher = document.getElementById('inaraChatLauncher');
        chatWindow = document.getElementById('inaraChatWindow');
        teaser = document.getElementById('inaraChatTeaser');
        badge = document.getElementById('inaraChatBadge');
        messagesContainer = document.getElementById('inaraChatMessages');
        typingIndicator = document.getElementById('inaraChatTyping');
        chipsContainer = document.getElementById('inaraChatChips');
        inputForm = document.getElementById('inaraChatForm');
        textInput = document.getElementById('inaraChatInput');

        if (!launcher || !chatWindow) return;

        // Event Listeners
        launcher.addEventListener('click', toggleChat);

        if (teaser) {
            teaser.addEventListener('click', function (e) {
                if (e.target.closest('.teaser-close')) {
                    e.stopPropagation();
                    cerrarTeaser();
                } else {
                    abrirChat();
                }
            });

            // Mostrar teaser después de 2.2 segundos si no fue cerrado antes
            if (!sessionStorage.getItem(STORAGE_KEY_TEASER_DISMISSED)) {
                setTimeout(function () {
                    if (!chatWindow.classList.contains('activo') && teaser) {
                        teaser.style.display = 'flex';
                    }
                }, 2200);
            }
        }

        const btnCerrar = document.getElementById('inaraBtnCerrar');
        if (btnCerrar) btnCerrar.addEventListener('click', cerrarChat);

        const btnReset = document.getElementById('inaraBtnReset');
        if (btnReset) btnReset.addEventListener('click', reiniciarChat);

        if (inputForm) {
            inputForm.addEventListener('submit', function (e) {
                e.preventDefault();
                const texto = textInput.value.trim();
                if (texto) {
                    enviarMensajeUsuario(texto);
                }
            });
        }

        // Cargar historial si existe o inicializar mensaje de bienvenida
        cargarHistorialOIniciar();
    }

    function toggleChat() {
        if (chatWindow.classList.contains('activo')) {
            cerrarChat();
        } else {
            abrirChat();
        }
    }

    function abrirChat() {
        chatWindow.classList.add('activo');
        launcher.classList.add('abierto');
        cerrarTeaser();

        if (badge) {
            badge.style.display = 'none';
        }

        setTimeout(function () {
            scrollAlFinal();
            if (textInput) textInput.focus();
        }, 150);
    }

    function cerrarChat() {
        chatWindow.classList.remove('activo');
        launcher.classList.remove('abierto');
    }

    function cerrarTeaser() {
        if (teaser) {
            teaser.style.display = 'none';
            sessionStorage.setItem(STORAGE_KEY_TEASER_DISMISSED, 'true');
        }
    }

    function reiniciarChat() {
        sessionStorage.removeItem(STORAGE_KEY_HISTORIAL);
        messagesContainer.innerHTML = '';
        renderMensajeBienvenida();
    }

    function cargarHistorialOIniciar() {
        const guardado = sessionStorage.getItem(STORAGE_KEY_HISTORIAL);
        if (guardado) {
            try {
                const items = JSON.parse(guardado);
                if (Array.isArray(items) && items.length > 0) {
                    items.forEach(function (m) {
                        renderMensajeHTML(m.remitente, m.texto, m.accionUrl, m.accionTexto, m.hora);
                    });
                    if (items[items.length - 1].sugerencias) {
                        actualizarChips(items[items.length - 1].sugerencias);
                    }
                    return;
                }
            } catch (e) {
                sessionStorage.removeItem(STORAGE_KEY_HISTORIAL);
            }
        }
        renderMensajeBienvenida();
    }

    function renderMensajeBienvenida() {
        const nombreDisplay = userName ? `<strong>${escaparHTML(userName)}</strong>` : 'estimado(a)';
        const textoBienvenida = `¡Hola ${nombreDisplay}! ✨ Soy <strong>Sofía</strong>, tu asesora virtual de <strong>INARA</strong>.<br><br>`
            + `Estoy aquí para orientarte en la planificación de tu evento exclusivo, cotizaciones, citas con nuestros planners y más.<br><br>`
            + `¿En qué te puedo asesorar hoy?`;

        const sugerenciasDefault = [
            "📅 Agendar Cita",
            "💍 Paquetes de Bodas",
            "🎆 Quinceañeros",
            "💰 Mis Cotizaciones",
            "🍾 Catering Gourmet",
            "💬 WhatsApp Asesor"
        ];

        agregarMensaje('bot', textoBienvenida, null, null, sugerenciasDefault);
    }

    function enviarMensajeUsuario(texto) {
        textInput.value = '';
        agregarMensaje('user', texto);
        mostrarEscribiendo(true);

        // Llamar al Servlet en el backend
        const endpoint = (contextPath ? contextPath : '') + '/api/chatbot';
        const params = new URLSearchParams();
        params.append('mensaje', texto);

        fetch(endpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
            },
            body: params.toString()
        })
            .then(function (res) {
                if (!res.ok) throw new Error('Error en el servidor');
                return res.json();
            })
            .then(function (data) {
                mostrarEscribiendo(false);
                if (data && data.respuesta) {
                    agregarMensaje('bot', data.respuesta, data.accionUrl, data.accionTexto, data.sugerencias);
                } else {
                    fallbackClientSide(texto);
                }
            })
            .catch(function (err) {
                console.warn('[INARA Chatbot] Fallback activado:', err);
                mostrarEscribiendo(false);
                fallbackClientSide(texto);
            });
    }

    function agregarMensaje(remitente, texto, accionUrl, accionTexto, sugerencias) {
        const hora = obtenerHoraActual();
        renderMensajeHTML(remitente, texto, accionUrl, accionTexto, hora);

        if (sugerencias && Array.isArray(sugerencias)) {
            actualizarChips(sugerencias);
        }

        guardarEnHistorial(remitente, texto, accionUrl, accionTexto, sugerencias, hora);
        scrollAlFinal();
    }

    function renderMensajeHTML(remitente, texto, accionUrl, accionTexto, hora) {
        const esBot = (remitente === 'bot');
        const fila = document.createElement('div');
        fila.className = 'inara-msg-row ' + (esBot ? 'bot' : 'user');

        let avatarHTML = '';
        if (esBot) {
            avatarHTML = `<div class="inara-msg-avatar" title="Sofía">S</div>`;
        }

        let botonAccionHTML = '';
        if (accionUrl && accionTexto) {
            const target = accionUrl.startsWith('http') ? 'target="_blank" rel="noopener noreferrer"' : '';
            botonAccionHTML = `
                <div>
                    <a href="${accionUrl}" ${target} class="inara-msg-action-btn">
                        <span>${escaparHTML(accionTexto)}</span>
                        <svg viewBox="0 0 24 24" style="width: 14px; height: 14px; fill: currentColor;"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
                    </a>
                </div>
            `;
        }

        fila.innerHTML = `
            ${avatarHTML}
            <div class="inara-msg-bubble">
                <div class="inara-msg-content">${texto}</div>
                ${botonAccionHTML}
                <div class="inara-msg-time">${hora || obtenerHoraActual()}</div>
            </div>
        `;

        messagesContainer.appendChild(fila);
    }

    function actualizarChips(sugerencias) {
        if (!chipsContainer) return;
        chipsContainer.innerHTML = '';

        if (!sugerencias || sugerencias.length === 0) {
            chipsContainer.style.display = 'none';
            return;
        }

        chipsContainer.style.display = 'flex';
        sugerencias.forEach(function (sug) {
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'inara-chip-btn';
            btn.textContent = sug;
            btn.addEventListener('click', function () {
                enviarMensajeUsuario(sug);
            });
            chipsContainer.appendChild(btn);
        });
        chipsContainer.scrollLeft = 0;
    }

    function mostrarEscribiendo(activo) {
        if (!typingIndicator) return;
        if (activo) {
            typingIndicator.classList.add('activo');
            messagesContainer.appendChild(typingIndicator);
            scrollAlFinal();
        } else {
            typingIndicator.classList.remove('activo');
        }
    }

    function scrollAlFinal() {
        if (messagesContainer) {
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        }
    }

    function guardarEnHistorial(remitente, texto, accionUrl, accionTexto, sugerencias, hora) {
        let historial = [];
        try {
            const prev = sessionStorage.getItem(STORAGE_KEY_HISTORIAL);
            if (prev) historial = JSON.parse(prev);
        } catch (e) {
            historial = [];
        }

        historial.push({
            remitente: remitente,
            texto: texto,
            accionUrl: accionUrl || null,
            accionTexto: accionTexto || null,
            sugerencias: sugerencias || null,
            hora: hora
        });

        // Limitar historial a últimos 30 mensajes
        if (historial.length > 30) {
            historial = historial.slice(historial.length - 30);
        }

        try {
            sessionStorage.setItem(STORAGE_KEY_HISTORIAL, JSON.stringify(historial));
        } catch (e) {
            // Manejo silencioso de cuota excedida
        }
    }

    function obtenerHoraActual() {
        const ahora = new Date();
        let h = ahora.getHours();
        let m = ahora.getMinutes();
        const ampm = h >= 12 ? 'pm' : 'am';
        h = h % 12;
        h = h ? h : 12;
        m = m < 10 ? '0' + m : m;
        return `${h}:${m} ${ampm}`;
    }

    function escaparHTML(s) {
        if (!s) return '';
        return s.replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    /**
     * Fallback cliente en caso de desconexión momentánea de red
     */
    function fallbackClientSide(texto) {
        const t = texto.toLowerCase();
        let resp = "Con gusto te ayudo con tu consulta. ";
        let sugerencias = ["📅 Agendar Cita", "💍 Bodas", "💰 Cotizaciones", "💬 Hablar con Asesor"];
        let accionUrl = (contextPath || '') + "/citas?accion=nueva";
        let accionTexto = "📅 Agendar Cita";

        if (t.includes('boda') || t.includes('matrimonio')) {
            resp = "💍 En <strong>INARA</strong> diseñamos bodas de lujo memorables con catering de autor, toldos arquitectónicos y producción audiovisual de primer nivel.";
            sugerencias = ["📅 Agendar Cita", "🍾 Catering", "💬 Cotizar Boda"];
        } else if (t.includes('quince') || t.includes('xv')) {
            resp = "🎆 Creamos fiestas de quinceañeros temáticas con tecnología de iluminación, shows en vivo y pistas LED.";
            sugerencias = ["📅 Agendar Cita", "🍾 Catering", "💬 WhatsApp"];
        } else if (t.includes('cita') || t.includes('reunion')) {
            resp = "Puedes agendar una cita privada con nuestros planners de manera presencial en nuestro Showroom o por videollamada.";
            accionUrl = (contextPath || '') + "/citas?accion=nueva";
            accionTexto = "📅 Agendar Cita";
        } else if (t.includes('cotiz') || t.includes('precio') || t.includes('presupuesto')) {
            resp = "Nuestras propuestas se cotizan de forma personalizada según el número de invitados y los servicios elegidos.";
            accionUrl = (contextPath || '') + "/mis-cotizaciones";
            accionTexto = "💰 Ver Cotizaciones";
        } else {
            resp += "¿Deseas coordinar los detalles con un asesor humano por WhatsApp o agendar una cita de asesoría?";
            accionUrl = "https://wa.me/51992937943?text=" + encodeURIComponent("Hola INARA, quisiera información sobre sus servicios de eventos.");
            accionTexto = "📲 Hablar por WhatsApp";
        }

        agregarMensaje('bot', resp, accionUrl, accionTexto, sugerencias);
    }

    // Exponer API global opcional
    window.InaraChat = {
        abrir: abrirChat,
        cerrar: cerrarChat,
        enviar: enviarMensajeUsuario,
        reiniciar: reiniciarChat
    };

})();
