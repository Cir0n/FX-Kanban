/* ================================================
   KANBAN — Création d'un tableau (board-new.html)
   ================================================ */

document.addEventListener('DOMContentLoaded', function () {

    var boardForm = document.getElementById('boardForm');
    if (!boardForm) {
        return;
    }

    // ── Validation côté client ───────────────────
    boardForm.addEventListener('submit', function (e) {
        var name = document.getElementById('name');
        var valid = true;

        Kanban.clearFieldError(name);

        if (!name.value.trim()) {
            Kanban.showFieldError(name, 'Le nom du tableau est requis.');
            valid = false;
        } else if (name.value.trim().length > 60) {
            Kanban.showFieldError(name, 'Le nom ne doit pas dépasser 60 caractères.');
            valid = false;
        }

        if (!valid) {
            e.preventDefault();
        }
    });

});
