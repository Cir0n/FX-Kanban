package fr.esgi.fx.kanban.service.implementation;

import fr.esgi.fx.kanban.service.IEmailService;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

/**
 * Envoie les notifications par email. La configuration SMTP est lue depuis .env
 * (dev) ou les variables d'environnement (déploiement) ; en son absence, l'envoi
 * est simplement désactivé pour ne pas empêcher les autres opérations (ex. une
 * assignation de tâche ne doit jamais échouer à cause d'un email non envoyé).
 */
public class EmailServiceImpl implements IEmailService {

    private final String host;
    private final String port;
    private final String username;
    private final String password;
    private final boolean configure;

    public EmailServiceImpl() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().ignoreIfMalformed().load();

        this.host = dotenv.get("SMTP_HOST");
        this.port = dotenv.get("SMTP_PORT", "587");
        this.username = dotenv.get("SMTP_USERNAME");
        this.password = dotenv.get("SMTP_PASSWORD");
        this.configure = host != null && !host.isBlank()
                && username != null && !username.isBlank()
                && password != null && !password.isBlank();

        if (!configure) {
            System.err.println("[EMAIL] Configuration SMTP absente ou incomplète, "
                    + "les notifications par email sont désactivées.");
        }
    }

    @Override
    public void envoyerNotificationAssignation(String destinataire, String nomTache) {
        if (!configure || destinataire == null || destinataire.isBlank()) {
            return;
        }

        try {
            Message message = new MimeMessage(session());
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject("Vous avez été assigné à une tâche");
            message.setText("Vous avez été assigné à la tâche « " + nomTache + " ».");
            Transport.send(message);
        } catch (MessagingException e) {
            System.err.println("[EMAIL] Échec de l'envoi de la notification à "
                    + destinataire + " : " + e.getMessage());
        }
    }

    private Session session() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }
}
