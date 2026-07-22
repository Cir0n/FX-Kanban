/* ================================================
   KANBAN — Tableau de bord (dashboard.html)
   Menu d'actions sur une carte de tableau
   ================================================ */

document.addEventListener('DOMContentLoaded', function () {

    // Les cartes sont des liens ; on empêche le menu (et ses actions) de
    // déclencher la navigation vers le tableau.
    document.querySelectorAll('.board-card-menu').forEach(function (menu) {
        menu.addEventListener('click', function (e) {
            e.preventDefault();
            e.stopPropagation();
        });
    });

    var editBoardModal = document.getElementById('modal-edit-board');
    document.querySelectorAll('[data-edit-board]').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var idInput = document.getElementById('editBoardId');
            var nameInput = document.getElementById('editBoardName');
            if (idInput) {
                idInput.value = btn.getAttribute('data-board-id');
            }
            if (nameInput) {
                nameInput.value = btn.getAttribute('data-board-name');
                Kanban.clearFieldError(nameInput);
            }
            Kanban.openModal(editBoardModal);
        });
    });

    var editBoardForm = document.getElementById('editBoardForm');
    if (editBoardForm) {
        editBoardForm.addEventListener('submit', function (e) {
            var name = document.getElementById('editBoardName');
            Kanban.clearFieldError(name);
            if (!name.value.trim()) {
                Kanban.showFieldError(name, 'Le nom du tableau est requis.');
                e.preventDefault();
            }
        });
    }

    var deleteBoardForm = document.getElementById('deleteBoardForm');
    var deleteBoardId = document.getElementById('deleteBoardId');
    document.querySelectorAll('[data-delete-board]').forEach(function (btn) {
        btn.addEventListener('click', function () {
            if (!window.confirm('Supprimer définitivement ce tableau et toutes ses tâches ?')) {
                return;
            }
            deleteBoardId.value = btn.getAttribute('data-board-id');
            deleteBoardForm.submit();
        });
    });

});
