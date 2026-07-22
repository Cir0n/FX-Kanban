/* ================================================
   KANBAN — JavaScript commun à toutes les pages
   Chargé en premier : expose les helpers partagés
   dans window.Kanban pour les scripts de page.
   ================================================ */

window.Kanban = (function () {

    // ── Helpers de validation de formulaire ──────

    function showFieldError(input, message) {
        input.classList.add('is-invalid');
        var feedback = input.parentElement.querySelector('.invalid-feedback');
        if (feedback) {
            feedback.textContent = message;
            feedback.style.display = 'block';
        }
    }

    function clearFieldError(input) {
        input.classList.remove('is-invalid');
        var feedback = input.parentElement.querySelector('.invalid-feedback');
        if (feedback) {
            feedback.style.display = 'none';
        }
    }

    function isValidEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    }

    return {
        showFieldError: showFieldError,
        clearFieldError: clearFieldError,
        isValidEmail: isValidEmail
    };

})();

document.addEventListener('DOMContentLoaded', function () {

    // ── Auto-dismiss des alertes après 5s ────────
    var alerts = document.querySelectorAll('.alert-error, .alert-success');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            alert.style.transition = 'opacity 0.3s';
            alert.style.opacity = '0';
            setTimeout(function () {
                alert.remove();
            }, 300);
        }, 5000);
    });

});
