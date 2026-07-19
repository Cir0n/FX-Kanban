package fr.esgi.fx.kanban.service;

public interface IEmailService {

    void envoyerNotificationAssignation(String destinataire, String nomTache);
}
