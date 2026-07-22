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

    // ── Modales génériques (utilisées par board.js et dashboard.js) ──
    // Un seul état partagé : une seule modale peut être ouverte à la fois,
    // quelle que soit la page.
    var openModalEl = null;

    function openModal(modal) {
        if (!modal) {
            return;
        }
        modal.hidden = false;
        document.body.style.overflow = 'hidden';
        openModalEl = modal;
    }

    function closeModal() {
        if (!openModalEl) {
            return;
        }
        // Repasse en mode lecture pour la prochaine ouverture de cette modale
        // (spécifique aux modales de détail de tâche ; no-op ailleurs).
        var view = openModalEl.querySelector('[data-task-view]');
        var editForm = openModalEl.querySelector('[data-task-edit]');
        var editBtn = openModalEl.querySelector('[data-edit-task]');
        if (view && editForm) {
            view.hidden = false;
            editForm.hidden = true;
        }
        if (editBtn) {
            editBtn.hidden = false;
        }
        openModalEl.hidden = true;
        document.body.style.overflow = '';
        openModalEl = null;
    }

    return {
        showFieldError: showFieldError,
        clearFieldError: clearFieldError,
        isValidEmail: isValidEmail,
        openModal: openModal,
        closeModal: closeModal
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

    // ── Fermeture des modales : bouton ✕, clic sur le fond, touche Échap ──
    // Générique : s'applique à toute modale présente sur la page (détail de
    // tâche, ajout de tâche, invitation, édition/suppression de tableau...).
    document.querySelectorAll('.modal-overlay').forEach(function (overlay) {
        overlay.addEventListener('click', function (e) {
            if (e.target === overlay || e.target.closest('[data-modal-close]')) {
                Kanban.closeModal();
            }
        });
    });

    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') {
            Kanban.closeModal();
        }
    });

});
