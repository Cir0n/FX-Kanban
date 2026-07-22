package fr.esgi.fx.kanban.service;

import fr.esgi.fx.kanban.service.implementation.EmailServiceImpl;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Transport;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mockStatic;

class EmailServiceTest {

    @Test
    void testConstructor_shouldNotThrow() {
        assertDoesNotThrow(EmailServiceImpl::new);
    }

    @Test
    void testEnvoyerNotificationAssignation_whenDestinataireIsNull_shouldNotThrow() {
        EmailServiceImpl emailService = new EmailServiceImpl();

        assertDoesNotThrow(() -> emailService.envoyerNotificationAssignation(null, "Tache A"));
    }

    @Test
    void testEnvoyerNotificationAssignation_whenDestinataireIsBlank_shouldNotThrow() {
        EmailServiceImpl emailService = new EmailServiceImpl();

        assertDoesNotThrow(() -> emailService.envoyerNotificationAssignation(" ", "Tache A"));
    }

    @Test
    void testEnvoyerNotificationAssignation_whenConfigured_shouldSendMessage() throws Exception {
        EmailServiceImpl emailService = new EmailServiceImpl();
        forceConfiguredSmtp(emailService);

        try (MockedStatic<Transport> transportMock = mockStatic(Transport.class)) {
            emailService.envoyerNotificationAssignation("dev@test.com", "Kanban board");

            transportMock.verify(() -> Transport.send(org.mockito.ArgumentMatchers.any(Message.class)));
        }
    }

    @Test
    void testEnvoyerNotificationAssignation_whenTransportFails_shouldNotThrow() throws Exception {
        EmailServiceImpl emailService = new EmailServiceImpl();
        forceConfiguredSmtp(emailService);

        try (MockedStatic<Transport> transportMock = mockStatic(Transport.class)) {
            transportMock.when(() -> Transport.send(org.mockito.ArgumentMatchers.any(Message.class)))
                    .thenThrow(new MessagingException("SMTP KO"));

            assertDoesNotThrow(() -> emailService.envoyerNotificationAssignation("dev@test.com", "Kanban board"));
        }
    }

    @Test
    void testEnvoyerNotificationAssignation_whenConfiguredButTaskNameNull_shouldStillBuildMessage() throws Exception {
        EmailServiceImpl emailService = new EmailServiceImpl();
        forceConfiguredSmtp(emailService);

        try (MockedStatic<Transport> transportMock = mockStatic(Transport.class)) {
            emailService.envoyerNotificationAssignation("dev@test.com", null);

            transportMock.verify(() -> Transport.send(org.mockito.ArgumentMatchers.any(Message.class)));
        }
    }

    /**
     * Configure par réflexion les champs privés SMTP de EmailServiceImpl
     * pour simuler un environnement SMTP valide sans propriétés système réelles.
     * Remarque : ne contient pas d'assertions — c'est un helper de setup, pas un test.
     */
    private void forceConfiguredSmtp(EmailServiceImpl emailService) throws Exception {
        setField(emailService, "host", "smtp.test.local");
        setField(emailService, "port", "587");
        setField(emailService, "username", "no-reply@test.com");
        setField(emailService, "password", "secret");
        setField(emailService, "configure", true);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}

