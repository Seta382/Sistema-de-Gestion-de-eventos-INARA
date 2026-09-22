// Validaciones en cliente para login y registro INARA
document.addEventListener('DOMContentLoaded', () => {
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', (e) => {
            const passwordInput = form.querySelector('input[type="password"]');
            if (passwordInput && passwordInput.value.length < 6) {
                alert('La contraseña debe tener al menos 6 caracteres.');
                e.preventDefault();
                passwordInput.focus();
            }
        });
    });
});
