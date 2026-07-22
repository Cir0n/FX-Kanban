/* ================================================
   KANBAN — Page de connexion (login.html)
   ================================================ */

document.addEventListener('DOMContentLoaded', function () {

    var loginForm = document.getElementById('loginForm');
    if (!loginForm) {
        return;
    }

    // ── Validation côté client ───────────────────
    loginForm.addEventListener('submit', function (e) {
        var pseudo = document.getElementById('pseudo');
        var password = document.getElementById('password');
        var valid = true;

        Kanban.clearFieldError(pseudo);
        Kanban.clearFieldError(password);

        if (!pseudo.value.trim()) {
            Kanban.showFieldError(pseudo, 'Le pseudo est requis.');
            valid = false;
        }

        if (!password.value) {
            Kanban.showFieldError(password, 'Le mot de passe est requis.');
            valid = false;
        }

        if (!valid) {
            e.preventDefault();
        }
    });

});
