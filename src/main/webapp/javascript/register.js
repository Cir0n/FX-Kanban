/* ================================================
   KANBAN — Page d'inscription (register.html)
   ================================================ */

document.addEventListener('DOMContentLoaded', function () {

    var registerForm = document.getElementById('registerForm');
    if (!registerForm) {
        return;
    }

    // ── Validation côté client ───────────────────
    registerForm.addEventListener('submit', function (e) {
        var pseudo = document.getElementById('pseudo');
        var email = document.getElementById('email');
        var password = document.getElementById('password');
        var confirmPassword = document.getElementById('confirmPassword');
        var valid = true;

        Kanban.clearFieldError(pseudo);
        Kanban.clearFieldError(email);
        Kanban.clearFieldError(password);
        Kanban.clearFieldError(confirmPassword);

        if (!pseudo.value.trim()) {
            Kanban.showFieldError(pseudo, 'Le pseudo est requis.');
            valid = false;
        }

        if (!email.value.trim()) {
            Kanban.showFieldError(email, 'L\'email est requis.');
            valid = false;
        } else if (!Kanban.isValidEmail(email.value)) {
            Kanban.showFieldError(email, 'Format d\'email invalide.');
            valid = false;
        }

        if (!password.value || password.value.length < 8) {
            Kanban.showFieldError(password, 'Le mot de passe doit contenir au moins 8 caractères.');
            valid = false;
        }

        if (confirmPassword.value !== password.value) {
            Kanban.showFieldError(confirmPassword, 'Les mots de passe ne correspondent pas.');
            valid = false;
        }

        if (!valid) {
            e.preventDefault();
        }
    });

});
