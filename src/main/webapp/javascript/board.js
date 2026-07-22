/* ================================================
   KANBAN — Tableau (board.html)
   Modales de tâche + drag & drop des cartes
   ================================================ */

document.addEventListener('DOMContentLoaded', function () {

    // Ouverture au clic sur une carte
    document.querySelectorAll('[data-modal-target]').forEach(function (card) {
        card.addEventListener('click', function () {
            Kanban.openModal(document.getElementById(card.getAttribute('data-modal-target')));
        });
    });

    // Ouverture de la modale d'ajout de tâche (pré-remplie selon la colonne)
    var addTaskModal = document.getElementById('modal-add-task');
    document.querySelectorAll('.add-task').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var form = document.getElementById('addTaskForm');
            if (form) {
                form.reset();
                Kanban.clearFieldError(document.getElementById('taskName'));
            }
            var idInput = document.getElementById('addTaskColonneId');
            var nameLabel = document.getElementById('addTaskColonneName');
            if (idInput) {
                idInput.value = btn.getAttribute('data-colonne-id');
            }
            if (nameLabel) {
                nameLabel.textContent = btn.getAttribute('data-colonne-name');
            }
            Kanban.openModal(addTaskModal);
        });
    });

    // Validation côté client (ajout de tâche)
    var addTaskForm = document.getElementById('addTaskForm');
    if (addTaskForm) {
        addTaskForm.addEventListener('submit', function (e) {
            var name = document.getElementById('taskName');
            Kanban.clearFieldError(name);
            if (!name.value.trim()) {
                Kanban.showFieldError(name, 'Le nom de la tâche est requis.');
                e.preventDefault();
            }
        });
    }

    // ── Modale d'invitation d'un membre ──────────
    var inviteModal = document.getElementById('modal-invite');
    var inviteBtn = document.getElementById('inviteBtn');
    if (inviteBtn) {
        inviteBtn.addEventListener('click', function () {
            var form = document.getElementById('inviteForm');
            if (form) {
                form.reset();
                Kanban.clearFieldError(document.getElementById('invitePseudo'));
            }
            Kanban.openModal(inviteModal);
        });
    }

    var inviteForm = document.getElementById('inviteForm');
    if (inviteForm) {
        inviteForm.addEventListener('submit', function (e) {
            var pseudo = document.getElementById('invitePseudo');
            Kanban.clearFieldError(pseudo);
            if (!pseudo.value.trim()) {
                Kanban.showFieldError(pseudo, 'Le pseudo est requis.');
                e.preventDefault();
            }
        });
    }

    // ── Édition d'une tâche (bascule vue/formulaire dans la modale) ──
    document.querySelectorAll('[data-edit-task]').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var modal = btn.closest('.task-modal');
            var view = modal.querySelector('[data-task-view]');
            var editForm = modal.querySelector('[data-task-edit]');
            view.hidden = true;
            editForm.hidden = false;
            // Le bouton "Modifier" vit dans l'en-tête, hors de la zone vue/édition
            // togglée ci-dessus : sans ça il resterait affiché et cliquable
            // pendant l'édition.
            btn.hidden = true;
        });
    });

    document.querySelectorAll('[data-cancel-edit]').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var modal = btn.closest('.task-modal');
            var view = modal.querySelector('[data-task-view]');
            var editForm = modal.querySelector('[data-task-edit]');
            var editBtn = modal.querySelector('[data-edit-task]');
            view.hidden = false;
            editForm.hidden = true;
            if (editBtn) {
                editBtn.hidden = false;
            }
        });
    });

    // Validation côté client (édition de tâche)
    document.querySelectorAll('form[data-task-edit]').forEach(function (form) {
        form.addEventListener('submit', function (e) {
            var name = form.querySelector('input[name="name"]');
            Kanban.clearFieldError(name);
            if (!name.value.trim()) {
                Kanban.showFieldError(name, 'Le nom de la tâche est requis.');
                e.preventDefault();
            }
        });
    });

    // ── Suppression d'une tâche ───────────────────
    var deleteTaskForm = document.getElementById('deleteTaskForm');
    var deleteTaskId = document.getElementById('deleteTaskId');
    document.querySelectorAll('[data-delete-task]').forEach(function (btn) {
        btn.addEventListener('click', function () {
            if (!window.confirm('Supprimer définitivement cette tâche ?')) {
                return;
            }
            deleteTaskId.value = btn.getAttribute('data-task-id');
            deleteTaskForm.submit();
        });
    });

    // ── Suppression d'une pièce jointe ────────────
    var deleteAttachmentForm = document.getElementById('deleteAttachmentForm');
    var deleteAttachmentId = document.getElementById('deleteAttachmentId');
    document.querySelectorAll('[data-delete-attachment]').forEach(function (btn) {
        btn.addEventListener('click', function () {
            if (!window.confirm('Supprimer cette pièce jointe ?')) {
                return;
            }
            deleteAttachmentId.value = btn.getAttribute('data-attachment-id');
            deleteAttachmentForm.submit();
        });
    });

    // ── Drag & Drop des tâches ───────────────────
    var board = document.querySelector('.kanban-board');
    if (!board) {
        return;
    }

    var moveUrl = board.getAttribute('data-move-url');
    var draggedCard = null;

    // Rend chaque carte déplaçable
    document.querySelectorAll('.task-card').forEach(function (card) {
        card.addEventListener('dragstart', function (e) {
            draggedCard = card;
            card.classList.add('dragging');
            e.dataTransfer.effectAllowed = 'move';
            e.dataTransfer.setData('text/plain', card.getAttribute('data-task-id'));
        });
        card.addEventListener('dragend', function () {
            card.classList.remove('dragging');
            draggedCard = null;
            // Sécurité : on retire le surlignage de TOUTES les colonnes à la fin
            // du glisser, même celles survolées sans dépôt (leur dragleave ne
            // s'est pas toujours déclenché proprement).
            document.querySelectorAll('.kanban-column-tasks.drag-over')
                .forEach(function (z) { z.classList.remove('drag-over'); });
        });
    });

    // Chaque colonne est une zone de dépôt
    document.querySelectorAll('.kanban-column-tasks').forEach(function (zone) {
        zone.addEventListener('dragover', function (e) {
            e.preventDefault();
            e.dataTransfer.dropEffect = 'move';
            zone.classList.add('drag-over');
        });
        zone.addEventListener('dragleave', function (e) {
            if (e.target === zone) {
                zone.classList.remove('drag-over');
            }
        });
        zone.addEventListener('drop', function (e) {
            e.preventDefault();
            zone.classList.remove('drag-over');
            if (!draggedCard) {
                return;
            }
            var sourceZone = draggedCard.closest('.kanban-column-tasks');
            if (sourceZone === zone) {
                return; // Déposée dans sa colonne d'origine : rien à faire
            }

            var taskId = draggedCard.getAttribute('data-task-id');
            var colonneId = zone.getAttribute('data-colonne-id');

            // Déplacement optimiste dans l'affichage
            zone.appendChild(draggedCard);
            updateColumnCounts();

            // Persistance côté serveur
            var params = new URLSearchParams();
            params.set('taskId', taskId);
            params.set('colonneId', colonneId);
            fetch(moveUrl, {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: params.toString()
            }).then(function (response) {
                if (!response.ok) {
                    throw new Error('Déplacement refusé (' + response.status + ')');
                }
            }).catch(function () {
                // Échec : on remet la carte dans sa colonne d'origine
                sourceZone.appendChild(draggedCard);
                updateColumnCounts();
                alert("Le déplacement n'a pas pu être enregistré. Réessayez.");
            });
        });
    });

    // Recalcule le compteur de tâches affiché dans l'en-tête de chaque colonne
    function updateColumnCounts() {
        document.querySelectorAll('.kanban-column').forEach(function (col) {
            var badge = col.querySelector('.kanban-column-count');
            if (badge) {
                badge.textContent = col.querySelectorAll('.task-card').length;
            }
        });
    }

});
